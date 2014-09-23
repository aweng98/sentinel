package com.alertavert.sentinel.persistence.mongodb

import com.mongodb.casbah.commons.TypeImports._

import language.postfixOps
import com.alertavert.sentinel.model.User
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCollection
import java.util.Date
import com.alertavert.sentinel.security.{Action, Permission, Credentials}
import com.alertavert.sentinel.persistence.DataAccessManager

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
  val permsSerializer = new PermissionSerializer

  override def serialize(user: User): MongoDBObject = {
    val permissions = for {
      p <- user.perms
    } yield permsSerializer.serialize(p)

    val userObj = MongoDBObject(
      "first" -> user.firstName,
      "last" -> user.lastName,
      "credentials" -> credsSerializer.serialize(user.getCredentials),
      "active" -> user.isActive,
      "last_seen" -> user.lastSeen,
      "permissions" -> permissions
    )
    userObj
  }

  override def deserialize(item: MongoDBObject): User = {
    val user = (User.builder(item.as[String]("first"), item.as[String]("last"))
      hasCreds credsSerializer.deserialize(item.as[BasicDBObject]("credentials"))
      lastSeenAt item.as[Date]("last_seen")).build()
    val permsList = item.as[MongoDBList]("permissions") map(p => permsSerializer.deserialize(p
      .asInstanceOf[BasicDBObject]))
    user.perms ++= permsList
    user
  }

  override def findByName(username: String): Option[User] = collection.findOne(
    MongoDBObject("credentials.username" -> username)) match {
    case None => None
    case Some(item) => Some(deserialize(item))
  }
}


object MongoUserDao {
  val USER_COLLECTION = "users"
  private var instance: MongoUserDao = _

  def apply(): MongoUserDao = instance match {
    case null =>
      if (DataAccessManager isReady) {
        instance = new MongoUserDao(DataAccessManager.db(USER_COLLECTION))
          with IdSerializer[User] with CreatorSerializer[User]
      } else {
        throw new IllegalStateException("DataAccessManager not initialized")
      }
      instance
    case _ => instance
  }
}
