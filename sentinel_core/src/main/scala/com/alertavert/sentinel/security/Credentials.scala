package com.alertavert.sentinel.security

//import language.postfixOps
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
  def apiKey = base64Encoder.encode(_apiKey)

  /**
   * Creates an API key from the hashed password and salt
   *
   * @return a pseudo-random API key in Base64 encoding
   */
  private def createApiKey() = {
    md.reset()
    // TODO(marco): add username to the hash
    md.update(hashedPassword getBytes)
    md update saltToBytes
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
  def saltToString = base64Encoder.encode(saltToBytes)

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

  /**
   * Generates a secure sequence of random bytes; however, the sequence is deterministic
   * based on the value of the `salt`.
   *
   * @param seed an initializer seed
   * @param size the number of bytes to generate, by default [[RND_SEED_LEN]]
   * @return a random [[scala.collection.immutable.List]] of [[Byte]]s
   */
  def makeRnd(seed: Long, size: Int = RND_SEED_LEN): List[Byte] = {
    // A brand new instance needs to be obtained here, so that seeding with the salt
    // has the intended effect.  Seeding an existing SecureRandom does not reset it to the
    // desired state
    val secureRnd = SecureRandom.getInstance("SHA1PRNG")
    val buf: Array[Byte] = Array.ofDim(size)

    secureRnd.setSeed(seed)
    secureRnd.nextBytes(buf)
    buf.toList
  }

  /**
   * Computes the SHA-256 hash of the password and returns it encoded Base-64
   *
   * @param password in plaintext, it will be hashed
   * @param salt a seed that makes hashed passwords different among users,
   *             even if they choose the same password (as users do)
   * @return the hash of `password` in `base64` encoding
   */
  def hash(password: String, salt: Long): String = {
    val saltingBytes = makeRnd(salt)

    md.reset()
    md.update(saltingBytes.toArray)
    md.update(password getBytes)
    base64Encoder.encode(md.digest())
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
    createCredentials("anon", "secret")
  }
}
