// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.persistence.mongodb

import java.util.Date

import com.alertavert.sentinel.errors.{DbException, NotAllowedException}
import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.persistence.DataAccessManager
import com.alertavert.sentinel.security.{Action, Credentials, Permission}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCollection

import scala.language.postfixOps

class CredentialsSerializer extends MongoSerializer[Credentials] {

  override def serialize(credentials: Credentials) = MongoDBObject(
    "username" -> credentials.username,
    "password" -> credentials.hashedPassword,
    "salt" -> credentials.salt
  )

  override def deserialize(item: MongoDBObject): Credentials =
    new Credentials(item.as[String]("username"), item.as[String]("password"),
      item.as[Long]("salt"))
}

class PermissionSerializer extends MongoSerializer[Permission] {

  override def serialize(permission: Permission) = MongoDBObject(
    "action" -> permission.action,
    "resource_id" -> permission.resource.id
  )

  override def deserialize(item: MongoDBObject): Permission = {
    val action = item.as[Action]("action")
    val resource = MongoResourceDao().find(item.as[ObjectId]("resource_id")).get
    new Permission(action, resource)
  }
}


class MongoUserDao(override val collection: MongoCollection) extends MongoDao[User](collection)
with MongoSerializer[User] {

  val credsSerializer = new CredentialsSerializer

  override def serialize(user: User): MongoDBObject = {
    val userObj = MongoDBObject(
      "first" -> user.firstName,
      "last" -> user.lastName,
      "credentials" -> credsSerializer.serialize(user.getCredentials),
      "active" -> user.isActive,
      "last_seen" -> user.lastSeen
    )
    userObj
  }

  override def deserialize(item: MongoDBObject): User = {
    val user = (User.builder(item.as[String]("first"), item.as[String]("last"))
      hasCreds credsSerializer.deserialize(item.as[BasicDBObject]("credentials"))
      lastSeenAt item.as[Date]("last_seen")).build()
    user
  }

  override def findByName(username: String): Option[User] = collection.findOne(
    MongoDBObject("credentials.username" -> username)) match {
    case None => None
    case Some(item) => Some(deserialize(item))
  }

  /**
   * @inheritdoc
   *
   * <p>This verifies that the username for the new `user` is not already taken; if it is,
   * it checks that this is an update operation and the IDs match.
   *
   * @param user the new [[User]] to upsert in the DB
   */
  override def beforeUpsert(user: User): Unit = {
    findByName(user.getCredentials.username) match {
      case Some(existUser) => if (!existUser.id.equals(user.id)) throw new NotAllowedException(
        s"Username must be unique - user [${existUser}] already in DB")
      case None =>
    }
  }
}


object MongoUserDao {
  val USER_COLLECTION = "users"
  private var instance: Option[MongoUserDao] = None

  def apply(): MongoUserDao = instance match {
    case None =>
      if (DataAccessManager isReady) {
        instance = Some(new MongoUserDao(DataAccessManager.db(USER_COLLECTION))
          with IdSerializer[User] with CreatorSerializer[User])
      }
      instance.getOrElse(throw new DbException("DataAccessManager not initialized"))
    case Some(x) => instance.get
  }
}
