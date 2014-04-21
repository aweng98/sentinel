package com.alertavert.sentinel.persistence.mongodb

import com.alertavert.sentinel.persistence.HasCreator
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import java.util.Date

/**
 * Created by marco on 4/20/14.
 */
trait CreatorSerializer[E <: HasCreator] extends MongoSerializer[E] {

  abstract override def serialize(createdItem: E): MongoDBObject = {
    val dbObj = super.serialize(createdItem)
    dbObj += ("created_by" -> createdItem.createdBy,
      "created_at" -> createdItem.createdAt)
  }

  abstract override def deserialize(dbObj: MongoDBObject): E = {
    val item = super.deserialize(dbObj)
    if (dbObj.contains("created_by"))
      item.createdBy = dbObj.as[ObjectId] ("created_by")
    if (dbObj.contains("created_at"))
      item.createdAt = dbObj.as[Date] ("created_at")
    item
  }
}
