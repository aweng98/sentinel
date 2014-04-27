package com.alertavert.sentinel.persistence.mongodb

import org.scalatest._
import com.alertavert.sentinel.model.{User, Organization}
import org.bson.types.ObjectId
import com.alertavert.sentinel.persistence.{DataAccessManager, DAO}

abstract class UnitSpec extends FlatSpec with Matchers with OptionValues with
Inside with Inspectors with BeforeAndAfter


class OrganizationDaoTest extends UnitSpec with BeforeAndAfter {

  var dao: DAO[Organization] = _

  trait OrgCreator {
    val admin = User.builder("admin") build()
    val userDao = MongoUserDao()
    admin.setId(userDao << admin)
  }

  before {
    DataAccessManager.init("mongodb:///orgs-test")
    dao = MongoOrganizationDao()
    dao.asInstanceOf[MongoOrganizationDao].collection.drop()
    assume(dao.asInstanceOf[MongoOrganizationDao].collection.count() == 0, "Collection should be empty " +
      "prior to running tests")
  }

  "Organizations" can "be saved in Mongo" in new OrgCreator {
    val acme = (Organization.builder("Acme Inc.")
      createdBy admin
      build)
    val id = dao.upsert(acme)
    assert(id != null)
    assert(! id.toString.isEmpty)
  }

  they can "be retrieved" in new OrgCreator {
    val acme = (Organization.builder("New Acme Inc.")
      createdBy admin
      build)
    val id = dao << acme
    assert(id != null)
    val acme_reborn = dao.find(id)
    assert(acme_reborn != None)
    // This is necessary to ensure equality, all else being equal too:
    acme.setId(id)
    assert(acme_reborn.get === acme)
  }
}
