// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package controllers

import com.alertavert.sentinel.errors.{NotFoundException, AuthenticationError, DbException}
import com.alertavert.sentinel.persistence.DataAccessManager
import models._
import models.resources._
import org.bson.types.ObjectId
import play.Logger
import play.api.libs.json._
import play.api.mvc._
import security.Authenticated


trait ApiController extends Controller {
  this: Controller =>

    Logger.info("{} Controller started", this.getClass.getSimpleName)

    if (!DataAccessManager.isReady) {
      DataAccessManager.init(AppController.configuration.dbUri)
      if (! DataAccessManager.isReady) {
        Logger.error("Could not start the Data Access Manager, it is possible that the DB server" +
          " may be down")
        throw new DbException("Could not connect to the DB server (" +
          AppController.configuration.dbUri + ")")
      }
    }

    // TODO(marco): this must be replaced with a suitable home page
    def index = Action {
      Ok(views.html.index("Sentinel - REST API-driven User Management made easy"))
    }

    def users = Authenticated { implicit request =>
      val resJson = Json.toJson(UsersResource.getAllUsers)
      Ok(resJson)
    }

    def userById(id: String) = Authenticated { implicit request =>
      if (!ObjectId.isValid(id)) BadRequest(s"$id not a valid user ID")
      else {
        UsersResource.getUserById(new ObjectId(id)) match {
          // TODO: discover why raising here bypasses Global.onError()
          case None => NotFound(s"Could not retrieve user $id")
          case Some(user) => Ok(Json.toJson(user))
        }
      }
    }

    def login = Action(BodyParsers.parse.json) {
      request =>
        val req = request.body
        val username = req \ "username"
        val password = req \ "password"
        val user = UsersResource.authUser(username.as[String], password.as[String])
        Ok(Json.toJson(user))
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

  /**
   * GET /org
   *
   * Retrieves all organizations in the system that the requesting user has View access to
   *
   * @return the JSON response with an array of Organization objects
   */
    def orgs = Authenticated { implicit request =>
      Ok(Json.toJson(OrgsResource.getAllOrgs))
    }

  /**
   * GET /org/`id`
   *
   * Retrieves the one org identified by `id`
   *
   * @param id the org's ID
   * @return the JSON representation of the organization, or 404 if it doesn't exist
   */
    def orgById(id: String) = Authenticated { implicit request =>
    if (!ObjectId.isValid(id)) BadRequest(s"$id not a valid ID")
    OrgsResource.getOrgById(new ObjectId(id)) match {
        case None => NotFound
        case Some(org) => Ok(Json.toJson(org))
      }
    }

  /**
   * POST /org
   *
   * Takes a JSON representation for the Org and creates a new one
   *
   * @return the full JSON representation for the Org, including the ID of the newly created one
   */
    def createOrg = Authenticated(BodyParsers.parse.json) { implicit request =>
      val newOrg = OrgsResource.createOrg(request.body)
      // TODO: Add Location header with URI of created resource
      Created(Json.toJson(newOrg))
    }

  /**
   * PUT /org/`id`
   *
   * Modifies the org identified by `id` with the passed in JSON body
   *
   * @param id the unique ID for the org
   * @return the modified organization, or 404 if it doesn't exist
   */
    def modifyOrg(id: String) = Authenticated(BodyParsers.parse.json) { implicit request =>
      val org = OrgsResource.updateOrg(id, request.body)
      Ok(Json.toJson(org))
    }

  /**
   * GET /org/`id`/user
   *
   * @param id the user ID
   * @return a list of all Users associated with the given organization, and their respective roles
   */
  def getUsersOrgs(id: String) = Authenticated {
    implicit request =>
      if (!ObjectId.isValid(id)) BadRequest(s"$id not a valid user ID")
      else {
        val orgs = UsersResource.getOrgsForUser(new ObjectId(id))
        // TODO: add the "link" to each organization (of the form /org/{id}
        Ok(Json.toJson(Map("organizations" -> orgs)))
      }
  }

  def getAllUsersForOrg(id: String) = play.mvc.Results.TODO

  /**
   * POST /user/`id`/org/`oid`
   * <pre>
   *   {
   *     "role": "admin"
   *   }
   * </pre>
   *
   * Associated a user with an organization, and assigns her the given `role`.
   *
   * @param uid the unique ID for the user (MUST exist)
   * @param oid the unique ID for the Organization (MUST exist)
   * @return 201 CREATED if successful
   */
  def assocUserOrg(uid: String, oid: String) = Authenticated(BodyParsers.parse.json) {
    implicit request =>
      val errors = List(uid, oid).filter(!ObjectId.isValid(_))
      if (errors.nonEmpty) BadRequest(s"$errors not a valid ID")
      else {
        UsersResource.associateWithOrganization(new ObjectId(uid), new ObjectId(oid), request.body)
        Created
      }
  }

  def removeAssocUserOrg(uid: String, oid: String) = play.mvc.Results.TODO
}

object ApiController extends Controller with ApiController
