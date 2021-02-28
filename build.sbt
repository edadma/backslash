lazy val backslash = crossProject(JSPlatform, JVMPlatform, NativePlatform).in(file(".")).
  settings(
    name := "backslash",
    version := "0.4.24",
    scalaVersion := "2.13.5",
    scalacOptions ++=
      Seq(
        "-deprecation", "-feature", "-unchecked",
        "-language:postfixOps", "-language:implicitConversions", "-language:existentials", "-language:dynamics",
        "-Xasync"
      ),
    organization := "xyz.hyperreal",
    mainClass := Some("xyz.hyperreal.backslash.Main"),
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.5" % "test",
    libraryDependencies ++=
      Seq(
        "xyz.hyperreal" %%% "json" % "0.8.3",
        "com.github.scopt" %%% "scopt" % "4.0.0",
        "xyz.hyperreal" %%% "hsl" % "1.0.0",
        "xyz.hyperreal" %%% "char-reader" % "0.1.10",
        "xyz.hyperreal" %%% "datetime" % "0.1.4"
      ),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    licenses += "ISC" -> url("https://opensource.org/licenses/ISC")
  ).
  jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided",
  ).
  nativeSettings(
    nativeLinkStubs := true
  ).
  jsSettings(
//    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.2.0",
//    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.2.0",
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
//    Test / scalaJSUseMainModuleInitializer := true,
//    Test / scalaJSUseTestModuleInitializer := false,
    Test / scalaJSUseMainModuleInitializer := false,
    Test / scalaJSUseTestModuleInitializer := true,
    scalaJSUseMainModuleInitializer := true,
  )
