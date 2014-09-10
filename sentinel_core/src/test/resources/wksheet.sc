/**
 * Worksheet for the model classes
 *
 * Created by marco on 2/10/14.
 */
import com.alertavert.sentinel.model._
import com.alertavert.sentinel.security.{Edit, Grant, Action, Credentials}
import java.security.{SecureRandom, MessageDigest}

import org.bson.types.ObjectId

def saltToBytes(salt: Long): Array[Byte] = {
  val numBytes = for {
    i <- 0 until 8
  } yield (salt >>> i * 8) & 0xFF toByte

  numBytes toArray
}

def bytesToHexString(bytes: Array[Byte]) = {
  bytes.reverse.map(x => f"$x%02X").mkString
}
// 0x00AF = 175
bytesToHexString(saltToBytes(175))

val builder = User.builder("marco")

val marco = builder.withId(new ObjectId("53fd79f5e4b0be5b2144c836"))
                   .hasCreds("marco", "foobaz", 3345)
                   .isActive
                   .build()

println(marco)
val md = MessageDigest.getInstance("SHA-256")
val digest = md.digest(saltToBytes(175).toArray)
bytesToHexString(digest)
// hashed pwd was 'zekret' and seed 175
val creds = new Credentials("marc", Credentials.hash("zekret", 175), 175)

creds.saltToString
creds.hashedPassword
creds.apiKey

md.reset()
md.update("bar" getBytes)
md.update("foo" getBytes)
md.update("bas" getBytes)
md.update("qos" getBytes)
val a = md.digest()

val words = List("bar", "foo", "bas", "qos")
md.reset()
words.foreach(w => md.update(w getBytes))
val b = md.digest()

assert(a == b)

def makeRnd(seed: Long, size: Int = 16) = {
  val secureRnd = SecureRandom.getInstance("SHA1PRNG")
  secureRnd.setSeed(seed)
  val buf: Array[Byte] = Array.ofDim(size)
  secureRnd.nextBytes(buf)
  buf.toList
}
// Using the same seed generates the same sequence of random bytes:
assert(makeRnd(9876543) == makeRnd(9876543))
