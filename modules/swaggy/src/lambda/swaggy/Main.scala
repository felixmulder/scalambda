package lambda.swaggy

import cats.implicits._
import cats.data.NonEmptyList
import cats.effect.{IO, ExitCode, IOApp}

import fileutils._
import error._
import syntax._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      parsed   <- IO.fromEither(parseArgs(args))
      swagger  <- readSwagger(parsed.inputPath)
      models   <- IO(models(swagger, parsed.pkg ++ List("models")))
      newEnds  <- endpoints(swagger, parsed.pkg ++ List("endpoints")).orRaiseMultiple[IO]
      hands    <- IO(handlers(newEnds, parsed.pkg ++ List("handlers")))
      existing <- readExisting(parsed.outputDir, newEnds.toList)
      ends     <- endpoints.adaptExisting(existing).orRaiseMultiple[IO]
      _        <- writeScalaFiles(parsed.outputDir, models ++ hands)
      _        <- writeTrees(parsed.outputDir, ends)
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
