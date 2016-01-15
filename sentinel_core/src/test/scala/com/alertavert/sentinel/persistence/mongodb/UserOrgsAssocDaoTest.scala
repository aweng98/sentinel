package com.alertavert.sentinel.persistence.mongodb

import com.alertavert.sentinel.UnitSpec
import com.alertavert.sentinel.errors.NotFoundException
import com.alertavert.sentinel.model.{Organization, User}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Implicits._
import org.bson.types.ObjectId
import org.scalatest.BeforeAndAfter

/**
 * Tests Users/Organizations associations.
 *
 * Created by marco on 10/13/14.
 */
class UserOrgsAssocDaoTest extends UnitSpec {

  var dao: UserOrgsAssocDao = _

  before {
    dao = UserOrgsAssocDao()
    dao.collection.dropCollection()
    assume(dao.collection.count() == 0,
           "Association collections User-Orgs should be empty prior to test")
  }

  trait WithUserAndOrgs {
    val userDao = MongoUserDao()
    val orgsDao = MongoOrganizationDao()

    val aUser = User.builder("bob") withId new ObjectId hasCreds getNewCreds setActive true build()
    userDao << aUser

    val orgs = List(Organization.builder("AnOrg") withId new ObjectId build,
      Organization.builder("AnotherOrg") withId new ObjectId build,
      Organization.builder("wowOrg") withId new ObjectId build
    )
    orgs.foreach(orgsDao << _)
    val orgsRoles = orgs.zipAll(Nil, Organization.EmptyOrg, "user")
  }

  "When associating a User to multiple orgs, it" should "be saved to DB" in new WithUserAndOrgs {
    dao.associate(aUser, orgsRoles)
    val orgsResult = dao.getByUserid(aUser.id.get).organizations.keys
    orgsResult should contain theSameElementsAs(orgs)
  }

  // Tests for the inner UserOrgAssociation

  "A UserOrgAssociation" can "be built easily" in new WithUserAndOrgs {
    val dbObj = MongoDBObject(
      "user_id" -> aUser.id.get,
      "organizations" -> List(
        MongoDBObject("org_id" -> orgs(0).id.get, "role" -> "chief"),
        MongoDBObject("org_id" -> orgs(1).id.get, "role" -> "designer")
      )
    )
    val actual = dao.UserOrgAssociation.fromDbObject(dbObj)
    actual.user should equal (aUser)
    actual.organizations should have size 2
    actual.organizations.values should contain allOf ("chief", "designer")
  }

  it should "throw with non-existent users" in intercept[NotFoundException] {
  val dbObj = MongoDBObject(
      "user_id" -> new ObjectId,
      "organizations" -> List(
        MongoDBObject("org_id" -> new ObjectId, "role" -> "chief"),
        MongoDBObject("org_id" -> new ObjectId, "role" -> "designer")
      )
    )
    val willFail = dao.UserOrgAssociation.fromDbObject(dbObj)
  }

 }
