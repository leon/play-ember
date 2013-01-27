// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.7")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.2")
