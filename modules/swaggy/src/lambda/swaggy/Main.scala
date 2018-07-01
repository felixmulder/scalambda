package lambda.swaggy

import cats.implicits._
import cats.effect.{IO, ExitCode, IOApp}

import fileutils._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      parsed  <- IO.fromEither(parseArgs(args))
      swagger <- readSwagger(parsed.inputPath)
      defs    <- IO(definitions(swagger, parsed.pkg :+ "model"))
      paths   <- IO {
                  paths(swagger, parsed.pkg :+ "paths")
                    .valueOr(e => throw new Exception(e.toList.mkString("\n")))
                 }
      _       <- paths.map(p => IO(println(p))).sequence
      _       <- writeScalaFiles(parsed.outputDir, defs)
    } yield ExitCode.Success

  private final case class ParsedArgs(
    inputPath: String,
    outputDir: String,
    pkg: List[String]
  )

  private def parseArgs(args: List[String]): Either[Exception, ParsedArgs] =
    args match {
      case inputFile :: outputDir :: pkg :: _ =>
        ParsedArgs(inputFile, outputDir, pkg.split('.').toList).asRight
      case otherwise =>
        new Exception(s"Needs three args, got: $otherwise").asLeft
    }
}
