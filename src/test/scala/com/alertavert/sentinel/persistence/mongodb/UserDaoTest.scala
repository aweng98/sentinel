package com.alertavert.sentinel.persistence.mongodb

import org.scalatest._
import com.alertavert.sentinel.model.User
import org.bson.types.ObjectId
import com.alertavert.sentinel.persistence.{DAO, DataAccessManager}


class UserDaoTest extends UnitSpec with BeforeAndAfter {

  var dao: DAO[User] = _

  before {
    DataAccessManager.init("mongodb:///user-test")
    dao = MongoUserDao()
    dao.asInstanceOf[MongoUserDao].collection.drop()
    assume(dao.asInstanceOf[MongoUserDao].collection.count() == 0, "Collection should be empty " +
      "prior to running tests")
  }

  trait CreatedByAdminUser {
    val adminUser = User.builder("admin") build
    val adminId = dao << adminUser
  }

  trait CreatedByOrdinaryUser extends CreatedByAdminUser {
    val creator = User.builder("Joe", "Schmoe") withId(new ObjectId) createdBy(adminUser) build
    val creatorId = dao << creator
  }

  "when connecting to a default mongo, we" should "get a valid connection" in {
    DataAccessManager.init("mongodb://localhost:27017/sentinel_test")
    val dao = MongoUserDao()
    assert(dao != null)
  }

  "when saving a valid user, we" should "get a valid OID" in new CreatedByOrdinaryUser {
    val user = User.builder("bob", "foo") createdBy creator build()
    val uid = dao << user
    assert (uid != null)
  }

  it should "preserve the data" in new CreatedByOrdinaryUser {
    val user = User.builder("Dan", "Dude") createdBy creator hasCreds("dandude", "abcfedead",
      1234) build()
    val uid = dao << user
    val retrievedUser = dao.find(uid).getOrElse(fail("No user found for the given OID"))
    assert(user.firstName === retrievedUser.firstName)
    assert(user.lastName === retrievedUser.lastName)
    assert(user.getCredentials === retrievedUser.getCredentials)
  }

  it should "get the same ID, if previously set" in new CreatedByOrdinaryUser {
    val user = User.builder("Joe", "blast") createdBy creator build()
    val uid = new ObjectId
    user.setId(uid)
    val newUid = dao << user
    assert (uid === newUid)
    info("changing data preserves the UID")
    user.resetPassword("foobar")
    val anUid = dao << user
    assert(uid === anUid)
  }

  it should "have the creators' chain preserved" in new CreatedByOrdinaryUser {
    val bob = User.builder("bob") createdBy creator build
    val bobId = dao << bob

    val bobAgain = dao.find(bobId).getOrElse(fail("Could not retrieve a valid user (Bob)"))
    assert(bob === bobAgain)
    assert(creator === bobAgain.createdBy.getOrElse(fail("No creator for bobAgain")))
    val admin = bob.createdBy.get.createdBy.getOrElse(fail("No Admin creator for Joe"))
    assert(adminUser === admin)
  }

  "when saving many users, they" should "be found again" in new CreatedByOrdinaryUser {
    val users = List(User.builder("alice") createdBy creator build(),
      User.builder("bob") createdBy creator build(),
      User.builder("charlie") createdBy creator build())

    users.foreach(dao << _)
    dao.findAll() map(_.firstName) should contain allOf ("alice", "bob", "charlie")
  }
}
