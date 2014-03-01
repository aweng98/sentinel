/**
 * Worksheet for the model classes
 *
 * Created by marco on 2/10/14.
 */
import com.alertavert.sentinel.model._
import com.alertavert.sentinel.security.Credentials
import java.security.MessageDigest
import java.util.UUID
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

val marco = builder.withId(UUID.fromString("edef9656-66ee-4550-bd4b-5c81a280fa45"))
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
creds.getApiKey

val grantPerm = Grant.action

println(grantPerm.name)
var perms: Set[Action] = Set()
perms + Edit.action
