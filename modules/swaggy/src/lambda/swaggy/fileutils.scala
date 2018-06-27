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

  def writeScalaFile(outputDir: String, file: ScalaFile): IO[Unit] =
    IO.fromEither(file.contents).flatMap { contents =>
      writeFile((outputDir :: file.pkg).mkString("/"), file.title, contents)
    }

  def writeScalaFiles(outputDir: String, files: List[ScalaFile]): IO[Unit] =
    files.map(writeScalaFile(outputDir, _)).sequence_

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
