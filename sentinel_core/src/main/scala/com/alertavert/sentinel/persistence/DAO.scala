// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.persistence

import com.alertavert.sentinel.model.{Organization, User}
import com.mongodb.casbah.Imports.ObjectId

/**
 * Generic DAO definition.
 */
trait DAO[T <: HasId] {
  def clear(): Unit

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
   * Generic method to retrieve entities on the basis of their "natural" `name`
   *
   * Each derived DAO implementation is free to interpret the meaning of `name` as is most
   * natural (or expedient) for the given entity; generally speaking,
   * every entity in the system should have a "user-friendly" name, alongside with a unique `ID`;
   * the user will be typically exposed to the former, admin users and support staff (as well as
   * clients taking advantage of the API) will be mostly exposed to the latter.
   *
   * @param name the entity's name, which will be used to look it up
   * @return if found, the entity, wrapped in an `Option`; otherwise, `None`
   */
  def findByName(name: String): Option[T]

  /**
   * Removes the object whose ID matches, if any.
   *
   * @param id the unique ID of the object to remove
   * @return ``true`` if the ``id`` is found, and the object was successfully removed;
   *        ``false`` otherwise.
   */
  def remove(id: ObjectId): Boolean

  def -=(item: T) {
    item.id match {
      case None =>
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
