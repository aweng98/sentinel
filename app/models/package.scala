
import com.alertavert.sentinel.errors.AuthenticationError

import scala.concurrent.Future

import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.security.Credentials

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

  class AuthenticatedRequest[A](val username: String, request: Request[A])
    extends WrappedRequest[A](request)

  object Authenticated extends ActionBuilder[AuthenticatedRequest] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
      request.headers.get("Authorization").map { authHeadr =>
        println(s"Authentication: $authHeadr")
        val segments = authHeadr.split(";")
        val usr = segments.filter(_.startsWith("user")).head.split("=")(1)
        val hash = segments.filter(_.startsWith("hash")).head.split("=")(1)
        println(s"Validating user [$usr]")
        block(new AuthenticatedRequest(usr, request))
      } getOrElse {
          Future.successful(Results.Forbidden)
      }
    }
  }
}
