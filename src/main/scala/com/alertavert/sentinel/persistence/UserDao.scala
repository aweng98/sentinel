package com.alertavert.sentinel.persistence

import com.alertavert.sentinel.model.User
import com.mongodb.casbah.Imports.ObjectId
import com.alertavert.sentinel.errors.DbException

/**
 * Created by marco on 3/8/14.
 */
trait UserDao {

  def save(user: User): ObjectId
  def remove(userId: ObjectId): Boolean
  def update(userId: ObjectId, user: User): Boolean

//  def update(user: User) = user.id match {
//    case None => throw new DbException("Cannot update a User without a valid ID")
//    case Some(x) => this.update(user.id, user)
//  }
}
