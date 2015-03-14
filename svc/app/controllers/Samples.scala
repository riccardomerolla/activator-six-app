package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.Logger

import apidoc.models.json._
import apidoc.models.sample.SampleImpl

object Samples extends Controller {
  private final val logger = Logger

  def getGuid(guid: String) = Action {
  	logger.info(s"GUID: $guid")
    val sampleObj = SampleImpl(guid, "SIX Team")
    Ok(Json.toJson(sampleObj))
  }
}
