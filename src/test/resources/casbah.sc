import com.alertavert.sentinel.persistence.DAO
import com.alertavert.sentinel.persistence.mongodb.ActionSerializer
import com.alertavert.sentinel.security.{Create, Delete}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.Imports.ObjectId
import org.clapper.classutil.ClassFinder
val mongoClient = MongoClient()
val db = mongoClient("test")

new ActionSerializer {} register
val collection = db("users")
val id = new ObjectId("5316a20e3004e8c205e8842f")
val permissions = Set(Create(), Delete())
val user = MongoDBObject("_id" -> id,
  "name" -> "Marco",
  "password" -> "zekre7",
  "permissions" -> permissions)
collection.save(user)
val users = for {
  u <- collection.find()
} yield new MongoDBObject(u)
users.foreach(x => println(x._id))



val marco = collection.findOneByID(id).getOrElse("None found")
println(marco)



































