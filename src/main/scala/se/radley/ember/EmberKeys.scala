package se.radley.ember

import sbt._

trait EmberKeys {
  lazy val emberEntryPoints = SettingKey[PathFinder]("play-ember-entry-points")
  lazy val emberExtension = SettingKey[String]("play-ember-extension")
  lazy val emberOptions = SettingKey[Seq[String]]("play-ember-options")

  /*lazy val emberFileRegexFrom = SettingKey[String]("play-ember-file-regex-from")
  lazy val emberFileRegexTo = SettingKey[String]("play-ember-file-regex-to")
  lazy val emberAssetsGlob = SettingKey[PathFinder]("play-ember-assets-glob")
  lazy val emberAssetsDir = SettingKey[File]("play-ember-assets-dir")
  lazy val emberFileEnding = SettingKey[String]("play-ember-file-ending")*/
}