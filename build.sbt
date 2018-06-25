lazy val `lambda-core` =
  project
    .in(file("./modules/core"))
    .settings(
      projectLayout ++
      compilerOptions ++
      compilerPlugins ++
      Nil
    )
    .settings(
      libraryDependencies ++= {
        val catsEffectVersion    = "1.0.0-RC2"
        val circeVersion         = "0.10.0-M1"
        val minitestVersion      = "2.1.1"
        val awsLambdaCoreVersion = "1.2.0"

        Seq(
          // Project dependencies:
          "org.typelevel" %% "cats-effect"          % catsEffectVersion,
          "io.circe"      %% "circe-jawn"           % circeVersion,
          "io.circe"      %% "circe-parser"         % circeVersion,
          "io.circe"      %% "circe-generic"        % circeVersion,
          "io.circe"      %% "circe-generic-extras" % circeVersion,
          "io.circe"      %% "circe-java8"          % circeVersion,
          "com.amazonaws" % "aws-lambda-java-core"  % awsLambdaCoreVersion,


          // Test dependencies:
          "io.monix" %% "minitest" % minitestVersion % "test",
        )
      },
      // Enable minitest:
      testFrameworks += new TestFramework("minitest.runner.Framework"),
    )

lazy val `lambda-swaggy` =
  project
    .in(file("./modules/swaggy"))
    .settings(
      projectLayout ++
      compilerOptions ++
      compilerPlugins ++
      Nil
    )
    .settings(
      libraryDependencies ++= {
        val minitestVersion      = "2.1.1"
        val swaggerCoreVersion   = "2.0.2"

        Seq(
          // Project dependencies:
          "io.swagger.core.v3" % "swagger-core" % swaggerCoreVersion,

          // Test dependencies:
          "io.monix" %% "minitest" % minitestVersion % "test",
        )
      },
      // Enable minitest:
      testFrameworks += new TestFramework("minitest.runner.Framework"),
    )

lazy val projectLayout = Seq(
  scalaSource in Compile       := baseDirectory.value / "src",
  scalaSource in Test          := baseDirectory.value / "test" / "src",
  resourceDirectory in Compile := baseDirectory.value / "resources",
  resourceDirectory in Test    := baseDirectory.value / "test" / "resources",
)

lazy val compilerOptions = Seq(
  scalacOptions ++= Seq(
    "-Ypartial-unification",
    "-Xfatal-warnings",
    "-feature",
    "-deprecation",
    "-language:higherKinds",
    "-language:implicitConversions",
    ),
    scalacOptions in (Test, compile) ~= (_ filterNot (_ == "-Ywarn-unused")),
)

lazy val compilerPlugins = Seq(
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
  addCompilerPlugin(
    "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
  ),
)
