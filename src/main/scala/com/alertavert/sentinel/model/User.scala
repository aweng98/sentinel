package com.alertavert.sentinel.model

import java.util.{Date, UUID}
import com.alertavert.sentinel.security.Credentials

/**
 * The main core class of the system, models a user of the system.
 *
 * This is a `value` class, and once created (using a {@link Builder}) the values
 * of its properties ought not be changed (although there are helper methods, such as
 * ones to reset the password, activate a user and update when it was last logged in).
 *
 * @since 0.1
 */

class User() {
  private var firstName = ""
  private var lastName = ""
  private var credentials: Credentials = Credentials.emptyCredentials
  private var id: UUID = UUID.randomUUID()
  private var created: Date = new Date()
  private var createdBy: UUID = null
  private var active: Boolean = false
  private var lastSeen: Date = new Date()

  override def toString: String = {
    val status = if (active) "active" else "disabled"
    s"[$id] $firstName $lastName ($status)"
  }

  // A number of "getter" methods to retrieve values for this User
  def userid = id
  def name = s"$firstName $lastName"
  def createdAt = created
  def creator = createdBy
  def seenAt = lastSeen
  def isActive = active

  def checkCredentials(that: Credentials) = credentials == that

  def activate() {
    active = true
  }

  def disable() {
    active = false
  }

  /**
   * Resets the password
   *
   * @param newPassword the plain text password, will be used to generate a new Credentials object,
   *                    that will store it hashed
   */
  def resetPassword(newPassword: String) {
    credentials = Credentials.createCredentials(credentials.username, newPassword)
  }

}

object User {

  class Builder(val first: String, val last: String = "") {
    var id = UUID.randomUUID()
    var credentials: Option[Credentials] = None
    var created_by: UUID = null
    var created = new Date()
    var active: Boolean = false

    def withId(id: UUID): Builder = {
      this.id = id
      this
    }

    def hasCreds(username: String, hashedPassword: String, salt: Long) = {
      credentials = Some(new Credentials(username, hashedPassword, salt))
      this
    }

    def createdBy(userId: UUID) = {
      this.created_by = userId
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

    def build(): User = {
      val user = new User()
      user.id = id
      user.firstName = first
      user.lastName = last
      user.credentials = credentials match {
        case None => Credentials.emptyCredentials
        case Some(_) => credentials get
      }
      user.createdBy = created_by
      user.created = created
      if (active) user.activate()
      user
    }
  }

  def builder(first: String, last: String = "") = {
    new Builder(first, last)
  }
}
