package se.radley.ember

import sbt._
import Keys._
import org.apache.commons.io.FilenameUtils

object EmberPlugin extends Plugin with EmberKeys with PlayAssetsCompiler {

  override def settings: Seq[Setting[_]] = super.buildSettings ++ Seq(
    emberEntryPoints <<= (sourceDirectory in Compile)(base => base / "views"),
    emberExtension := ".handlebars",
    emberOptions := Seq.empty[String],
    resourceGenerators in Compile <+= EmberSbtCompiler

    /*emberAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets")),
    emberFileEnding := ".handlebars",
    emberAssetsGlob <<= (emberAssetsDir)(assetsDir => assetsDir ** "*.handlebars"),
    emberFileRegexFrom <<= (emberFileEnding)(fileEnding => fileEnding),
    emberFileRegexTo <<= (emberFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),
    resourceGenerators in Compile <+= EmberCompiler)
    */
  )

  val EmberSbtCompiler = AssetsCompiler("ember-handlebars",
    (_ ** s"*$emberExtension"),
    emberEntryPoints,
    { (name, min) => "javascripts/" + name + ".pre" + (if (min) ".min.js" else ".js") },
    { (handlebarsFile, options) =>
      import scala.util.control.Exception._

      // options aren't used at the moment
      val (jsSource, dependencies) = EmberCompiler.compileDir(handlebarsFile, options)
      // Any error here would be because of Handlebars, not the developer, so we don't want compilation to fail.
      val minified = catching(classOf[CompilationException]).opt(play.core.jscompile.JavascriptCompiler.minify(jsSource, Some(handlebarsFile.getName())))
      (jsSource, minified, dependencies)
    },
    emberOptions
  )
}
