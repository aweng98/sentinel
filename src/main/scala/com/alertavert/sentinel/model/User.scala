package com.alertavert.sentinel.model

//import language.postfixOps
import java.util.Date

import com.alertavert.sentinel.security.Credentials
import com.mongodb.casbah.Imports.ObjectId
import com.alertavert.sentinel.persistence.HasId

/**
 * The main core class of the system, models a user of the system.
 *
 * This is a `value` class, and once created (using a {@link Builder}) the values
 * of its properties ought not be changed (although there are helper methods, such as
 * ones to reset the password, activate a user and update when it was last logged in).
 *
 * @since 0.1
 */

class User() extends HasId {
  private var _id: Option[ObjectId] = None
  private var _firstName: String = _
  private var _lastName: String = _
  private var _credentials: Credentials = Credentials.emptyCredentials
  private var _created: Date = _
  private var _createdBy: Option[ObjectId] = None
  private var _active: Boolean = _
  private var _lastSeen: Date = _

  // Implementation of HasId trait
  override def id = _id

  override def setId(id: ObjectId) {
    this._id = Some(id)
  }

  // A number of "getter" methods to retrieve values for this User
  def firstName = _firstName
  def lastName = _lastName
  def created = _created
  def createdBy= _createdBy
  def isActive = _active
  def lastSeen = _lastSeen

  def getCredentials = _credentials

  def checkCredentials(that: Credentials) = _credentials == that

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
    val userId = _id match {
      case None => ""
      case Some(x) => x toString
    }
    val enabled = _active match {
      case true => "Active"
      case false => "Disabled"
    }

    s"[$userId] $firstName $lastName ($enabled)"
  }


  def canEqual(other: Any): Boolean = other.isInstanceOf[User]

  override def equals(other: Any): Boolean = other match {
    case that: User =>
      (that canEqual this) &&
        idEquals(_id, that._id) &&
        _credentials.username == that._credentials.username
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(_id, _credentials)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object User {

  class Builder(val first: String, val last: String = "") {
    var id: Option[ObjectId] = None
    var credentials: Option[Credentials] = None
    var created_by: Option[ObjectId] = None
    var created = new Date()
    var active = false
    var lastSeen = new Date()

    def withId(id: ObjectId): Builder = {
      this.id = Some(id)
      this
    }

    def hasCreds(username: String, hashedPassword: String, salt: Long) = {
      credentials = Some(new Credentials(username, hashedPassword, salt))
      this
    }

    def createdBy(userId: ObjectId) = {
      this.created_by = Some(userId)
      this
    }

    def wasCreatedOn(when: Date) = {
      this.created = when
      this
    }

    def isActive = {
      this.active = true
      this
    }

    def lastSeenAt(lastSeen: Date) = {
      this.lastSeen = lastSeen
      this
    }

    def build(): User = {
      val user = new User()
      user._id = id
      user._firstName = first
      user._lastName = last
      user._credentials = credentials match {
        case None => Credentials.emptyCredentials
        case Some(_) => credentials get
      }
      user._createdBy = created_by
      user._created = created
      user._lastSeen = lastSeen
      if (active) user.activate()
      user
    }
  }

  def builder(first: String, last: String = "") = {
    new Builder(first, last)
  }
}
