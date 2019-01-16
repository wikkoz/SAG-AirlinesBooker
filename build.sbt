import scala.sys.process.Process

lazy val akkaHttpVersion = "10.1.2"
lazy val akkaVersion    = "2.5.11"

val ng = inputKey[Int]("The angular-cli command.")
val ngBuild = taskKey[Int]("cd; ng build -prod -aot.")

lazy val root = (project in file(".")).
  settings(
    name            := "Airlines",
    version         := "0.1",
    licenses        := Seq("Unlicense" -> new URL("http://unlicense.org/")),
    organization    := "net.creasource",
    scalaVersion    := "2.12.4",
    scalacOptions   := Seq("-unchecked", "-deprecation", "-feature"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"           % akkaVersion,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.8",
      "ch.qos.logback"    %  "logback-classic"      % "1.2.3",
      "com.github.scopt" %% "scopt" % "3.7.0",
      
      "io.gatling" % "gatling-core" % "3.0.2",
      "io.gatling" % "gatling-http" % "3.0.2",
      "io.gatling" % "gatling-app" % "3.0.2",
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.0.2",

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.4"         % Test
    ),
    unmanagedResourceDirectories in Compile += baseDirectory.value / "config",
    unmanagedResourceDirectories in Compile += baseDirectory.value / "web" / "resources",
    ng := {
      import complete.DefaultParsers._
      val args = spaceDelimited("<arg>").parsed.mkString(" ")
      val command = {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
          s"powershell -Command ng $args"
        } else {
          s"cd web; ng $args"
        }
      }
      Process(command, new File(".").getAbsoluteFile).!
    },
    ngBuild := {
      val log = streams.value.log
      log.info("Building Angular application...")
      val command = {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
          s"powershell -Command ng build -prod -aot"
        } else {
          s"cd web; ng build -prod -aot"
        }
      }
      val exitCode = Process(command, new File(".").getAbsoluteFile).!
      if (exitCode != 0)
        throw new Exception("Build failed!")
      exitCode
    },
    stage := stage.dependsOn(ngBuild).value
  ).enablePlugins(JavaAppPackaging)