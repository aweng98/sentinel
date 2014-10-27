// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

import com.alertavert.sentinel.persistence.DataAccessManager
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.mongodb.{BasicDBList, DBObject}
import org.bson.types.ObjectId
import scala.language.implicitConversions
import com.mongodb.casbah.Implicits._
DataAccessManager.init("mongodb:///sentinel-test")
val coll = DataAccessManager.db("user_orgs")
val res = coll.findOne(MongoDBObject("user_id" -> new ObjectId("543cc97ae4b08b0846f4c1bf")))
val orgs:MongoDBList = res.get.get("organizations").asInstanceOf[BasicDBList]
implicit def map(list: BasicDBList): List[DBObject] = {
  val res: scala.collection.mutable.MutableList[DBObject] = collection.mutable.MutableList()
  val it = list.iterator()
  while (it.hasNext) {
    res += it.next().asInstanceOf[DBObject]
  }
  res.toList
}

orgs.getClass
//orgs.foreach(x => println(s"For Org: ${x.a("org_id")} is a ${x.get("role")}"))

//val oneRole = orgs.get(0).asInstanceOf[DBObject]
//
//val orgRole = (oneRole.get("org_id").asInstanceOf[ObjectId],
//  oneRole.get("role").asInstanceOf[String])
//
//val or = MongoOrganizationDao().find(orgRole._1)

val mm = Map("foo" -> 1, "bar" -> 2, "baz" -> 3)

val ss = List(1, 2, 3)
val foo: Seq[(String, Int)] = mm.toSeq

