package models.resources

/**
 * REST resource to retrieve and manipulate User objects
 *
 * Created by marco on 9/1/14.
 */

import java.util.Date

import com.alertavert.sentinel.errors.AuthenticationError
import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.persistence.UserDao
import com.alertavert.sentinel.persistence.mongodb.MongoUserDao
import com.alertavert.sentinel.security.Credentials
import org.bson.types.ObjectId
import play.api.libs.json.{JsObject, Json, JsValue}

object UsersResource {

  val dao = MongoUserDao()

  def getAllUsers = {
    dao.findAll()
  }

  def getUserById(id: String) = {
    dao.find(new ObjectId(id))
  }

  def authUser(username: String, password: String): User = {
    dao.findByUsername(username) match {
      case None => throw new AuthenticationError(s"Cannot authenticate $username")
      case Some(user) => {
        if (!user.authenticate(username, password)) throw new AuthenticationError()
        // Record that the user has now logged in and update its 'last-seen' field
        user.updateActivity()
        dao.upsert(user)
        user
      }
    }
  }

  def createUser(request: JsValue): JsObject = {
    val first = (request \ "first_name").as[String]
    val last = (request \ "last_name").as[String]
    val username = (request \ "username").as[String]
    val password = (request \ "password").as[String]

    // Create a new set of credentials from the username/password
    val creds = Credentials.createCredentials(username, password)
    val user = User builder(first, last) hasCreds creds build()

    val userId = dao << user

    Json.obj(
      "id" -> userId.toString
    )
  }
}
