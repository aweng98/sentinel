package com.alertavert.sentinel.persistence.mongodb

import language.postfixOps
import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.persistence.DAO
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.{MongoCollection, MongoURI}
import com.mongodb.casbah.commons.TypeImports.ObjectId
import java.util.Date
import com.alertavert.sentinel.security.Credentials

class UserDao(val userCollection: MongoCollection) extends DAO[User] {

  override def find(id: ObjectId): Option[User] = userCollection.findOne(
    MongoDBObject("_id" -> id)) match {
      case None => None
      case Some(item) => Some(UserDao.deserialize(item))
    }

  /**
   * Removes the object whose ID matches, if any.
   *
   * @param id the unique ID of the object to remove
   * @return ``true`` if the ``id`` is found, and the object was successfully removed;
   *         ``false`` otherwise.
   */
  override def remove(id: ObjectId): Boolean = ???

  override def upsert(user: User): ObjectId = {
    val item = UserDao.serialize(user)
    val writeResult = userCollection += item
    val cmdResult = writeResult.getLastError()
    // TODO: create app-specific exception and throw, with better error message
    if (! cmdResult.ok()) throw new RuntimeException("Save failed: " + cmdResult.getErrorMessage)
    item.as[ObjectId] ("_id")
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
    } yield UserDao.deserialize(user)
  }
}


object UserDao {

  val USER_COLLECTION = "users"
  var instance: UserDao = _

  // TODO: should I just use a Singleton here?
  // TODO: use a DataAccessManager as a Factory for all the DAOs
  private def create(uri: String) {
    val mongoUri = MongoURI(uri)
    val dbName = mongoUri.database.getOrElse(throw new IllegalArgumentException("MongoDB URI must" +
      " specify a database name (use: mongodb://[[host][:port]]/database"))
    val mongoConn = MongoConnection(mongoUri)
    instance = new UserDao(mongoConn.getDB(dbName)(UserDao.USER_COLLECTION))
  }

  def apply(uri: String = "mongod:///test"): UserDao = instance match {
    case null => create(uri); instance
    case _ => instance
  }

  def serializeCredentials(credentials: Credentials) = MongoDBObject(
      "username" -> credentials.username,
      "password" -> credentials.hashedPassword,
      "salt" -> credentials.salt
    )

  def deserializeCredentials(item: MongoDBObject, builder: User.Builder) =
    builder.hasCreds(item.as[String]("username"), item.as[String]("password"),
                     item.as[Long]("salt"))

  def serialize(user: User): MongoDBObject = {
    val userObj = MongoDBObject(
      "first" -> user.firstName,
      "last" -> user.lastName,
      "credentials" -> serializeCredentials(user.getCredentials),
      "active" -> user.isActive,
      "last_seen" -> user.lastSeen,
      "created_at" -> user.created,
      "created_by" -> user.createdBy.getOrElse(null)
    )
    user.id match {
      case None =>
      case Some(x) => userObj += "_id" -> x
    }
    userObj
  }

  def deserialize(item: MongoDBObject): User = {
    val builder = User.builder(item.as[String]("first"), item.as[String]("last"))
    (deserializeCredentials(item.as[BasicDBObject]("credentials"),
      builder) withId item._id.getOrElse(throw new IllegalStateException("All DB items should " +
          "have a valid _id - missing for user [" + builder.credentials.get.username + "]"))
      createdBy item.as[ObjectId]("created_by")
      wasCreatedOn item.as[Date]("created_at")
      lastSeenAt item.as[Date]("last_seen")).build()
  }
}
