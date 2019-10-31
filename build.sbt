name := "puppeteer"

val scalaV = "2.12.10"

val projectName = "puppeteer"

val projectVersion = "0.1"

def commonSettings = Seq(
  version := projectVersion,
  scalaVersion := scalaV,
  scalacOptions ++= Seq(
    //"-deprecation",
    "-feature"
  ),
  javacOptions ++= Seq("-encoding", "UTF-8")
)

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}


lazy val shared =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(commonSettings: _*)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

val clientMain = "org.seekloud.puppeteer.client.Boot"
resolvers in ThisBuild ++= Seq(
  "Spring Plugins Repository" at "https://repo.spring.io/plugins-release/"
)
lazy val client = (project in file("client")).enablePlugins(PackPlugin)
  .settings(commonSettings: _*)
  .settings(
    mainClass in reStart := Some(clientMain),
    javaOptions in reStart += "-Xmx3g"
  )
  .settings(name := "client")
  .settings(
    //pack
    // If you need to specify main classes manually, use packSettings and packMain
    //packSettings,
    // [Optional] Creating `hello` command that calls org.mydomain.Hello#main(Array[String])
    packMain := Map("client" -> clientMain),
    packJvmOpts := Map("client" -> Seq("-Xmx512m", "-Xms256m", "-XX:+HeapDumpOnOutOfMemoryError")),
    packExtraClasspath := Map("client" -> Seq("."))
  )
  .settings(
    libraryDependencies ++= Dependencies.backendDependencies,
    libraryDependencies ++= Dependencies.bytedecoLibs,
    libraryDependencies ++= Dependencies.jme3Libs
  )
  .dependsOn(sharedJvm)