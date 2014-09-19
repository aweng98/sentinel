package com.alertavert.sentinel.persistence.mongodb

import language.postfixOps
import com.alertavert.sentinel.persistence.{HasId, DAO}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.TypeImports.ObjectId
import com.alertavert.sentinel.errors.DbException

abstract class MongoDao[T <: HasId](val collection: MongoCollection) extends DAO[T] with
    MongoSerializer[T] {

  override def find(id: ObjectId): Option[T] = collection.findOne(
    MongoDBObject("_id" -> id)) match {
      case None => None
      case Some(item) => Some(deserialize(item))
    }

  /**
   * Removes the object whose ID matches, if any.
   *
   * @param id the unique ID of the object to remove
   * @return ``true`` if the ``id`` is found, and the object was successfully removed;
   *         ``false`` otherwise.
   */
  override def remove(id: ObjectId): Boolean =
    (collection -= MongoDBObject("_id" -> id)).getN == 1

  override def upsert(obj: T): ObjectId = {
    val item = serialize(obj)
    val writeResult = collection += item
    val cmdResult = writeResult getLastError

    // TODO: create app-specific exception and throw, with better error message
    if (! cmdResult.ok()) throw new DbException("Save failed: " + cmdResult.getErrorMessage)
    obj.setId(item.as[ObjectId] ("_id"))
    obj.id.get
  }

  /**
   * Returns a list of all the items.
   *
   * @param limit the maximum number of items to return, or all if 0 (the default)
   * @param offset where to start from (by default, the first element)
   * @return a list of items found in the underlying storage
   */
  override def findAll(limit: Int, offset: Int): Iterable[T] = {
    for {
      item <- collection find() toIterable
    } yield deserialize(item)
  }

  override def clear() {
    collection remove MongoDBObject()
  }
}
