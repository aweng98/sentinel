// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

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
    md.digest.toSeq
  }

  /**
   * Hashes a sequence of strings.
   *
   * Use `encode` to obtain the base-64 representation of the hash
   *
   * @param values a sequence of strings that will all, in order, be hashed
   * @return the generated hash, as a sequence of bytes
   */
  def hashStrings(values: Seq[String]) = hash(values.mkString getBytes)

  /**
   * Generates a secure sequence of random bytes; however, the sequence is deterministic
   * based on the value of the `seed`.
   *
   * @param seed an initializer seed
   * @param size the number of bytes to generate, by default [[RND_SEED_LEN]]
   * @return a random [[List]] of [[Byte]]s
   */
  def makeRnd(seed: Long, size: Int = RND_SEED_LEN): Seq[Byte] = {
    if (size < 2) throw new IllegalArgumentException("A random sequence must be at least 2 bytes " +
      "in length")
    // A brand new instance needs to be obtained here, so that seeding with the salt
    // has the intended effect.  Seeding an existing SecureRandom does not reset it to the
    // desired state
    val secureRnd = SecureRandom.getInstance(RND_ALGORITHM)
    val buf: Array[Byte] = Array.ofDim(size)

    secureRnd.setSeed(seed)
    secureRnd.nextBytes(buf)
    buf.toSeq
  }

  /**
   * Encode an array of bytes into a BASE64 string
   *
   * @param bytes the bytes to encode
   * @return a BASE64-encoded string
   */
  def encode(bytes: Seq[Byte]) = base64Encoder.encode(bytes.toArray)

  /**
   * Decode a base-64 string into its component bytes
   *
   * @param msg the encoded string
   * @return the decoded bytes
   */
  def decode(msg: String) = base64Decoder.decodeBuffer(msg).toSeq
}
