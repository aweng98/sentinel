
import com.alertavert.sentinel.errors.AuthenticationError
import models.resources.UsersResource

import scala.concurrent.Future

import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import com.alertavert.sentinel.model.User
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
          case ex: Exception => Results.Unauthorized
        }
    }
  }

  // TODO: the returned errors disclose too much information; replace with generic errors in Prod
  def validateHash[A](request: Request[A]): Option[User] = {
    println(request.headers)
    val date = request.headers.get("Date-auth").getOrElse(
      throw new AuthenticationError("Missing `Date:` header"))

    // Extract the value pairs from the Auth header: username=foo;hash=xYzaa99==
    val auth = request.headers.get("Authorization").getOrElse(
      throw new AuthenticationError("Missing `Authorization:` header"))
    val values = parseAuthHeader(auth)
    println(2)

    val username = values.getOrElse("username",
      throw new AuthenticationError("`username` key missing in Authorization header"))
    val hash = values.getOrElse("hash",
      throw new AuthenticationError("`hash` key missing in Authorization header"))
    // TODO: some hashing negotiation - for now only SHA-256 supported
    val user = UsersResource.getUserByUsername(username).getOrElse(
      throw new AuthenticationError(s"$username is not a recognized username"))
    val apiKey = user.getCredentials.apiKey
        println(3)

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
