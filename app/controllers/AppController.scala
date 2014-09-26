// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

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

}
