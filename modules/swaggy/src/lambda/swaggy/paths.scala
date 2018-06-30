package lambda.swaggy

import io.swagger.models._
import io.swagger.models.parameters.BodyParameter
import scala.collection.JavaConverters._
import scala.meta._

import converters._

object paths {

  type Method = HttpMethod

  final case class Endpoint(name: String, path: String, operations: Map[Method, Handler])
  final case class Handler(name: String, body: Option[Type.Name], queryParams: List[Type.Name])

  def apply(swagger: Swagger, pkg: List[String]): List[Endpoint] =
    swagger.getPaths.asScala.map { case (pathString, path) =>
      Endpoint(
        endpointName(pathString),
        pathString,
        getOps(path),
      )
    }
    .toList

  private[swaggy] def getOps(path: Path): Map[Method, Handler] =
    path
      .getOperationMap.asScala
      .map { case (method, op) =>
        val handler = Handler(
          op.getOperationId,
          bodyParam(op),
          Nil
        )

        (method, handler)
      }
      .toMap

  private[swaggy] def bodyParam(op: Operation): Option[Type.Name] =
    op.getParameters.asScala.collectFirst {
      case p: BodyParameter =>
        p.getSchema match {
          case r: RefModel =>
            Type.Name(r.getReference.split("/").last)

          case r: ArrayModel =>
            Type.Name(s"Array[${propertyToType(r.getItems)}]")
        }
    }

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
