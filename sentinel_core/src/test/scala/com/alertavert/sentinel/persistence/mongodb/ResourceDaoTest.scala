package com.alertavert.sentinel.persistence.mongodb

import com.alertavert.sentinel.UnitSpec
import org.scalatest.BeforeAndAfter
import com.alertavert.sentinel.persistence.DAO
import com.alertavert.sentinel.model.{User, Resource}
import org.bson.types.ObjectId
import com.alertavert.sentinel.security.{View, Edit}

class ResourceDaoTest extends UnitSpec with BeforeAndAfter {

  var resourceDao: DAO[Resource] = _

  before {
    resourceDao = MongoResourceDao()
    val coll = resourceDao.asInstanceOf[MongoResourceDao].collection
    resourceDao.clear()
    assume(coll.count() == 0, "Resource Collection should be empty prior to running tests")
  }

  def creds = getNewCreds

  trait CreatedByOrdinaryUser {
    val creator = User.builder("Joe", "Schmoe") withId new ObjectId  build()
    val creatorId = MongoUserDao() << creator
  }

  trait ASimpleResource extends CreatedByOrdinaryUser {
    val res = new Resource("simple", creator)
    res.allowedActions ++= Set(Edit(), View())
  }

  "A Resource" can "be saved" in new CreatedByOrdinaryUser {
    val myRes = new Resource("saveme", User.builder("marco") withId new ObjectId build())
    myRes.createdBy = Some(creator)
    myRes.allowedActions += Edit()
    val resId = resourceDao << myRes
    assert(myRes.id != None)
    assert(resId === myRes.id.get)
  }

  it can "be retrieved" in new ASimpleResource {
    val id = resourceDao << res
    val actual = resourceDao.find(id).getOrElse(fail(s"Resource with $id cannot be found"))
    assert(res === actual)
  }

  it can "have its owner changed" in new ASimpleResource {
    // let's first test that the default owner == creator holds:
    val resId = resourceDao << res

    // not let's retrieve it from the DB ...
    val foundRes = resourceDao.find(resId) getOrElse fail("Cannot retrieve the resource just saved")
    assert(foundRes === res)
    val newOwner = User.builder("Bob", "Builder") build()
    // ... assign a new owner and save
    foundRes.owner = newOwner
    val sameId = resourceDao << foundRes
    assert(sameId === resId)

    // finally, let's get it back (again) and check the owner
    val sameRes = resourceDao.find(sameId) getOrElse fail("Cannot find after changing owner")
    sameRes should be (foundRes)
    sameRes.owner should be (newOwner)
  }
}
