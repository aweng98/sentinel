package com.alertavert.sentinel.persistence

import com.alertavert.sentinel.model.{Organization, User}
import com.mongodb.casbah.Imports.ObjectId

/**
 * Generic DAO definition.
 */
trait DAO[T <: HasId] {

  /**
   * Saves the item to the underlying persistence layer (eg, DB)
   *
   * If ``item`` does not define already an ID, a new one will be created.
   * (usually, this is referred to as an 'upsert' method).
   *
   * @param item the object to save
   * @return the ID of the newly created object
   */
  def <<(item: T): ObjectId = upsert(item)

  def upsert(item: T): ObjectId

  /**
   * Retrieves the object whose ID is given, if any.
   *
   * @param id the unique ID of the object we are looking up
   * @return the object itself, if the ID exists in the storage layer, or ``None``
   */
  def >>(id: ObjectId): Option[T] = find(id)

  def find(id: ObjectId): Option[T]

  /**
   * Removes the object whose ID matches, if any.
   *
   * @param id the unique ID of the object to remove
   * @return ``true`` if the ``id`` is found, and the object was successfully removed;
   *        ``false`` otherwise.
   */
  def remove(id: ObjectId): Boolean

  def -=(item: T) {
    item id match {
      case None => false
      case Some(obj_id) => remove(obj_id)
    }
  }

  /**
   * Returns a list of all the items.
   *
   * @param limit the maximum number of items to return, or all if 0 (the default)
   * @param offset where to start from (by default, the first element)
   * @return a list of items found in the underlying storage
   */
  def findAll(limit: Int = 0, offset: Int = 0): Iterable[T]
}

abstract class UserDao extends DAO[User]
abstract class OrganizationDao extends DAO[Organization]
