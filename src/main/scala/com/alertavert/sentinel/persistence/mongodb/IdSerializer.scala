package com.alertavert.sentinel.persistence.mongodb

import com.mongodb.casbah.Imports.MongoDBObject
import com.alertavert.sentinel.persistence.HasId

trait IdSerializer[T <: HasId] extends MongoSerializer[T] {
  abstract override def serialize(item: T): MongoDBObject = {
    val dbObj = super.serialize(item)
    item.id match {
      case None =>
      case Some(id) => dbObj += "_id" -> id
    }
    dbObj
  }

  abstract override def deserialize(dbObj: MongoDBObject): T = {
    val item = super.deserialize(dbObj)
    item.setId(dbObj._id.getOrElse(null))
    item
  }
}
