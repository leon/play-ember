package se.radley.ember

import io.Source
import java.io._
import play.api.PlayException
import sbt.FeedbackProvidedException

case class CompilationException(source: Option[File], message: String, atLine: Option[Int], column: Option[Int])
  extends PlayException.ExceptionSource("Compilation error", message)
  with FeedbackProvidedException {

  def line = atLine.map(_.asInstanceOf[java.lang.Integer]).orNull
  def position = column.map(_.asInstanceOf[java.lang.Integer]).orNull
  def input = source.filter(_.exists()).map(Source.fromFile(_).mkString).orNull
  def sourceName = source.map(_.getAbsolutePath).orNull
}

object EmberCompiler {

  val emberHeadless = "ember-headless.js"
  val handlebars = "handlebars-1.0.rc.1.js"
  val ember = "ember-1.0.0-pre.x.js"

  import org.mozilla.javascript._
  import org.mozilla.javascript.tools.shell._

  import scala.collection.JavaConverters._

  import scalax.file._

  // Should be lazy val, but fails :(
  private def compiler = {
    // Setup rhino context to execute the javascript within
    val ctx = Context.enter; ctx.setOptimizationLevel(-1)
    val global = new Global; global.init(ctx)
    val scope = ctx.initStandardObjects(global)

    //val wrappedEmberCompiler = Context.javaToJS(this, scope)
    //ScriptableObject.putProperty(scope, "EmberCompiler", wrappedEmberCompiler)

    // Loads resource into rhino context
    def loadIntoContext(resource: String) {
      ctx.evaluateReader(
        scope,
        new InputStreamReader(this.getClass.getClassLoader.getResource(resource).openConnection().getInputStream(), "UTF-8"),
        resource,
        1,
        null
      )
    }

    // Add fake browser environment
    loadIntoContext(emberHeadless)

    // Load handlebars file and add it to the rhino scope
    loadIntoContext(handlebars)

    // Load the ember.js file and add it to the rhino scope
    loadIntoContext(ember)

    // Get the precompile function on the scope
    val precompileFunction = scope.get("precompile", scope).asInstanceOf[Function]

    // Get the render function on the scope
    val renderFunction = scope.get("render", scope).asInstanceOf[Function]

    // Exit the context since we now have the reference to the precompile function
    Context.exit()

    (source: File, options: Seq[String]) => {
      val handlebarsCode = Path(source).string.replace("\r", "")
      println(handlebarsCode)
      //val options = ctx.newObject(scope)
      //options.put("bare", options, bare)
      Context.call(null, precompileFunction, scope, scope, Array(handlebarsCode)).asInstanceOf[String]
    }
  }

  def compile(source: File, options: Seq[String] = Seq.empty) = {
    try {
      compiler(source, options)
    } catch {
      case e: JavaScriptException =>
        val line = """.*on line ([0-9]+).*""".r
        val error = e.getValue.asInstanceOf[Scriptable]

        //println("ERROROR: \"" + ScriptableObject.getProperty(error, "message") + "\"")

        throw ScriptableObject.getProperty(error, "message").asInstanceOf[String] match {
          case msg @ line(l) => CompilationException(
            Some(source),
            msg,
            Some(Integer.parseInt(l)),
            None
          )
          case msg => CompilationException(
            Some(source),
            msg,
            None,
            None
          )
        }

      case e: Throwable => throw CompilationException(
        Some(source),
        "Unexpected exception during Ember compilation (file=%s, ember=%s): %s".format(source, ember, e),
        None,
        None
      )
    }
  }

  def compileDir(root: File, options: Seq[String]): (String, Seq[File]) = {
    val dependencies = Seq.newBuilder[File]

    val o = new StringBuilder
    o ++= "(function() {\n"
    o ++= "var template = Ember.Handlebars.template;\n"
    o ++= "var templates = Ember.TEMPLATES = Ember.TEMPLATES || {};\n\n"

    def addTemplateDir(dir: File, path: String) {
      for {
        file <- dir.listFiles.toSeq.sortBy(_.getName)
        name = file.getName
      } {
        if (file.isDirectory) {
          addTemplateDir(file, path + name + File.separator)
        }
        else if (file.isFile && name.endsWith(".handlebars")) {
          val templateName = path + name.replace(".handlebars", "")
          println("play-ember: processing template %s".format(templateName))

          val jsSource = compile(file, options)
          dependencies += file
          o ++= "templates['"
          o ++= templateName
          o ++= "'] = template("
          o ++= jsSource
          o ++= ");\n\n"
        }
      }
    }
    addTemplateDir(root, "")

    o ++= "})();\n"
    (o.toString, dependencies.result)
  }
}
