import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.security.Credentials
import play.api.libs.json.{Json, Writes}

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
      "first_name" -> user.firstName,
      "last_name" -> user.lastName,
      "active" -> user.isActive,
      "last_seen" -> format.format(user.lastSeen),
      "credentials" -> Json.toJson(user.getCredentials)
    )
  }
}
