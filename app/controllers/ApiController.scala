// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package controllers

import com.alertavert.sentinel.errors.{DbException, AuthenticationError}
import com.alertavert.sentinel.persistence.DataAccessManager
import models._
import models.resources._
import org.slf4j.{Logger, LoggerFactory}
import play.Play
import play.api.libs.json._
import play.api.mvc._
import security.Authenticated


object ApiController extends Controller {

  val logger = LoggerFactory.getLogger("API Controller")
  logger.info("API Controller started")


  DataAccessManager.init(AppController.configuration.dbUri)
  if (!DataAccessManager.isReady) {
    logger.error("Could not start the Data Access Manager, it is possible that the DB server" +
      " may be down")
    throw new DbException("Could not connect to the DB server (" +
      AppController.configuration.dbUri + ")")
  }

  // TODO(marco): this must be replaced with a suitable home page
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def users = Authenticated { implicit request =>
    // TODO: authorize user request.user to access all users' data
    val resJson = Json.toJson(UsersResource.getAllUsers)
    Ok(resJson)
  }

  def userById(id: String) = Authenticated { implicit request =>
    // TODO: authorize request.user to access user.id details
    UsersResource.getUserById(id) match {
      case None => NotFound
      case Some(user) => Ok(Json.toJson(user))
    }
  }

  def login = Action(BodyParsers.parse.json) {
    request =>
      val req = request.body
      val username = req \ "username"
      val password = req \ "password"
      try {
        val user = UsersResource.authUser(username.as[String], password.as[String])
        Ok(Json.toJson(user))
      } catch {
        case ex: AuthenticationError => Unauthorized(Json.obj("error" -> ex.getLocalizedMessage))
      }
  }

  /**
   * POST /user/{id}
   *
   * @return
   */
  def createUser = Authenticated(BodyParsers.parse.json) { implicit request =>
    val user = UsersResource.createUser(request.body)
    // TODO: add Location header with URI of created resource
    Created(Json.toJson(user))
  }

  /**
   * PUT /user/{id}
   *
   * Modify the user data with the incoming request's JSON body
   *
   * @param id the ID of the user to update
   * @return
   */
  def modifyUser(id: String) = TODO

  def orgs = Authenticated { implicit request =>
    Ok(Json.toJson(OrgsResource.getAllOrgs))
  }

  def orgById(id: String) = Authenticated { implicit request =>
    // TODO: authorize request.user to access org.id details
    OrgsResource.getOrgById(id) match {
      case None => NotFound
      case Some(org) => Ok(Json.toJson(org))
    }
  }

  def createOrg = Authenticated(BodyParsers.parse.json) { implicit request =>
    val newOrg = OrgsResource.createOrg(request.body)
    // TODO: Add Location header with URI of created resource
    Created(Json.toJson(newOrg))
  }

  def modifyOrg(id: String) = Authenticated(BodyParsers.parse.json) { implicit request =>
    val org = OrgsResource.updateOrg(id, request.body)
    Ok(Json.toJson(org))
  }
}
