package com.alertavert.sentinel.persistence

import com.alertavert.sentinel.model.User
import com.mongodb.casbah.Imports.ObjectId

/**
 * DAO for the User object.
 */
trait UserDao {

  /**
   * Used to insert a new user into the system, or update an existing one.
   *
   * <p>If the {@example code.id} is not specified, it assumes this is a new
   * user instance and one will be created in the database, and the new {@example id}
   * returned; otherwise, the user whose ID matches in the DB, will be updated with the
   * contents of the passed in object.
   *
   * @param user the instance that will be saved ("upserted") in the DB
   * @return the ID of the new (or existing) user
   */
  def save(user: User): ObjectId

  /**
   * Retrieves the user, if any, that matches the ID
   *
   * @param user_id the ID of the user that we want to retrieve
   * @return the corresponding user, or ``None``
   */
  def find(user_id: ObjectId): Option[User]

  /**
   * Deletes the user whose ID corresponds to the given ``ObjectId``
   *
   * @param userId the ID of the user to delete
   * @return ``true`` if the user has been found and removed, ``false`` otherwise
   */
  def remove(userId: ObjectId): Boolean

  /**
   * Retrieves a User by its username, if it exists
   *
   * @param username that will be looked up
   * @return the corresponding ``User`` object, or None
   */
  def findByUsername(username: String): Option[User]
}
