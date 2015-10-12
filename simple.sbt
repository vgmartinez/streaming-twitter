import AssemblyKeys._

name := "StreamingML"

version := "1.0"

scalaVersion := "2.10.4"

val SPARK_VERSION = "1.4.0"

libraryDependencies += "org.apache.spark" %% "spark-core" % SPARK_VERSION

libraryDependencies += "org.apache.spark" %% "spark-mllib" % SPARK_VERSION

libraryDependencies += "org.apache.spark" %% "spark-sql" % SPARK_VERSION

libraryDependencies += "org.apache.spark" %% "spark-streaming" % SPARK_VERSION

libraryDependencies += "org.apache.spark" %% "spark-streaming-twitter" % SPARK_VERSION

libraryDependencies += "com.google.code.gson" % "gson" % "2.3"

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "3.0.3"

libraryDependencies += "commons-cli" % "commons-cli" % "1.2"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

assemblySettings

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}