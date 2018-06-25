package lambda.swaggy

import java.nio.file.Paths
import java.nio.file.Files
import java.io.IOException
import io.swagger.models.{Swagger, Model}
import io.swagger.parser.SwaggerParser

import cats.implicits._
import cats.effect.IO
import scala.meta._

object fileutils {
  final case class ParsedArgs(
    inputPath: String,
    outputDir: String,
    domainPackage: List[String]
  )

  sealed trait ScalaFile {
    def pkg: List[String]
    def title: String
    def contents: Array[Byte]
  }

  final case class DomainType(
    title: String,
    pkg: List[String],
    imports: List[Import],
    cls: Defn.Class, obj: Defn.Object
  ) extends ScalaFile {
    def contents =
      q"""
        package ${Term.Name(pkg.mkString("."))} {
          ..${imports :+ cls :+ obj}
        }
      """.syntax.getBytes
  }

  def writeScalaFile(outputDir: String, file: ScalaFile): IO[Unit] =
    writeFile((outputDir :: file.pkg).mkString("/"), file.title, file.contents)

  def writeFile(outputDir: String, title: String, contents: Array[Byte]): IO[Unit] =
    IO.fromEither(
      Either.catchOnly[IOException] {
        val path = Paths.get(outputDir, title)
        Files.createDirectories(path.getParent());
        Files.write(path, contents)
      }
    )

  def readSwagger(path: String): IO[Swagger] =
    IO(new SwaggerParser().read(path))
}
