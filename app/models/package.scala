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
          logger.debug("Authenticating request")
          val user = validateApiKey(request) match {
            case Some(u) => u
            case None => logger.error(s"Cannot authenticate request for Authorization: ${
                                        request.headers.get("Authorization").getOrElse("empty")}")
              throw new AuthenticationError("Cannot authenticate request")
          }
          f(AuthenticatedRequest(user, request))
        } catch {
          case ex: AuthenticationError => Results.Unauthorized(ex.getLocalizedMessage)
          case ex: SentinelException => Results.Forbidden(ex.getLocalizedMessage)
          case ex: Exception => Results.BadRequest(ex.getLocalizedMessage)
        }
    }
  }

  /**
   * Authenticates the request, by comparing the passed in API key with the one stored in the database for the user.
   *
   * The `user` is validated against the database and its API key retrieved: the value is compared lexicographically
   * against the one passed in the `Authorization` header:
   *
   * <pre>Authorization: username=auser;api-key=abcde....00987</pre>
   *
   * If the username does not exist, or the API key does not match, or either value is missing, an
   * [[AuthenticationError]] is thrown.
   *
   * This check can be disabled by setting the `application.signature.validate` key to `false` in the
   * `conf/application.conf` file (this is <strong>strongly discouraged</strong> in production.
   *
   * @param request the request to validate; its `Authorization` header will be used only.
   * @return the actual [[User]] objects that maps to the `username` passed in to the `Authorization` header, if it
   *         exists and the keys match.
   *
   * @throws AuthenticationError if any of the above is missing or the keys don't match.
   */
  // TODO: the returned errors disclose too much information; replace with generic errors in Prod
  def validateApiKey[A](request: Request[A]): Option[User] = {

    // Extract the value pairs from the Auth header: username=foo;hash=xYzaa99==
    val auth = request.headers.get("Authorization").getOrElse(
      throw new AuthenticationError("Missing `Authorization:` header"))
    val values = parseAuthHeader(auth)

    // Retrieve the username, as we need it anyway, even if validation is disabled
    val username = values.getOrElse("username",
      throw new AuthenticationError("`username` key missing in Authorization header"))

    // TODO(marco): cache valid usernames in an in-memory store (eg, Redis).
    val maybeUser = UsersResource.getUserByUsername(username)
    if (AppController.configuration.shouldValidate) {
      val apiKeySent = values.getOrElse("api-key",
          throw new AuthenticationError("API key missing in Authorization header (`api-key`)"))
      val user = maybeUser.getOrElse(
          throw new AuthenticationError(s"$username is not a valid username"))

      // TODO(marco): cache the API key value in a memory store (eg, using Redis).
      val apiKey = user.getCredentials.apiKey

      if (apiKey == apiKeySent) Some(user) else throw new AuthenticationError("API Key does not match user key")
    } else {
      logger.warn("Bypassing API Key check")
      // TODO(marco): remove the `madeUpUser` which is for now only useful until we have better testing infrastructure.
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

