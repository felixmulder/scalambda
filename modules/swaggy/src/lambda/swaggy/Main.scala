package lambda.swaggy

import cats.implicits._
import cats.effect.{IO, ExitCode, IOApp}

import fileutils._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      parsed  <- IO.fromEither(parseArgs(args))
      swagger <- readSwagger(parsed.inputPath)
      defs    <- IO(definitions(swagger, parsed.domainPackage))
      _       <- IO(endpoints(swagger))
      _       <- writeScalaFiles(parsed.outputDir, defs)
    } yield ExitCode.Success

  private final case class ParsedArgs(
    inputPath: String,
    outputDir: String,
    domainPackage: List[String]
  )
  private def parseArgs(args: List[String]): Either[Exception, ParsedArgs] =
    args match {
      case inputFile :: outputDir :: pkg :: _ =>
        ParsedArgs(inputFile, outputDir, pkg.split('.').toList).asRight
      case otherwise =>
        new Exception(s"Needs three args, got: $otherwise").asLeft
    }
}
