package controllers

import play.api._
import play.api.mvc._

/**
 * Main application controller, provides status, health and other Application-related
 * endpoints.
 *
 * Created by marco on 9/7/14.
 */
object AppController extends Controller {

  def health = Action {
    Ok("OK")
  }

  def foo = Action {
    Forbidden("No can do, dude!")
  }
}
