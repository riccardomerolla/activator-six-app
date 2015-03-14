package apidoc.models {
  package object json {
    import play.api.libs.json._
    import play.api.libs.functional.syntax._

    implicit val jsonReadsUUID = __.read[String].map(java.util.UUID.fromString)

    implicit val jsonWritesUUID = new Writes[java.util.UUID] {
      def writes(x: java.util.UUID) = JsString(x.toString)
    }

    implicit val jsonReadsJodaDateTime = __.read[String].map { str =>
      import org.joda.time.format.ISODateTimeFormat.dateTimeParser
      dateTimeParser.parseDateTime(str)
    }

    implicit val jsonWritesJodaDateTime = new Writes[org.joda.time.DateTime] {
      def writes(x: org.joda.time.DateTime) = {
        import org.joda.time.format.ISODateTimeFormat.dateTime
        val str = dateTime.print(x)
        JsString(str)
      }
    }
  }

  import json._

  /**
   * A sample is a top level object.
   */
  trait Sample {
    /**
     * Internal unique identifier for this product.
     */
    def `guid`: java.lang.String
    
    /**
     * `name`
     */
    def `name`: java.lang.String
  }
  
  package sample {
    /**
     * A sample is a top level object.
     * @param `guid` Internal unique identifier for this product.
     * @param `name` `name`
     */
    case class SampleImpl(
      `guid`: java.lang.String,
      `name`: java.lang.String
    ) extends Sample
  }

  object Sample {
    def unapply(x: Sample) = {
      Some(x.`guid`,x.`name`)
    }
  
    implicit val reads: play.api.libs.json.Reads[Sample] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "guid").read[java.lang.String] and
         (__ \ "name").read[java.lang.String])(sample.SampleImpl.apply _)
      }
  
    implicit val writes: play.api.libs.json.Writes[Sample] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "guid").write[java.lang.String] and
         (__ \ "name").write[java.lang.String])(unlift(Sample.unapply))
      }
  }
}

package play.api.libs.apidoc.apidoc {
  import play.api.libs.ws._
  /**
   * A helper that provides access to some needed, but private
   * functionality of the play WS client library.
   */
  object WSHelper {
    /**
     * Allows users to perform patch requests using a WSRequestHolder.
     * Necessary in play 2.2.x, but needed for 2.3 +.
     */
    def patch(
      req: WSRequestHolder,
      data: play.api.libs.json.JsValue
    ): scala.concurrent.Future[WSResponse] = {
      req.patch(data)
    }
  }
}

package apidoc {
  class Client(apiUrl: String, apiToken: Option[String] = None) {
    import play.api.libs.ws._
    import play.api.Play.current
    import apidoc.models._
    import apidoc.models.json._

    private val logger = play.api.Logger("apidoc.client")

    logger.info(s"Initializing apidoc.client for url $apiUrl")

    private def requestHolder(resource: String) = {
      val url = apiUrl + resource
      val holder = WS.url(url)
      apiToken.map { token =>
        holder.withAuth(token, "", WSAuthScheme.BASIC)
      }.getOrElse {
        holder
      }
    }

    private def logRequest(method: String, req: WSRequestHolder)(implicit ec: scala.concurrent.ExecutionContext): WSRequestHolder = {
      val q = req.queryString.flatMap { case (name, values) =>
        values.map(name -> _).map { case (name, value) =>
          s"$name=$value"
        }
      }.mkString("&")
      val url = s"${req.url}?$q"
      apiToken.map { _ =>
        logger.info(s"curl -X $method -u '[REDACTED]:' $url")
      }.getOrElse {
        logger.info(s"curl -X $method $url")
      }
      req
    }

    private def processResponse(f: scala.concurrent.Future[WSResponse])(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[WSResponse] = {
      f.map { response =>
        lazy val body: String = scala.util.Try {
          play.api.libs.json.Json.prettyPrint(response.json)
        } getOrElse {
          response.body
        }
        logger.debug(s"${response.status} -> $body")
        response
      }
    }

    private def POST(path: String, data: play.api.libs.json.JsValue)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[WSResponse] = {
      processResponse(logRequest("POST", requestHolder(path)).post(data))
    }

    private def GET(path: String, q: Seq[(String, String)])(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[WSResponse] = {
      processResponse(logRequest("GET", requestHolder(path).withQueryString(q:_*)).get())
    }

    private def PUT(path: String, data: play.api.libs.json.JsValue)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[WSResponse] = {
      processResponse(logRequest("PUT", requestHolder(path)).put(data))
    }

    private def PATCH(path: String, data: play.api.libs.json.JsValue)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[WSResponse] = {
      processResponse(play.api.libs.apidoc.apidoc.WSHelper.patch(logRequest("PATCH", requestHolder(path)), data))
    }

    private def DELETE(path: String)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[WSResponse] = {
      processResponse(logRequest("DELETE", requestHolder(path)).delete())
    }

    object Samples {
      /**
       * 
       * @param `guid` `guid`
       * @return (200, Sample)
       */
      def `getByGuid`(
        `guid`: java.lang.String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Any] = {
        val queryBuilder = List.newBuilder[(String, String)]
        
        
        GET(s"/samples/${({x: java.lang.String =>
          val s = x
          java.net.URLEncoder.encode(s, "UTF-8")
        })(`guid`)}", queryBuilder.result).map {
          case r if r.status == 200 => r.status -> r.json.as[Sample]
          case r => r
        }
      }
    }
  }
}