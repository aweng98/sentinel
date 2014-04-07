package com.alertavert.sentinel.persistence.mongodb

import org.scalatest._
import com.alertavert.sentinel.model.User
import org.bson.types.ObjectId
import com.alertavert.sentinel.UnitSpec


class UserDaoTest extends UnitSpec with BeforeAndAfter {

  val dao = UserDao("mongodb:///sentinel-test")

  before {
    dao.userCollection dropCollection()
    assume(dao.userCollection.getCount() == 0, "User collection for tests has not been cleared")
  }

  trait CreatedBy {
    val oid = new ObjectId
  }

  "when connecting to a default mongo, we" should "get a valid connection" in {
    val dbUri = "mongodb://localhost:27017/sentinel_test"
    val dao = UserDao(dbUri)
    assert(dao != null)
  }

  "when saving a valid user, we" should "get a valid OID" in new CreatedBy {
    val user = User.builder("bob", "foo") createdBy(oid) build()
    val uid = dao << user
    assert (uid != null)
  }

  it should "preserve the data" in new CreatedBy {
    val user = User.builder("Dan", "Dude") createdBy(oid) hasCreds("dandude", "abcfedead",
      1234) build()
    val uid = dao << user
    val retrievedUser = dao.find(uid).getOrElse(fail("No user found for the given OID"))
    assert(user.firstName === retrievedUser.firstName)
    assert(user.lastName === retrievedUser.lastName)
    assert(user.getCredentials === retrievedUser.getCredentials)
  }

  "when saving an existing user, we" should "get the same OID" in new CreatedBy {
    val user = User.builder("Joe", "blast") createdBy(oid) build()
    val uid = new ObjectId
    user.setId(uid)
    val newUid = dao << user
    assert (uid === newUid)
    info("changing data preserves the UID")
    user.resetPassword("foobar")
    val anUid = dao << user
    assert(uid === anUid)
  }

  "when saving many users, we" should "get them all back" in {
    val oid = new ObjectId
    val users = List(User.builder("alice") createdBy oid build,
      User.builder("bob") createdBy oid build,
      User.builder("charlie") createdBy oid build)

    users.foreach(dao << _)
    dao.findAll() map(_.firstName) should contain allOf ("alice", "bob", "charlie")
  }
}
