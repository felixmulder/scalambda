package lambda.swaggy

import io.swagger.parser.SwaggerParser
import io.swagger.models._
import io.swagger.models.parameters._
import io.swagger.models.HttpMethod._

import cats.implicits._
import cats.effect.IO

import fileutils._

object Main {
  def main(args: List[String]): IO[Unit] =
    for {
      parsed  <- IO.fromEither(parseArgs(args))
      swagger <- readSwagger(parsed.inputPath)
      defs    <- IO(definitions(swagger, parsed.domainPackage))
      _       <- defs.map(writeScalaFile(parsed.outputDir, _)).sequence
    } yield ()

  def parseArgs(args: List[String]): Either[Exception, ParsedArgs] =
    args match {
      case inputFile :: outputDir :: pkg :: _ =>
        ParsedArgs(inputFile, outputDir, pkg.split('.').toList).asRight
      case otherwise =>
        new Exception(s"Needs three args, got: $otherwise").asLeft
    }


  def printModel(m: Model): Unit = m match {
    case m: RefModel =>
      println(m.getProperties)
      println(m.getSimpleRef)
      println(m.getExample)
  }

  def main(args: Array[String]): Unit =
    main(args.toList).unsafeRunSync()
}
