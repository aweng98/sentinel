// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package models.resources

/**
 * REST resource to retrieve and manipulate User objects
 *
 * Created by marco on 9/1/14.
 */

import java.util.Date

import com.alertavert.sentinel.errors.{NotAllowedException, AuthenticationError}
import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.persistence.UserDao
import com.alertavert.sentinel.persistence.mongodb.MongoUserDao
import com.alertavert.sentinel.security.Credentials
import org.bson.types.ObjectId
import play.api.libs.json.{JsObject, Json, JsValue}
import play.api.libs.openid.Errors.AUTH_CANCEL

object UsersResource {

  val dao = MongoUserDao()

  def getAllUsers = {
    dao.findAll()
  }

  def getUserById(id: String) = {
    dao.find(new ObjectId(id))
  }

  def getUserByUsername(username: String) = {
    dao.findByName(username)
  }

  def authUser(username: String, password: String): User = {
    dao.findByName(username) match {
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

  /**
   * Creates a new user, based on the request body (JSON) and then returns it,
   * after saving it to the database
   *
   * @param request the HTTP request body
   * @return the newly created `User`
   */
  def createUser(request: JsValue) = {
    val first = (request \ "first_name").as[String]
    val last = (request \ "last_name").as[String]
    val username = (request \ "username").as[String]
    val password = (request \ "password").as[String]

    // First ensure the user does not exist already:
    getUserByUsername(username) match {
      case None =>
      case Some(_) => throw new NotAllowedException(s"$username already exists")
    }
    // Create a new set of credentials from the username/password
    val creds = Credentials.createCredentials(username, password)
    val user = User builder(first, last) hasCreds creds build()

    dao << user
    user
  }
}
