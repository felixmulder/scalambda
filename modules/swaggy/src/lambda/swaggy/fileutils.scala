package lambda.swaggy

import java.nio.file.Paths
import java.nio.file.Files
import java.io.IOException
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser

import cats.implicits._
import cats.effect.IO

object fileutils {
  sealed trait ScalaFile {
    def title: String
    def contents: Array[Byte]
  }

  final case class DomainType(title: String, obj: SCaseClass, cc: SObject)
    extends ScalaFile {

    def contents = ???
  }

  case class SCaseClass()
  case class SObject()

  def writeScalaFile(outputDir: String, file: ScalaFile): IO[Unit] =
    writeFile(outputDir, file.title, file.contents)

  def writeFile(outputDir: String, title: String, contents: Array[Byte]): IO[Unit] =
    IO.fromEither(
      Either.catchOnly[IOException] {
        val path = Paths.get(outputDir, title)
        Files.write(path, contents)
      }
    )

  def readSwagger(path: String): IO[Swagger] =
    IO(new SwaggerParser().read(path))
}
