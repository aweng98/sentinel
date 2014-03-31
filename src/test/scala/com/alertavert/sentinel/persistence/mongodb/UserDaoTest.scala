package com.alertavert.sentinel.persistence.mongodb

import org.scalatest._
import com.alertavert.sentinel.model.User
import org.bson.types.ObjectId

abstract class UnitSpec extends FlatSpec with Matchers with OptionValues with Inside with Inspectors


class UserDaoTest extends UnitSpec with BeforeAndAfter {

  val dao = UserDao.create("mongodb:///sentinel-test")

  before {
    assume(dao.userCollection.getCount() == 0, "User collection for tests has not been cleared")
  }

  after {
    dao.userCollection dropCollection
  }

  trait CreatedBy {
    val oid = new ObjectId
  }

  "when connectiong to a default mongo, we" should "get a valid connection" in {
    val dbUri = "mongodb://localhost:27017/sentinel_test"
    val dao = UserDao.create(dbUri)
    assert(dao != null)
  }

  "when saving a valid user, we" should "get a valid OID" in new CreatedBy {
    val user = User.builder("bob", "foo") createdBy(oid) build()
    val uid = dao << user
    assert (uid != null)
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
