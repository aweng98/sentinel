package com.alertavert.sentinel.persistence.mongodb

import com.alertavert.sentinel.UnitSpec
import org.scalatest.BeforeAndAfter
import com.alertavert.sentinel.persistence.{DataAccessManager, DAO}
import com.alertavert.sentinel.model.{User, Resource}
import org.bson.types.ObjectId
import com.alertavert.sentinel.security.{View, Edit}

class MongoResourceDaoTest extends UnitSpec with BeforeAndAfter {

  var dao: DAO[Resource] = _

  before {
    dao = MongoResourceDao()
    dao.asInstanceOf[MongoResourceDao].collection.drop()
  }

  trait CreatedByOrdinaryUser {
    val creator = User.builder("Joe", "Schmoe") withId(new ObjectId) build
    val creatorId = MongoUserDao() << creator
  }

  trait ASimpleResource extends CreatedByOrdinaryUser {
    val res = new Resource(creator)
    res.allowedActions ++= Set(Edit(), View())
  }

  "A Resource" can "be saved" in new CreatedByOrdinaryUser {
    val myRes = new Resource(User.builder("marco") withId(new ObjectId) build())
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
}
