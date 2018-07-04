package lambda.swaggy

import java.nio.file.Paths
import java.nio.file.Files
import java.io.IOException
import io.swagger.models.{Swagger, Model}
import io.swagger.parser.SwaggerParser

import cats.implicits._
import cats.effect.IO
import scala.meta._
import org.scalafmt.Scalafmt.format

object fileutils {

  def writeScalaFile(outputDir: String, file: ScalaFile): IO[Unit] =
    IO.fromEither(format(ScalaFile.toTree(file).syntax).toEither)
      .map(_.getBytes)
      .flatMap { bytes =>
        writeFile(
          (outputDir :: file.pkg).toList.mkString("/"),
          file.title + ".scala",
          bytes
        )
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

  def writeTrees(outputDir: String, filePathAndTree: List[(String, Tree)]): IO[Unit] =
    filePathAndTree
      .map { case (filePath, tree) =>
        writeFile(outputDir, filePath, tree.syntax.getBytes)
      }
      .sequence_

  def pathFrom(dir: String, f: ScalaFile): String =
    dir + f.pkg.toList.mkString("/", "/", s"/${f.title}.scala")

  def readFile(path: String): IO[Option[String]] =
    IO(new String(Files.readAllBytes(Paths.get(path)))).attempt.map(_.toOption)

  def readExisting[E <: ScalaFile](dir: String, f: E): IO[(E, Option[String])] =
    readFile(pathFrom(dir, f)).map((f, _))

  def readExisting[E <: ScalaFile](dir: String, fs: List[E]): IO[List[(E, Option[String])]] =
    fs.map(readExisting(dir, _)).sequence

  def readSwagger(path: String): IO[Swagger] =
    IO(new SwaggerParser().read(path))
}
