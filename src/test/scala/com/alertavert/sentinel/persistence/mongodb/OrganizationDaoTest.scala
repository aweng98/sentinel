package com.alertavert.sentinel.persistence.mongodb

import org.scalatest.{BeforeAndAfter, FunSuite}
import com.alertavert.sentinel.UnitSpec
import com.alertavert.sentinel.model.{User, Organization}
import org.bson.types.ObjectId
import com.alertavert.sentinel.persistence.{DataAccessManager, DAO}

class OrganizationDaoTest extends UnitSpec with BeforeAndAfter {

  var dao: DAO[Organization] = _

  before {
    DataAccessManager.init("mongodb:///test")
    dao = MongoOrganizationDao()
    dao.asInstanceOf[MongoOrganizationDao].collection.drop()
    assume(dao.asInstanceOf[MongoOrganizationDao].collection.count() == 0, "Collection should be empty " +
      "prior to running tests")
  }

  "Organizations" can "be saved in Mongo" in {
    val acme = new Organization("Acme Inc.")
    acme.created_by = User.builder("admin").withId(new ObjectId()).build()
    val id = dao.upsert(acme)
    assert(id != null)
    assert(! id.toString.isEmpty)
  }
}
