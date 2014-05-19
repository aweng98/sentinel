package com.alertavert.sentinel.persistence.mongodb

import com.mongodb.casbah.Imports._
import com.alertavert.sentinel.model.Resource
import com.alertavert.sentinel.security.Action
import com.alertavert.sentinel.persistence.DataAccessManager
import com.alertavert.sentinel.errors.{NotFoundException, NotAllowedException, DbException}

class MongoResourceDao(val resourceCollection: MongoCollection) extends
    MongoDao[Resource](resourceCollection) with MongoSerializer[Resource] {

  override def serialize(resource: Resource): MongoDBObject = {
    MongoDBObject(
      "owner_id" -> resource.owner.id.getOrElse({
        MongoUserDao() << resource.owner
        resource.owner.id
      }),
      "allowed_actions" -> resource.allowedActions,
      "name" -> resource.name
    )
  }

  override def deserialize(dbObj: MongoDBObject): Resource = {
    val ownerId = dbObj.as[ObjectId]("owner_id")
    val owner = MongoUserDao().find(ownerId).getOrElse(
      throw new NotFoundException(ownerId, "Invalid Owner ID for resource"))
    val name = dbObj.as[String]("name")
    val res = new Resource(name, owner)
    res.allowedActions ++= dbObj.as[List[Action]]("allowed_actions")

    res
  }
}

object MongoResourceDao {
  private val RESOURCE_COLLECTION = "resources"
  private var instance: MongoResourceDao = _

  def apply(): MongoResourceDao = instance match {
    case null =>
      if (DataAccessManager isReady) {
      instance = new MongoResourceDao(DataAccessManager.db(RESOURCE_COLLECTION))
          with IdSerializer[Resource] with CreatorSerializer[Resource]
      } else {
        throw  new DbException("DataAccessManager not initialized; use DataAccessManager.init()")
      }
      instance
    case _ => instance
  }
}
