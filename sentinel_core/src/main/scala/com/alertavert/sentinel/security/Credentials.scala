// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.security

import java.security.SecureRandom

import scala.language.postfixOps

/**
 * User credentials class
 *
 * <p>A user is uniquely authenticated by its username and password: the latter is
 * always stored (and manipulated across the system) in its hashed form (SHA-256,
 * until they break that one too).
 *
 * <p>A user is also given a random salt when it is created by the system for the first
 * time, which is subsequently used to hash the password (so that users with the same
 * password will have anyway different hashes).
 *
 * <p>In addition, from the username, password and salt, an API key is generated (pseudo
 * random, but deterministically from those values) and this is then used to sign all
 * API calls (on the client side) and to verify those (server side).
 */
class Credentials(val username: String,
                  val hashedPassword: String,
                  val salt: Long) {


  private val _apiKey: Array[Byte] = createApiKey()

  /** Returns the API key as a base-64 encoded string */
  def apiKey = base64Encoder.encode(_apiKey)

  /**
   * Creates an API key from the hashed password and salt
   *
   * @return a pseudo-random API key in Base64 encoding
   */
  private def createApiKey() = {
    md.reset()
    md.update(hashedPassword getBytes)
    md.digest
  }

  def saltToBytes(): Array[Byte] = {
    val numBytes = for {
      i <- 0 until 8
    } yield (salt >>> i * 8) & 0xFF toByte

    numBytes toArray
  }

  /**
   * Base64 encoding of the salt value
   *
   * @return String representation of the salt value encoded in Base64
   */
  def saltToString = base64Encoder.encode(saltToBytes())

  def canEqual(other: Any): Boolean = other.isInstanceOf[Credentials]

  /**
   * @inheritdoc
   *
   * Checks that the given credentials match these ones (same username and
   * password).
   *
   * @param other the Credentials to match against
   * @return `true` if `other` matches these credentials
   */
  override def equals(other: Any): Boolean = other match {
    case that: Credentials =>
      (that canEqual this) &&
        username == that.username &&
        hashedPassword == that.hashedPassword
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(username, hashedPassword)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"[$username :: $hashedPassword]"
}

object Credentials {

  def apply(username: String, password: String) = createCredentials(username, password)

  /**
   * Computes the SHA-256 hash of the password and returns it encoded Base-64
   *
   * @param password in plaintext, it will be hashed
   * @param salt a seed that makes hashed passwords different among users,
   *             even if they choose the same password (as users do)
   * @return the hash of `password` in `base64` encoding
   */
  def hashPwd(password: String, salt: Long): String = {
    val bytes = makeRnd(salt) ++ password.getBytes
    base64Encoder.encode(hash(bytes).toArray)
  }

  /**
   * Creates a new set of credentials from a username and plaintext password.
   * The password will be stored in hashed format, and a new salt (to create the new API key)
   * will be generated
   *
   * @param username the username to generate credentials for
   * @param password the plaintext password; this will be used to generate the
   *                 hash, but never stored
   */
  def createCredentials(username: String, password: String): Credentials = {
    val secureRnd = SecureRandom.getInstance(RND_ALGORITHM)
    val salt = secureRnd.nextLong()
    val hashedPwd = hashPwd(password, salt)
    new Credentials(username, hashedPwd, salt)
  }

}
