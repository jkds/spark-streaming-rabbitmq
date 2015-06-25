net.virtualvoid.sbt.graph.Plugin.graphSettings

name := "spark-streaming-rabbitmq-example"
version := "0.1"

scalaVersion := "2.10.5"

val SparkVersion = "1.2.1"
val SparkCassandraVersion = "1.2.1"

resolvers += "The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/"

libraryDependencies ++= Seq(
  ("org.apache.spark" %%  "spark-core"  % SparkVersion).
    exclude("org.eclipse.jetty.orbit", "javax.transaction").
    exclude("org.eclipse.jetty.orbit", "javax.mail").
    exclude("org.eclipse.jetty.orbit", "javax.activation").
    exclude("org.slf4j","slf4j-log4j12").
    exclude("commons-beanutils", "commons-beanutils-core").
    exclude("commons-collections", "commons-collections").
    exclude("commons-collections", "commons-collections").
    exclude("com.esotericsoftware.minlog", "minlog")
)

libraryDependencies ++= Seq(
  "org.apache.spark"        %%  "spark-streaming"                         % SparkVersion,
  "com.datastax.spark"      %%  "spark-cassandra-connector"               % SparkCassandraVersion,
  "com.rabbitmq"            %   "amqp-client"                             % "3.5.3",
  ("com.thenewmotion.akka"  %%  "akka-rabbitmq"                           % "1.2.4").exclude(org="com.typesafe.akka",name="akka-actor_2.10"),
  "com.typesafe.akka"       %%  "akka-slf4j"                              % "2.3.7",
  "ch.qos.logback"          %   "logback-classic"                         % "1.1.3"
)

mainClass in assembly := Some("com.datastax.demo.DemoApp")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter {!_.data.getName.matches("(amqp-client.*|akka-rabbitmq.*)")}
}
