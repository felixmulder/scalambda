package scalambda.lambda

import minitest._
import cats.implicits._


object TestRequest extends SimpleTestSuite {
  val json =
    """{
      |  "resource": "/{proxy+}",
      |  "path": "/proxy",
      |  "httpMethod": "POST",
      |  "headers": {
      |    "accept": "*/*",
      |    "content-type": "application/json",
      |    "day": "YoloDay",
      |    "Host": "8majttbgw6.execute-api.eu-west-1.amazonaws.com",
      |    "User-Agent": "curl/7.54.0",
      |    "X-Amzn-Trace-Id": "Root=1-5b2fdba0-756fb20c823b70022e45fb42",
      |    "X-Forwarded-For": "80.217.157.88",
      |    "X-Forwarded-Port": "443",
      |    "X-Forwarded-Proto": "https"
      |  },
      |  "queryStringParameters": {
      |    "wat": "fak",
      |    "who": "you"
      |  },
      |  "pathParameters": {
      |    "proxy": "proxy"
      |  },
      |  "stageVariables": null,
      |  "requestContext": {
      |    "resourceId": "yjw8ml",
      |    "resourcePath": "/{proxy+}",
      |    "httpMethod": "POST",
      |    "extendedRequestId": "I_9BHH-qjoEF11w=",
      |    "requestTime": "24/Jun/2018:17:57:52 +0000",
      |    "path": "/test/proxy",
      |    "accountId": "418005409410",
      |    "protocol": "HTTP/1.1",
      |    "stage": "test",
      |    "requestTimeEpoch": 1529863072702,
      |    "requestId": "1d89ede6-77d8-11e8-8008-bf86d5d7e198",
      |    "identity": {
      |      "cognitoIdentityPoolId": null,
      |      "accountId": null,
      |      "cognitoIdentityId": null,
      |      "caller": null,
      |      "sourceIp": "80.217.157.88",
      |      "accessKey": null,
      |      "cognitoAuthenticationType": null,
      |      "cognitoAuthenticationProvider": null,
      |      "userArn": null,
      |      "userAgent": "curl/7.54.0",
      |      "user": null
      |    },
      |    "apiId": "8majttbgw6"
      |  },
      |  "body": "{\"wat\": \"\\nsuckit\"}",
      |  "isBase64Encoded": false
      |}""".stripMargin

  test("must be able to deserialize") {
    import io.circe.parser.decode

    decode[Request[Map[String, String]]](json).as(()).valueOr(throw _)
  }
}
