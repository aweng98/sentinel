import com.alertavert.sentinel.model.{Organization, User}
import play.api.libs.json.{JsArray, JsValue, Json}

import scala.io.Source

class Person(val id: Int, name: String) {
  override def toString = name
}

val jsArr =
  """[{"id": 2, "name": "Joe",
    |  "address": {
    |     "street": "775 Pear ave",
    |     "state": {
    |       "name": "CA",
    |       "zip": 4087
    |     }
    |  }
    | },
    | {"id": 3, "name": "Jill"},
    | {"id": 4, "name": "Bob"}]
  """.stripMargin
val jsonRepr = Json.parse(jsArr)
val personsArray = jsonRepr.as[JsArray]
val zips = personsArray.value.map(_ \ "address" \ "state" \ "zip")
val jj = Json.parse("""{"id": 2, "name": "Joe",
    |  "address": {
    |     "street": "775 Pear ave",
    |     "state": {
    |       "name": "CA",
    |       "zip": 4087
    |     }
    |  }
    | }""".stripMargin)
import play.api.libs.json._

implicit object PersonFormat extends Reads[Person] {

  def reads(json: JsValue) = JsSuccess(new Person((json \ "id").as[Int],
    (json \ "name").as[String]))
}
val persons = jsonRepr.as[Seq[Person]]

import models._

val newUser = Json.parse( """{
                            |  "first_name": "Marco",
                            |  "last_name": "Mass",
                            |  "credentials": {
                            |    "username": "marco",
                            |    "password": "secret"
                            |  }
                            |}""".stripMargin)

val user = newUser.as[User]

import play.api.libs.json._
import play.api.libs.functional.syntax._


val json = Json.parse(
  """
    |{
    |  "id":"p1",
    |  "items": [
    |    {"id":"v1","info":{"foo":"fooVal","bar":1234}},
    |    {"id":"v2", "info": {"foo": "fooVal", "bar": 5678}}
    |  ]}
  """.stripMargin)

implicit def tupleReads: Reads[(String, Int)] =
  ((JsPath \ "id").read[String] and (JsPath \ "info" \ "bar").read[Int]).tupled

(json \ "items").as[Seq[(String, Int)]]

val jsnon = Json.parse("""{"foo": 3, "bar": true}""")

try {
  val bar = (jsnon \ "bar2").asOpt[Boolean] match {
    case None => false
    case Some(b) => b
  }
  println(s"This is bar: $bar")

} catch {
  case ex: Exception => println("ex: " + ex.getMessage)
}


val org = Organization.builder("org") build
val org2 = Organization.builder("org2") build
val org3 = Organization.builder("org3") build

val ps = Map(
  org -> "user-1",
  org2 -> "user-2",
  org3 -> "user-3"
)

Json.toJson(ps.map(x => s"""{"org": "${x._1.name}", "role": "${x._2}"}"""))

val ss = Source.fromFile("/tmp/test.json").getLines().mkString
val users = Json.parse(ss).as[List[Map[String, JsValue]]]

users.foreach(user => println(user.get("credentials")))

