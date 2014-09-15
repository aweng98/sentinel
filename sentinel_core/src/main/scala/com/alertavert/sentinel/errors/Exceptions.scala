package com.alertavert.sentinel.errors

import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.security.Permission
import org.bson.types.ObjectId

/**
 * Exception classes that enable Application-specific errors to be surfaced
 */

class SentinelException(val message: String, val cause: Throwable = null) extends
    Exception (message, cause)


class NotAllowedException(override val message: String, override val cause: Throwable = null)
  extends SentinelException (message, cause)


class DbException(override val message: String, override  val cause: Throwable = null) extends
    SentinelException (message, cause)


class SecurityException(override val message: String) extends SentinelException(message) {
  def this(subject: User) = this("Could not authorize user: " + subject.getCredentials.username)
}

class AuthenticationError(override val message: String = "Invalid credentials or authentication failure",
                          val subject: User = null) extends SecurityException(message) {

  def details = {
    if (subject != null) s"$message - Could not authenticate " + subject.getCredentials.username
    message
  }
}


class PermissionAccessError(val subject: User, val permission: Permission,
    override val message: String = "not available")
  extends
    SecurityException(s"$permission not allowed for $subject (detail: $message)")


class NotFoundException(val id: ObjectId, override val message: String) extends
    DbException(s"[$id] not found: $message")
