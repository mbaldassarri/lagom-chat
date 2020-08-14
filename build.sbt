
organization in ThisBuild := "it.unibo"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.0"

lagomServiceGatewayPort in ThisBuild := 3000
lagomServiceLocatorPort in ThisBuild := 3001
lagomCassandraCleanOnStart in ThisBuild := true
//lagomCassandraPort in ThisBuild := 9043
//lagomCassandraJvmOptions in ThisBuild :=
//  Seq("-Xms256m", "-Xmx1024m", "-Dcassandra.jmx.local.port=4099") // these are actually the default jvm options

val postgresDriverVersion = "42.2.14"
val hibernateCoreVersion = "5.4.18.Final"
val javaxAnnotationVersion = "1.3.2"
val immutablesVersion =  "2.8.2"
val lombokVersion = "1.18.8"
val mockitoVersion = "1.10.19"

// Message Dispatcher Service
lazy val `message-dispatcher-api` = project("message-dispatcher-api")
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      "org.immutables" % "value" % immutablesVersion, //IMMUTABLES
      lagomJavadslImmutables, //IMMUTABLES
      lagomJavadslJackson,
      "javax.annotation" % "javax.annotation-api" % javaxAnnotationVersion, //javax.annotation
      "org.projectlombok" % "lombok" % lombokVersion
    )
  )

lazy val `message-dispatcher-impl` = project("message-dispatcher-impl")
  .enablePlugins(LagomJava)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      lagomJavadslPubSub,
      "org.projectlombok" % "lombok" % lombokVersion
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`message-dispatcher-api`, `channel-api`)

// Channel Service
lazy val `channel-api` = project("channel-api")
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      "org.immutables" % "value" % immutablesVersion, //IMMUTABLES
      lagomJavadslImmutables, //IMMUTABLES
      lagomJavadslJackson,
      "javax.annotation" % "javax.annotation-api" % javaxAnnotationVersion, //javax.annotation
      "org.projectlombok" % "lombok" % lombokVersion,
      "org.mockito" % "mockito-core" % mockitoVersion
    )
  )

lazy val `channel-impl` = project("channel-impl")
  .enablePlugins(LagomJava)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPubSub,
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      "org.immutables" % "value" % immutablesVersion, //IMMUTABLES
      lagomJavadslImmutables, //IMMUTABLES
      "org.projectlombok" % "lombok" % lombokVersion
    )
  )
  .settings(lagomForkedTestSettings: _*) // Test Settings must be forked when using Cassandra
  .dependsOn(`channel-api`)


// User Service
lazy val `user-api` = project("user-api")
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      "org.projectlombok" % "lombok" % lombokVersion
    )
  ).dependsOn(`channel-api`)

lazy val `user-impl` = project("user-impl")
  .enablePlugins(LagomJava)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceJdbc,
      lagomJavadslPersistenceJpa,
      "org.postgresql" % "postgresql" % postgresDriverVersion,
      "org.hibernate" % "hibernate-core" % hibernateCoreVersion,
      lagomLogback,
      lagomJavadslTestKit,
      "org.mockito" % "mockito-core" % mockitoVersion,
      "org.projectlombok" % "lombok" % lombokVersion
    )
  )
  .dependsOn(`user-api`, `channel-api`)

def project(id: String) = Project(id, base = file(id))
  .settings(javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"))
  .settings(jacksonParameterNamesJavacSettings: _*) // applying it to every project even if not strictly needed.

// See https://github.com/FasterXML/jackson-module-parameter-names
lazy val jacksonParameterNamesJavacSettings = Seq(
  javacOptions in compile += "-parameters"
)

initialize := {
  val _ = initialize.value
  val javaVersion = sys.props("java.specification.version")
  if (javaVersion != "1.8")
    sys.error("Java 1.8 is required for this project. Found " + javaVersion + " instead")
}

def common = Seq(
  javacOptions in Compile += "-parameters"
)