// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

import com.alertavert.sentinel.errors.{SentinelException, AuthenticationError}
import controllers.AppController
import models.resources.UsersResource
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import play.Play

import scala.concurrent.Future

import play.api.libs.json._
import play.api.mvc._

import com.alertavert.sentinel.model.{Resource, Organization, User}
import com.alertavert.sentinel.security.Credentials
import com.alertavert.sentinel.security.{encode, hashStrings}

/**
 * JSON Serialization models
 *
 * Created by marco on 9/1/14.
 */
package object models {

  val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss zzz")

  implicit val credsWrites = new Writes[Credentials] {
    def writes(creds: Credentials) = Json.obj(
      "username" -> creds.username,
      "api_key" -> creds.apiKey
    )
  }

  implicit val userWrites = new Writes[User] {

    def writes(user: User) = Json.obj(
        "id" -> user.id.map(_ toString),
        "first_name" -> user.firstName,
        "last_name" -> user.lastName,
        "active" -> user.isActive,
        "last_seen" -> format.format(user.lastSeen),
        "credentials" -> Json.toJson(user.getCredentials),
        "created_when" -> format.format(user.createdAt),
        "created_by" -> user.createdBy.map(_.id.map(_ toString))
      )
  }

  /**
   * Deserializes the JSON for the [[User]] model, as returned by the API call.
   */
  implicit object UserReads extends Reads[User] {
    def reads(json: JsValue): JsResult[User] = {
      val id = (json \ "id").asOpt[ObjectId].orNull
      val fname = (json \ "first_name").asOpt[String].getOrElse("")
      val lname = (json \ "last_name").asOpt[String].getOrElse("")
      val uname = (json \ "credentials" \ "username").as[String]
      val pwd = (json \ "credentials" \ "password").asOpt[String].getOrElse("")
      val active = (json \ "active").asOpt[Boolean].getOrElse(false)

      // TODO: missing created_by and created_at fields

      val builder = (User.builder(fname, lname) withId id hasCreds Credentials(uname, pwd)
        setActive active)
      JsSuccess(builder build())
    }
  }

  implicit val oidReads = new Reads[ObjectId] {
    def reads(json: JsValue) = {
      val oid = json.asOpt[String] match {
        case None => null
        case Some(s) => if (ObjectId.isValid(s)) new ObjectId(s)
                        else throw new IllegalArgumentException(s"$s is not a valid ObjectId")
      }
      JsSuccess(oid)
    }
  }

  implicit val orgsWrites = new Writes[Organization] {
    def writes(org: Organization) = Json.obj(
      "id" -> org.id.map(_ toString),
      "name" -> org.name,
      "active" -> org.active
    )
  }

  implicit val orgReads = new Reads[Organization] {
    def reads(json: JsValue) = {
      val id = (json \ "id").asOpt[ObjectId].orNull
      val orgName = (json \ "name").as[String]
      val active = (json \ "active").asOpt[Boolean] match {
        case None => false
        case Some(b) => b
      }
      JsSuccess(Organization.builder(orgName) withId id setActive active build)
    }
  }

  implicit val orgsRoleWrites = new Writes[(Organization, String)] {
    def writes(orgRole: (Organization, String)) = Json.obj(
      "organization" -> orgRole._1.id.getOrElse(
        throw new IllegalArgumentException("Missing Org " + "ID")).toString,
      "role" -> orgRole._2
    )
  }

  implicit val resourceWrites = new Writes[Resource] {
    def writes(resource: Resource) = Json.obj(
      "id" -> resource.id.map(_ toString),
      "name" -> resource.name,
      "path" -> resource.path,
      "owner" -> resource.owner.id.map(_ toString),

      // TODO: created_by, created_when must be handled by a unified super-trait
      "created_by" -> resource.createdBy.map(_.id.map(_ toString)),
      "created_when" -> format.format(resource.createdAt)
    )
  }

  implicit val resourceReads = new Reads[Resource] {
    def reads(json: JsValue) = {
      val id = (json \ "id").as[ObjectId]
      val name = (json \ "name").as[String]
      val ownerId = (json \ "owner").as[ObjectId]
      // This is just a placeholder for the ID - the retrieval from the persistence layer cannot be
      // done here
      val owner = (User.builder("") withId ownerId hasCreds Credentials.createCredentials("", "")
        build())

      // TODO: created_by, created_when must be handled by a unified super-trait
      val res = new Resource(name, owner)
      res.setId(id)
      JsSuccess(res)
    }
  }
}

