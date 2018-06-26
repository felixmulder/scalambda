package lambda.swaggy

import scala.collection.JavaConverters._
import io.swagger.models.Swagger

object paths {

  final case class Endpoint(name: String, path: String)

  def apply(swagger: Swagger, pkg: List[String]): List[Endpoint] =
    swagger.getPaths.asScala.map { case (pathString, path) =>
      Endpoint(
        endpointName(pathString),
        pathString,
      )
    }
    .toList

  /** Create an endpoint name from the given path string
   *
   *  Will currently remove path parameters in order to create a terser name
   */
  private[swaggy] def endpointName(path: String): String =
    path
      .split("/")
      .filter(_.nonEmpty)
      .filterNot(_.head == '{')
      .map(_.capitalize).mkString("", "", ".scala")
}
