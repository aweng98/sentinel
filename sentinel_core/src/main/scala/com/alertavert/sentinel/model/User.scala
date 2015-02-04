// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.model

//import language.postfixOps
import java.util.Date

import com.alertavert.sentinel.errors.NotAllowedException
import com.alertavert.sentinel.security.{Permission, Credentials}
import com.mongodb.casbah.Imports.ObjectId
import com.alertavert.sentinel.persistence.{HasCreator, HasId}
import scala.collection.mutable

/**
 * The main core class of the system, models a user of the system.
 *
 * This is a `value` class, and once created (using a `Builder`) the values
 * of its properties ought not be changed (although there are helper methods, such as
 * ones to reset the password, activate a user and update when it was last logged in).
 *
 * @since 0.1
 */

class User() extends HasId with HasCreator {
  private var _firstName: String = _
  private var _lastName: String = _
  private var _credentials: Credentials = _
  private var _active: Boolean = _
  private var _lastSeen: Date = _

  // A number of "getter" methods to retrieve values for this User
  def firstName = _firstName
  def lastName = _lastName
  def isActive = _active
  def lastSeen = _lastSeen

  def getCredentials = _credentials

  def checkCredentials(that: Credentials) = _credentials == that

  /**
   * Authenticates user against plaintext password
   *
   * @param username
   * @param password the plaintext password for the user (we will use the `salt` stored in this User object to
   *                 hash the password and compare with the stored credentials
   * @return `true` if the credentials match
   * @see User#checkCredentials
   */
  def authenticate(username: String, password: String) = checkCredentials(new Credentials(username,
                                     Credentials.hashPwd(password, _credentials.salt),
                                     _credentials.salt))

  def activate() {
    _active = true
  }

  def disable() {
    _active = false
  }

  /**
   * Updates the 'last seen' field, recording that the user was active at the moment this method was called
   */
  def updateActivity() {
    this._lastSeen = new Date()
  }

  /**
   * Resets the password
   *
   * @param newPassword the plain text password, will be used to generate a new Credentials object,
   *                    that will store it hashed
   * @return the newly created credentials (which will contain the newly generated API key and salt)
   */
  def resetPassword(newPassword: String): Unit = {
    _credentials = Credentials.createCredentials(_credentials.username, newPassword)
    _credentials
  }

  // TODO: add toString() with the JSON representation of this User
  override def toString = {
    val userId = id match {
      case None => ""
      case Some(x) => x toString
    }
    val enabled = _active match {
      case true => "Active"
      case false => "Disabled"
    }
    val username = _credentials.username
    s"[$userId:$username] $firstName $lastName ($enabled)"
  }


  def canEqual(other: Any): Boolean = other.isInstanceOf[User]

  override def equals(other: Any): Boolean = other match {
    case that: User =>
      (that canEqual this) &&
        idEquals(id, that.id) &&
        _credentials.username == that._credentials.username
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(id, _credentials)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object User {

  class Builder(val first: String, val last: String = "") {
    var id: ObjectId = null
    var credentials: Option[Credentials] = None
    var created_by: User = null
    var created = new Date()
    var active = false
    var lastSeen = new Date()

    def withId(id: ObjectId): Builder = {
      this.id = id
      this
    }

    def hasCreds(username: String, hashedPassword: String, salt: Long) = {
      credentials = Some(new Credentials(username, hashedPassword, salt))
      this
    }

    def hasCreds(creds: Credentials) = {
      credentials = Some(creds)
      this
    }

    def createdBy(user: User) = {
      this.created_by = user
      this
    }

    def wasCreatedOn(when: Date) = {
      this.created = when
      this
    }

    def setActive(active: Boolean = true) = {
      this.active = true
      this
    }

    def lastSeenAt(lastSeen: Date) = {
      this.lastSeen = lastSeen
      this
    }

    def build(): User = {
      val user = new User()
      user.setId(id)
      user._firstName = first
      user._lastName = last
      val x = s"$first $last"
      user._credentials = credentials match {
        case None => throw new NotAllowedException(
          s" Cannot create a user without valid credentials; please specify a username and password for $first $last")
        case Some(_) => credentials get
      }
      user.createdBy = created_by match {
        case u: User => Some(u)
        case _ => None
      }
      user.createdAt = created
      user._lastSeen = lastSeen
      if (active) user.activate()
      user
    }
  }

  def builder(first: String, last: String = "") = {
    new Builder(first, last)
  }
}
