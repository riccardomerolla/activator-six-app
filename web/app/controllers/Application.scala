package controllers

import play.api.mvc._
import play.api.Logger
import apidoc.models.json._
import apidoc.models.Sample

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {
  private final val logger = Logger

  val serverUrl = "http://localhost:9000"

  val client = new apidoc.Client(serverUrl)

  def index = Action.async {
    for {
      sample <- client.Samples.getByGuid("abc")
    } yield {
      logger.info(s"sample: $sample")
      sample match {
        case (code, sample: Sample) =>
          Ok(views.html.index(s"Hello ${sample.name}"))

        case o: Result =>
          logger.info(s"result: $o")
          Ok(views.html.index(o.body.toString))
      }
    }
  }
}
