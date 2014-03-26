/**
 * Created by marco on 3/4/14.
 */

import com.mongodb.casbah.commons.MongoDBObject







import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.Imports.ObjectId


val mongoClient = MongoClient()
val db = mongoClient("test")
val collection = db("users")

val id = new ObjectId("5316a20e3004e8c205e8842f")
val user = MongoDBObject("_id" -> id,
  "name" -> "Marco",
  "password" -> "zekre7")
collection.save(user)

val users = for {
  u <- collection.find()
} yield new MongoDBObject(u)

users.foreach(x => println(x._id))


















