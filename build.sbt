ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.0.0"
ThisBuild / version      := "0.1.0-SNAPSHOT"
val AkkaHttpVersion = "10.2.5"

lazy val root = (project in file("."))
  .aggregate(server, client, shared.jvm, shared.js)

lazy val server = project
  .settings(
    scalaJSProjects := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      ("com.typesafe.akka" %% "akka-http" % AkkaHttpVersion).cross(CrossVersion.for3Use2_13),
      ("com.typesafe.akka" %% "akka-stream" % "2.6.15").cross(CrossVersion.for3Use2_13),
      ("com.typesafe.akka" %% "akka-http-caching" % AkkaHttpVersion).cross(CrossVersion.for3Use2_13),
      ("com.vmunier" %% "scalajs-scripts" % "1.2.0").cross(CrossVersion.for3Use2_13)
    ),
    libraryDependencies := libraryDependencies.value.map {
      case module if module.name == "twirl-api" =>
        module.cross(CrossVersion.for3Use2_13)
      case module => module
    },
    Assets / WebKeys.packagePrefix := "public/",
    Runtime / managedClasspath += (Assets / packageBin).value
  )
  .enablePlugins(SbtWeb,SbtTwirl,  JavaAppPackaging)
  .dependsOn(shared.jvm)


lazy val client = project
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "0.13.1",
      "com.lihaoyi" %%% "upickle" % "1.3.13",
      "com.raquo" %%% "waypoint" % "0.4.1"
    )
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(shared.js)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-generic" % "0.14.1",
      "io.circe" %%% "circe-parser" % "0.14.1"
    )
  )
  .jsConfigure(_.enablePlugins(ScalaJSWeb))
