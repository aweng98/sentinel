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


class SecurityException(val subject: User, override val message: String) extends
    SentinelException(s"Could not authenticate $subject: $message")


class PermissionAccessError(val permission: Permission, override val message: String) extends
    SecurityException(permission.grantedTo.getOrElse(User.unknown), message)


class NotFoundException(val id: ObjectId, override val message: String) extends
    DbException(s"[$id] not found: $message")