package object security {


  val logger = LoggerFactory.getLogger("security")

  // TODO: Understand what really goes on behind the curtain
  // Code based on: https://www.playframework.com/documentation/2.0.2/ScalaActionsComposition

  case class AuthenticatedRequest[A](user: User, request: Request[A])
    extends WrappedRequest(request)

  def Authenticated[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result) = {
    Action(p) { request =>
        try {
          val user = validateHash(request).getOrElse(throw new AuthenticationError(
            "Cannot authenticate request"))
          f(AuthenticatedRequest(user, request))
        } catch {
          case ex: AuthenticationError => Results.Unauthorized(ex.getLocalizedMessage)
          case ex: SentinelException => Results.Forbidden(ex.getLocalizedMessage)
          case ex: Exception => Results.BadRequest(ex.getLocalizedMessage)
        }
    }
  }

  // TODO: the returned errors disclose too much information; replace with generic errors in Prod
  def validateHash[A](request: Request[A]): Option[User] = {

    val date = request.headers.get("Date").getOrElse(
      throw new AuthenticationError("Missing `Date:` header"))
    // TODO: verify that the Date this request is signed with is not a replay attack
    logger.debug(s"Request sent at $date")

    // Extract the value pairs from the Auth header: username=foo;hash=xYzaa99==
    val auth = request.headers.get("Authorization").getOrElse(
      throw new AuthenticationError("Missing `Authorization:` header"))
    val values = parseAuthHeader(auth)

    // Retrieve the username, as we need it anyway, even if validation is disabled
    val username = values.getOrElse("username",
      throw new AuthenticationError("`username` key missing in Authorization header"))

    val maybeUser = UsersResource.getUserByUsername(username)
    if (AppController.configuration.shouldValidate) {
      val hash = values.getOrElse("hash",
          throw new AuthenticationError("`hash` key missing in Authorization header"))
      val user = maybeUser.getOrElse(
          throw new AuthenticationError(s"$username is not a valid username"))
      val apiKey = user.getCredentials.apiKey

      val computedHash = encode(hashStrings(List(
        apiKey,
        date,
        request.path,
        request.body.toString
      )))
      logger.debug(s"Computed hash: $computedHash")
      if (computedHash == hash) Some(user) else None
    } else {
      logger.warn("Bypassing API signature check")
      Some(maybeUser.getOrElse(madeUpUser(username)))
    }
  }

  /**
   * Creates a bogus user, so that all authentication is bypassed.
   * Only used for testing and development; DO NOT use in production
   *
   * @param username the newly created bogus user will have this username
   */
  private def madeUpUser(username: String) = {
    User.builder(username) hasCreds Credentials(username, "secret") withId new ObjectId build()
  }

  private def parseAuthHeader(authHeader: String) = {
    val pairs = scala.collection.mutable.Map[String, String]()
    // Note the `2` limit on the split, to avoid problems with == padding for base-64 encoded strings
    authHeader.split(";").map(t => t.split("=", 2)).foreach(pair =>
      pairs += (pair(0) -> pair(1)))
    pairs
  }

  // Overloaded method to use the default body parser
  import play.api.mvc.BodyParsers._
  def Authenticated(f: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent]  = {
    Authenticated(parse.anyContent)(f)
  }
}

