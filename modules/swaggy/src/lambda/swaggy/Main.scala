package lambda.swaggy

import io.swagger.parser.SwaggerParser
import io.swagger.models._
import io.swagger.models.parameters._
import io.swagger.models.HttpMethod._

import cats.implicits._
import cats.effect.IO
import scala.collection.JavaConverters._

  import fileutils._

object Main {
  def main(args: Array[String]): Unit = {
    main(args.toList).unsafeRunSync()
    //val swagger = new SwaggerParser().read(args.head)

    //val path = "/pet"
    //val op = swagger.getPaths.get(path).getOperationMap.get(POST)
    //println(op.getConsumes.asScala)

    //println(swagger.getDefinitions.asScala)

    //op.getParameters.asScala.foreach {
    //  case p: BodyParameter => printModel(p.getSchema)
    //  case p => println(p)
    //}
  }

  def main(args: List[String]): IO[Unit] =
    for {
      swagger <- readSwagger(args.head)
      defs <- IO(definitions(swagger))
    } yield ()


  def definitions(swagger: Swagger): List[DomainType] = Nil


  def printModel(m: Model): Unit = m match {
    case m: RefModel =>
      println(m.getProperties.asScala)
      println(m.getSimpleRef)
      println(m.getExample)
  }
}
