package com.alertavert.sentinel.persistence.mongodb

import com.alertavert.sentinel.persistence.HasCreator
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import java.util.Date
import com.alertavert.sentinel.errors.NotFoundException
import com.alertavert.sentinel.model.User

trait CreatorSerializer[E <: HasCreator] extends MongoSerializer[E] {

  abstract override def serialize(createdItem: E): MongoDBObject = {
    val dbObj = super.serialize(createdItem)
    createdItem.createdBy match {
      case Some(x) => dbObj += "created_by" -> x.id
      case None =>
    }
    dbObj += "created_at" -> createdItem.createdAt
  }

  abstract override def deserialize(dbObj: MongoDBObject): E = {
    val item = super.deserialize(dbObj)
    if (dbObj.contains("created_by")) {
      val user_id = dbObj.as[ObjectId]("created_by")
      val dao = MongoUserDao()
      item.createdBy = dao.find(user_id)
    }
    if (dbObj.contains("created_at"))
      item.createdAt = dbObj.as[Date] ("created_at")
    item
  }
}
