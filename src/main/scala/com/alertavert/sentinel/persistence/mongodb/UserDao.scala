package com.alertavert.sentinel.persistence.mongodb

import language.postfixOps
import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.persistence.DAO
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.{MongoCollection, MongoURI}
import com.mongodb.casbah.commons.ValidBSONType.ObjectId
import com.mongodb.casbah.commons.TypeImports.ObjectId
import java.util.Date

/**
 * Created by marco on 3/30/14.
 */
class UserDao(val mongo: MongoConnection, val dbName: String) extends DAO[User] {

  val userCollection: MongoCollection = mongo.getDB(dbName)(UserDao.USER_COLLECTION)

  override def find(id: ObjectId): Option[User] = ???

  /**
   * Removes the object whose ID matches, if any.
   *
   * @param id the unique ID of the object to remove
   * @return ``true`` if the ``id`` is found, and the object was successfully removed;
   *         ``false`` otherwise.
   */
  override def remove(id: ObjectId): Boolean = ???

  override def upsert(item: User): ObjectId = {
    val userObj = MongoDBObject(
        "first" -> item.firstName,
        "last" -> item.lastName,
        // TODO: add credentials
        "active" -> item.isActive,
        "last_seen" -> item.lastSeen,
        "created_at" -> item.created,
        "created_by" -> item.createdBy.getOrElse(throw new IllegalArgumentException("Cannot save " +
          "a user without a 'created_by' value (it must have been created by another authorized " +
          "user)"))
    )
    item.id match {
      case None =>
      case Some(x) => userObj += "_id" -> x
    }
    val writeResult = userCollection += userObj
    val cmdResult = writeResult.getLastError()
    // TODO: create app-specific exception and throw, with better error message
    if (! cmdResult.ok()) throw new RuntimeException("Save failed: " + cmdResult.getErrorMessage)
    userObj.as[ObjectId] ("_id")
  }

  /**
   * Returns a list of all the items.
   *
   * @param limit the maximum number of items to return, or all if 0 (the default)
   * @param offset where to start from (by default, the first element)
   * @return a list of items found in the underlying storage
   */
  override def findAll(limit: Int, offset: Int): Iterable[User] = {
    for {
      user <- userCollection find() toIterable
    } yield deserialize(user)
  }

  def deserialize(item: MongoDBObject): User = {
    (User.builder(item.as[String] ("first"), item.as[String] ("last"))
        withId item._id.getOrElse(null)
        createdBy item.as[ObjectId]("created_by")
        wasCreatedOn item.as[Date]("created_at")
        lastSeenAt item.as[Date]("last_seen")) build
  }
}


object UserDao {

  val USER_COLLECTION = "users"

  // TODO: should I just use a Singleton here?
  def create(uri: String): UserDao = {
    val mongoUri = MongoURI(uri)
    val dbName = mongoUri.database.getOrElse(throw new IllegalArgumentException("MongoDB URI must" +
      " specify a database name (use: mongodb://[[host][:port]]/database"))
    val mongoConn = MongoConnection(mongoUri)
    new UserDao(mongoConn, dbName)
  }
}
