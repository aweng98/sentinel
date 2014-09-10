package com.alertavert.sentinel

import java.security.MessageDigest

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
}
