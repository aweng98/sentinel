package com.alertavert.sentinel

import java.security.{SecureRandom, MessageDigest}

/**
 * Security utilities
 *
 * @author Marco Massenzio (marco@alertavert.com)
 * @since 1.0
 */
package object security {

  /** We always use this hash algorithm (currently, `SHA-256` */
  val HASH_ALGORITHM = "SHA-256"

  val RND_ALGORITHM = "SHA1PRNG"

  /** The default length of the seeding byte array */
  val RND_SEED_LEN: Int = 64

  // TODO: make these only accessible within this package; and probably only via functions
  val md = MessageDigest.getInstance(HASH_ALGORITHM)
  val base64Encoder = new sun.misc.BASE64Encoder
  val base64Decoder = new sun.misc.BASE64Decoder

  def hash(values: Seq[Byte]): Seq[Byte] = {
    md.reset()
    md.update(values.toArray)
    md.digest.toList
  }

  def hash(msg: String): Seq[Byte] = hash(msg getBytes)

  def hashStrings(values: Seq[String]) = hash(values.mkString)

  /**
   * Generates a secure sequence of random bytes; however, the sequence is deterministic
   * based on the value of the `salt`.
   *
   * @param seed an initializer seed
   * @param size the number of bytes to generate, by default [[RND_SEED_LEN]]
   * @return a random [[scala.collection.immutable.List]] of [[Byte]]s
   */
  def makeRnd(seed: Long, size: Int = RND_SEED_LEN): Seq[Byte] = {
    // A brand new instance needs to be obtained here, so that seeding with the salt
    // has the intended effect.  Seeding an existing SecureRandom does not reset it to the
    // desired state
    val secureRnd = SecureRandom.getInstance(RND_ALGORITHM)
    val buf: Array[Byte] = Array.ofDim(size)

    secureRnd.setSeed(seed)
    secureRnd.nextBytes(buf)
    buf.toSeq
  }
}
