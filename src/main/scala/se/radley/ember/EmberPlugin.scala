package se.radley.ember

import sbt._
import Keys._

trait EmberKeys {
  lazy val emberEntryPoints = SettingKey[PathFinder]("play-ember-entry-points")
  lazy val emberExtension = SettingKey[String]("play-ember-extension")
  lazy val emberOptions = SettingKey[Seq[String]]("play-ember-options")
}

object EmberPlugin extends Plugin with EmberKeys with PlayAssetsCompiler {

  override def projectSettings: Seq[Setting[_]] = super.projectSettings ++ Seq(
    emberEntryPoints <<= (sourceDirectory in Compile)(base => base / "assets" / "templates"),
    emberExtension := ".handlebars",
    emberOptions := Seq.empty[String],
    resourceGenerators in Compile <+= EmberSbtCompiler
  )

  val EmberSbtCompiler = AssetsCompiler("ember-handlebars",
    (_ ** ("*" + emberExtension)),
    emberEntryPoints,
    { (name, min) => "javascripts/" + name + ".pre" + (if (min) ".min.js" else ".js") },
    { (handlebarsFile, options) =>
      import scala.util.control.Exception._
      val (jsSource, dependencies) = EmberCompiler.compileDir(handlebarsFile, options)
      val minified = catching(classOf[CompilationException]).opt(play.core.jscompile.JavascriptCompiler.minify(jsSource, Some(handlebarsFile.getName())))
      (jsSource, minified, dependencies)
    },
    emberOptions
  )
}
