/**
 * Worksheet for the model classes
 *
 * Created by marco on 2/10/14.
 */
import com.alertavert.sentinel.model._
import com.alertavert.sentinel.security.{Edit, Grant, Action, Credentials}
import java.security.MessageDigest

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
                   .setActive()
                   .build()

println(marco)
val md = MessageDigest.getInstance("SHA-256")
val digest = md.digest(saltToBytes(175).toArray)
bytesToHexString(digest)
// hashed pwd was 'zekret' and seed 175
val creds = new Credentials("marc", Credentials.hashPwd("zekret", 175), 175)

creds.saltToString
creds.hashedPassword
creds.apiKey
// JSON serializers
import play.api.libs.json._

implicit val credsWrites = new Writes[Credentials] {
  def writes(creds: Credentials) = Json.obj(
    "username" -> creds.username,
    "api_key" -> creds.apiKey
  )
}

implicit val userWrites = new Writes[User] {
  val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss zzz")
  def writes(user: User) = Json.obj(
    "first_name" -> user.firstName,
    "last_name" -> user.lastName,
    "active" -> user.isActive,
    "last_seen" -> format.format(user.lastSeen),
    "credentials" -> Json.toJson(user.getCredentials)
  )
}

val json = Json.toJson(marco)

val myDict = Map(
  "foo" -> "33",
  "bar" -> "baz"
)

Json.toJson(myDict)

val jsArr = """[{"id": 2, "name": "Joe"}, {"id": 3, "name": "Bob"}]"""
val jsonRepr = Json.toJson(jsArr)
def activity(day: String) {
  day match {
    case "Mon" => print ("work...")
    case "Fri" => print("Drink!")
    case _ => print("more work...")
  }
}

List("Mon", "Tue", "Fri").foreach { activity }

val dud = None

dud.map(x => s"$x is now").filter(_ startsWith "foo").map(_.toUpperCase)
