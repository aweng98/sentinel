// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package models.resources

/**
 * REST resource to retrieve and manipulate User objects
 *
 * Created by marco on 9/1/14.
 */

import com.alertavert.sentinel.errors.{NotFoundException, AuthenticationError, NotAllowedException}
import com.alertavert.sentinel.model.{Organization, User}
import com.alertavert.sentinel.persistence.mongodb.{UserOrgsAssocDao, MongoUserDao}
import models._
import org.bson.types.ObjectId

import play.api.libs.json.JsValue
import play.api.mvc.AnyContent

object UsersResource {

  val dao = MongoUserDao()

  def associateWithOrganization(uid: ObjectId, oid: ObjectId, request: JsValue) = {
    val user = getUserById(uid).getOrElse(throw new NotFoundException(uid, "Not a valid user"))
    val org = OrgsResource.getOrgById(oid).getOrElse(throw new NotFoundException(oid,
      "Not a valid Organization"))
    val role = (request \ "role").as[String]
    try {
      val currentAssociations = UserOrgsAssocDao().findAll(user)
      UserOrgsAssocDao().associate(user, currentAssociations :+ (org, role))
    } catch {
      case ex: NotFoundException => UserOrgsAssocDao().associate(user, Seq((org, role)))
      case ex: Exception => throw ex
    }
  }

  def getOrgsForUser(id: ObjectId): Map[Organization, String] = {
    getUserById(id) match {
      case None => throw new NotFoundException(id, "Not a valid user")
      case Some(user) => UserOrgsAssocDao().findAll(user).toMap
    }
  }


  def getAllUsers = {
    dao.findAll()
  }

  def getUserById(id: ObjectId) = {
    dao.find(id)
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
    val user = request.as[User]
    // First ensure the user does not exist already:
    val username = user.getCredentials.username
    getUserByUsername(username) match {
      case None =>
      case Some(_) => throw new NotAllowedException(s"$username already exists")
    }
    dao << user
    user
  }
}
