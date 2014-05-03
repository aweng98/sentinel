package com.alertavert.sentinel.persistence.mongodb

import language.postfixOps
import com.alertavert.sentinel.model.User
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCollection
import java.util.Date
import com.alertavert.sentinel.security.Credentials
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


class MongoUserDao(val userCollection: MongoCollection) extends MongoDao[User](userCollection)
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
    (User.builder(item.as[String]("first"), item.as[String]("last"))
      hasCreds credsSerializer.deserialize(item.as[BasicDBObject]("credentials"))
      lastSeenAt item.as[Date]("last_seen")).build()
  }
}


object MongoUserDao {
  private val USER_COLLECTION = "users"
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
