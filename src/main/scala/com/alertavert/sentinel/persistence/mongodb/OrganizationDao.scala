package com.alertavert.sentinel.persistence.mongodb

import com.alertavert.sentinel.model.Organization
import com.alertavert.sentinel.persistence.DAO
import com.mongodb.casbah.Imports
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import java.util.Date

class OrganizationDao(val uri: String) extends DAO[Organization] {
  /**
   * Returns a list of all the items.
   *
   * @param limit the maximum number of items to return, or all if 0 (the default)
   * @param offset where to start from (by default, the first element)
   * @return a list of items found in the underlying storage
   */
  override def findAll(limit: Int, offset: Int): Iterable[Organization] = ???

  /**
   * Removes the object whose ID matches, if any.
   *
   * @param id the unique ID of the object to remove
   * @return ``true`` if the ``id`` is found, and the object was successfully removed;
   *         ``false`` otherwise.
   */
  override def remove(id: Imports.ObjectId): Boolean = ???

  override def find(id: Imports.ObjectId): Option[Organization] = ???

  override def upsert(item: Organization): Imports.ObjectId = ???
}

object OrganizationDao {

  def serialize(org: Organization) = MongoDBObject(
    "_id" -> (org.id match {
      case None => null
      case Some(x: ObjectId) => x
    }),
    "name" -> org.name,
    "active" -> org.active,
    "created" -> org.created,
    "created_by" -> org.created_by.id
  )

  def deserialize(item: MongoDBObject) = {
    val builder = Organization.builder(item.as[String]("name"))
    (builder withId(item.as[ObjectId]("_id")) setActive(item.as[Boolean]("active"))
      created(item.as[Date]("created"))
      createdBy(UserDao().find(item.as[ObjectId]("created_by")).get)) build
  }
}
