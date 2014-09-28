// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package controllers

import play.Play
import play.api._
import play.api.mvc._

/**
 * Main application controller, provides status, health and other Application-related
 * endpoints.
 *
 * It also centralizes the management of all application-wide configuration settings,
 * abstracting away from the actual use of a configuration file (`/conf/application.conf`) or
 * system environment variables.
 *
 * Created by marco on 9/7/14.
 */
object AppController extends Controller {

  def health = Action {
    Ok("OK")
  }

  object configuration {

    val SIGN_VALIDATE_KEY = "application.signature.validate"
    val DATE_VALIDATE_THRESHOLD_SEC = "application.signature.threshold_sec"

    private val shouldValidateSignature = Play.application.configuration.getBoolean(SIGN_VALIDATE_KEY)
    private val dateValidationThresholdSec = Play.application.configuration.
      getInt(DATE_VALIDATE_THRESHOLD_SEC)

    def shouldValidate = shouldValidateSignature

    def validationThresholdSec = dateValidationThresholdSec

    def dbUri = Play.application.configuration.getString("db_uri")
  }
}
