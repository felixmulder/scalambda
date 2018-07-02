package lambda.swaggy

import cats.implicits._
import cats.data.NonEmptyList
import cats.effect.{IO, ExitCode, IOApp}

import fileutils._
import error._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      parsed  <- IO.fromEither(parseArgs(args))
      swagger <- readSwagger(parsed.inputPath)
      defs    <- IO(definitions(swagger, parsed.pkg ++ List("models")))
      ends    <- endpoints(swagger, parsed.pkg ++ List("endpoints"))
                   .fold(e => IO.raiseError(MultipleErrors(e)), IO.pure(_))
      hands   <- IO(handlers(ends, parsed.pkg ++ List("handlers")))
      _       <- writeScalaFiles(parsed.outputDir, defs ++ ends ++ hands)
    } yield ExitCode.Success

  private final case class ParsedArgs(
    inputPath: String,
    outputDir: String,
    pkg: NonEmptyList[String]
  )

  private def parseArgs(args: List[String]): Either[Exception, ParsedArgs] =
    args match {
      case inputFile :: outputDir :: pkg :: _ if pkg.nonEmpty =>
        ParsedArgs(
          inputFile,
          outputDir,
          NonEmptyList.fromListUnsafe(pkg.split('.').toList)
        ).asRight
      case otherwise =>
        new Exception(s"Needs three args, got: $otherwise").asLeft
    }
}
