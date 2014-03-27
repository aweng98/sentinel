package com.alertavert.sentinel.errors

import com.alertavert.sentinel.model.User

/**
 * Created by marco on 2/23/14.
 */

// TODO: add constructor that take a message too
class SentinelException(cause: Throwable) extends  Throwable(cause) {

}

class NotAllowedException(cause: Throwable) extends SentinelException(cause) {

}

class DbException(cause: Throwable) extends SentinelException(cause) {

}

class SecurityException(cause: Throwable, val subject: User, val message: String) extends SentinelException(cause) {

}
