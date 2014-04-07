import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.persistence.mongodb.UserDao
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

val marco = collection.findOneByID(id)

val dao = UserDao.create("mongodb:///sentinel-test")
val found = dao.find(new ObjectId("5342161be4b02e398a38dfbd"))
found match {
  case None => println("None found")
  case Some(user: User) => println(user)
}

