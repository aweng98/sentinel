package com.alertavert.sentinel.persistence.mongodb

import language.postfixOps
import com.alertavert.sentinel.model.Organization
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCollection
import org.bson.types.ObjectId
import java.util.Date
import com.alertavert.sentinel.persistence.DataAccessManager

class MongoOrganizationDao(val orgsCollection: MongoCollection) extends
    MongoDao[Organization](orgsCollection) with MongoSerializer[Organization] {

  override def serialize(org: Organization) = MongoDBObject(
    "_id" -> (org.id match {
      case None => null
      case Some(x: ObjectId) => x
    }),
    "name" -> org.name,
    "active" -> org.active,
    "created" -> org.created,
    "created_by" -> org.created_by.id
  )

  override def deserialize(item: MongoDBObject) = {
    (Organization.builder(item.as[String]("name"))
      withId item.as[ObjectId]("_id")
      setActive item.as[Boolean]("active")
      created item.as[Date]("created")
      createdBy MongoUserDao().find(item.as[ObjectId]("created_by")).get) build
  }
}

object MongoOrganizationDao {
  private val ORGS_COLLECTION = "organizations"
  private var instance: MongoOrganizationDao = _

  def apply(): MongoOrganizationDao = instance match {
    case null => if (DataAccessManager isReady) instance = new MongoOrganizationDao(
      DataAccessManager.db(ORGS_COLLECTION)); instance
    case _ => instance
  }
}
