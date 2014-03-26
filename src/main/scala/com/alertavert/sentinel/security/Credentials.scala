package com.alertavert.sentinel.security

import java.security.{SecureRandom, MessageDigest}

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
  def apiKey = Credentials.base64Encoder.encode(_apiKey)

  /**
   * Creates an API key from the hashed password and salt
   *
   * @return a pseudo-random API key in Base64 encoding
   */
  private def createApiKey() = {
    Credentials.md.reset
    Credentials.md.update(hashedPassword getBytes)
    Credentials.md.update(saltToBytes)
    Credentials.md.digest
  }

  def saltToBytes(): Array[Byte] = {
    val numBytes = for {
      i <- 0 until 8
    } yield (salt >>> i * 8) & 0xFF toByte

    numBytes toArray
  }

  /**
   * Hex encoding of the salt value
   *
   * @return String representation of the salt value hex encoded
   */
  def saltToString = saltToBytes.reverse.map(x => f"$x%02X") mkString

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
}

object Credentials {
  val HASH_ALGORITHM = "SHA-256"
  val RND_ALGORITHM = "SHA1PRNG"

  private val md = MessageDigest.getInstance(HASH_ALGORITHM)
  private val base64Encoder = new sun.misc.BASE64Encoder()

  /**
   * Computes the SHA-256 hash of the password and returns it encoded Base-64
   *
   * @param password will be hashed
   * @return the hash of <pre>password</pre> in hex encoding
   */
  def hash(password: String, salt: Long): String = {
    // A brand new instance needs to be obtained here, so that seeding with the salt
    // has the intended effect.  Seeding an existing SecureRandom does not reset it to the
    // desired state
    val secureRnd = SecureRandom.getInstance(RND_ALGORITHM)
    secureRnd.setSeed(salt)
    val passwordBytes = password getBytes
    val saltingBytes: Array[Byte] = Array.ofDim(passwordBytes.length)
    secureRnd.nextBytes(saltingBytes)
    md.reset()
    val xoredBytes = for {
      i <- 0 until passwordBytes.length
      xor = (passwordBytes(i) ^ saltingBytes(i)) toByte
    } yield xor
    base64Encoder.encode(md.digest(xoredBytes toArray))
  }

  /**
   * Converts the byte array into a printable string using hex encoding; the first element of the
   * array is assumed to be lowest order byte; in other words, the value 3135 (0x0C3F) is stored
   * as the array:
   * <pre>
   *   [00] [01] [02] [03]
   *    3F   0C   00   00
   * </pre>
   *
   * @param bytes an array to convert
   * @return the hex representation of the bytes
   */
  def bytesToHexString(bytes: Array[Byte]) = {
    bytes.reverse.map(x => f"$x%02X").mkString
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
    val hashPwd = hash(password, salt)
    new Credentials(username, hashPwd, salt)
  }

  /**
   * A special type of credentials, that are highly insecure: use ONLY for testing purposes (or
   * to create users with default values, for example if using injection)
   *
   * @return an insecure set of credentials
   */
  def emptyCredentials = {
    createCredentials("anonymous", "")
  }
}
