package se.radley.ember

import io.Source
import java.io.{InputStreamReader, File}
import play.api.PlayException
import sbt.FeedbackProvidedException

object EmberCompiler {

  import org.mozilla.javascript._
  import org.mozilla.javascript.tools.shell._

  import scala.collection.JavaConverters._

  import scalax.file._

  // Ember filename
  val emberHeadless = "ember-headless.js"
  val handlebars = "handlebars-1.0.rc.1.js"
  val ember = "ember-1.0.0-pre.3.js"

  // Setup rhino context to execute the javascript within
  val ctx = Context.enter
  val global = new Global; global.init(ctx)
  val scope = ctx.initStandardObjects(global)

  // Loads resource into rhino context
  def loadIntoContext(resource: String) {
    ctx.evaluateReader(
      scope,
      new InputStreamReader(this.getClass.getClassLoader.getResource(resource).openConnection().getInputStream()),
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

  // Exit the context since we now have the reference to the precompile function
  Context.exit()

	def compile(source: File, options: Seq[String] = Seq.empty) = {
	  val handlebarsCode = Path(source).string.replace("\r", "")
    println(handlebarsCode)
	  try {
      Context.call(null, precompileFunction, scope, scope, Array(handlebarsCode)).asInstanceOf[String]
    } catch {
      case e: JavaScriptException =>
        val line = """.*on line ([0-9]+).*""".r
        val error = e.getValue.asInstanceOf[Scriptable]

        println("ERROROR: \"" + ScriptableObject.getProperty(error, "message") + "\"")

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
        s"unexpected exception during Ember compilation (file=$source, options=$options, ember=$ember): $e",
        None,
        None
      )
    }
	}

  def render(templateFile: File, context: AnyRef) = {
    scope.put("render", scope, compile(templateFile))
    val render = scope.get("render", scope).asInstanceOf[Function]
    Context.call(null, render, scope, scope, Array(context)).asInstanceOf[String]
  }
}

case class CompilationException(source: Option[File], message: String, atLine: Option[Int], column: Option[Int])
  extends PlayException.ExceptionSource("Compilation error", message)
  with FeedbackProvidedException {

  def line = atLine.map(_.asInstanceOf[java.lang.Integer]).orNull
  def position = column.map(_.asInstanceOf[java.lang.Integer]).orNull
  def input = source.filter(_.exists()).map(Source.fromFile(_).mkString).orNull
  def sourceName = source.map(_.getAbsolutePath).orNull
}
