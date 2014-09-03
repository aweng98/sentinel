package models.resources

/**
 * REST resource to retrieve and manipulate User objects
 *
 * Created by marco on 9/1/14.
 */

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
      case None => throw new AuthenticationError(
          User.builder("") hasCreds Credentials.createCredentials(username, password) build
        )
      case Some(user) => {
        val salt = user.getCredentials.salt
        val hashPwd = Credentials.hash(password, salt)
        if (hashPwd != user.getCredentials.hashedPassword) throw new AuthenticationError(user)
        user
      }
    }
  }

  // TODO(marco): this layer should know nothing about JSON and friends
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
