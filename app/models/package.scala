
// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

import com.alertavert.sentinel.errors.{SentinelException, AuthenticationError}
import models.resources.UsersResource

import scala.concurrent.Future

import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import com.alertavert.sentinel.model.{Organization, User}
import com.alertavert.sentinel.security.Credentials
import com.alertavert.sentinel.security.{encode, hashStrings}

/**
 * JSON Serialization models
 *
 * Created by marco on 9/1/14.
 */
package object models {

  implicit val credsWrites = new Writes[Credentials] {
    def writes(creds: Credentials) = Json.obj(
      "username" -> creds.username,
      "api_key" -> creds.apiKey
    )
  }

  implicit val userWrites = new Writes[User] {
    val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss zzz")
    def writes(user: User) = Json.obj(
      "id" -> user.id.getOrElse(throw new IllegalStateException("User " +
        user.getCredentials.username + " has no valid ID")).toString,
      "first_name" -> user.firstName,
      "last_name" -> user.lastName,
      "active" -> user.isActive,
      "last_seen" -> format.format(user.lastSeen),
      "credentials" -> Json.toJson(user.getCredentials)
    )
  }

  implicit val orgsWrites = new Writes[Organization] {
    def writes(org: Organization) = Json.obj(
      "id" -> org.id.getOrElse(throw new IllegalStateException(
        s"Organization $org has no valid ID")).toString,
      "name" -> org.name,
      "active" -> org.active
    )
  }
}

package object security {

  // TODO: Understand what really goes on behind the curtain
  // Code based on: https://www.playframework.com/documentation/2.0.2/ScalaActionsComposition

  case class AuthenticatedRequest[A](user: User, request: Request[A])
    extends WrappedRequest(request)

  def Authenticated[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result) = {
    Action(p) { request =>
        try {
          val user = validateHash(request).getOrElse(
              throw new AuthenticationError())
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

    // Extract the value pairs from the Auth header: username=foo;hash=xYzaa99==
    val auth = request.headers.get("Authorization").getOrElse(
      throw new AuthenticationError("Missing `Authorization:` header"))
    val values = parseAuthHeader(auth)

    val username = values.getOrElse("username",
      throw new AuthenticationError("`username` key missing in Authorization header"))
    val hash = values.getOrElse("hash",
      throw new AuthenticationError("`hash` key missing in Authorization header"))
    // TODO: some hashing negotiation - for now only SHA-256 supported
    val user = UsersResource.getUserByUsername(username).getOrElse(
      throw new AuthenticationError(s"$username is not a recognized username"))
    val apiKey = user.getCredentials.apiKey

    // TODO: verify that the Date this request is signed with is not a replay attack
    val computedHash = encode(hashStrings(List(
      apiKey,
      date,
      request.path,
      request.body.toString
    )))
    // TODO: replace with a log.debug()
    println(s"Computed hash: $computedHash")
    if (computedHash == hash) Some(user) else throw new AuthenticationError(
      "Hash values don't match")
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

