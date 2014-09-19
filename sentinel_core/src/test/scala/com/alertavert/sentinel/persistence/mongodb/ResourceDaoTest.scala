package com.alertavert.sentinel.persistence.mongodb

import com.alertavert.sentinel.UnitSpec
import com.alertavert.sentinel.model.{Resource, User}
import com.alertavert.sentinel.persistence.DAO
import com.alertavert.sentinel.security.{Credentials, Edit, View}
import org.bson.types.ObjectId
import org.scalatest.BeforeAndAfter

class ResourceDaoTest extends UnitSpec with BeforeAndAfter {

  var dao: DAO[Resource] = _

  before {
    dao = MongoResourceDao()
    val coll = dao.asInstanceOf[MongoResourceDao].collection
    coll.drop()
    assume(coll.count() == 0, "Resource Collection should be empty prior to running tests")
  }

  val creds = Credentials("user", "test")

  trait CreatedByOrdinaryUser {
    val creator = User.builder("Joe", "Schmoe") withId new ObjectId  hasCreds creds build()
    val creatorId = MongoUserDao() << creator
  }

  trait ASimpleResource extends CreatedByOrdinaryUser {
    val res = new Resource("simple", creator)
    res.allowedActions ++= Set(Edit(), View())
  }

  "A Resource" can "be saved" in new CreatedByOrdinaryUser {
    val myRes = new Resource("saveme", User.builder("marco") withId new ObjectId hasCreds creds build())
    myRes.createdBy = Some(creator)
    myRes.allowedActions += Edit()
    val resId = dao << myRes
    assert(myRes.id != None)
    assert(resId === myRes.id.get)
  }

  it can "be retrieved" in new ASimpleResource {
    val id = dao << res
    val actual = dao.find(id).getOrElse(fail(s"Resource with $id cannot be found"))
    assert(res === actual)
  }

  it can "have its owner changed" in new ASimpleResource {
    // let's first test that the default owner == creator holds:
    val resId = dao << res

    // not let's retrieve it from the DB ...
    val foundRes = dao.find(resId) getOrElse fail("Cannot retrieve the resource just saved")
    assert(foundRes === res)
    val newOwner = User.builder("Bob", "Builder") hasCreds creds build()
    // ... assign a new owner and save
    foundRes.owner = newOwner
    val sameId = dao << foundRes
    assert(sameId === resId)

    // finally, let's get it back (again) and check the owner
    val sameRes = dao.find(sameId) getOrElse fail("Cannot find after changing owner")
    sameRes should be (foundRes)
    sameRes.owner should be (newOwner)
  }
}
