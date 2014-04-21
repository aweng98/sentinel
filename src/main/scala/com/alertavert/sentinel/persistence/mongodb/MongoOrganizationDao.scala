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
    "name" -> org.name,
    "active" -> org.active
  )

  override def deserialize(item: MongoDBObject) = {
    (Organization.builder(item.as[String]("name"))
      setActive item.as[Boolean]("active")) build
  }
}

object MongoOrganizationDao {
  private val ORGS_COLLECTION = "organizations"
  private var instance: MongoOrganizationDao = _

  def apply(): MongoOrganizationDao = instance match {
    case null => if (DataAccessManager isReady) {
          instance = new MongoOrganizationDao(DataAccessManager.db(ORGS_COLLECTION))
            with IdSerializer[Organization] with CreatorSerializer[Organization]
        }
        instance
    case _ => instance
  }
}
