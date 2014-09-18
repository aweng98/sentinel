package com.alertavert.sentinel.persistence.mongodb

import org.scalatest._
import com.alertavert.sentinel.model.{Resource, User}
import org.bson.types.ObjectId
import com.alertavert.sentinel.persistence.DAO
import com.alertavert.sentinel.UnitSpec
import com.alertavert.sentinel.security.{Credentials, Grant, Permission}


class UserDaoTest extends UnitSpec with BeforeAndAfter {

  var dao: DAO[User] = _

  before {
    dao = MongoUserDao()
    val coll = dao.asInstanceOf[MongoUserDao].collection
    coll.drop()
    assume(coll.count() == 0, "Collection should be empty prior to running tests")
  }

  // Use the same credentials everywhere, doesn't matter really
  val creds = Credentials("user", "pwd")

  trait CreatedByAdminUser {
    val adminUser = User.builder("admin") hasCreds creds build()
    val adminId = dao << adminUser
  }

  trait CreatedByOrdinaryUser extends CreatedByAdminUser {
    val ordinaryUser = User.builder("Creator", "User") hasCreds creds withId new ObjectId createdBy adminUser build()
    val creatorId = dao << ordinaryUser
  }

  "when saving a valid user, we" should "get a valid OID" in new CreatedByOrdinaryUser {
    val user = User.builder("bob", "foo") createdBy ordinaryUser hasCreds creds build()
    val uid = dao << user
    assert (uid != null)
  }

  it should "preserve the data" in new CreatedByOrdinaryUser {
    val user = User.builder("Dan", "Dude") createdBy ordinaryUser hasCreds("dandude", "abcfedead",
      1234) build()
    val uid = dao << user
    val retrievedUser = dao.find(uid).getOrElse(fail("No user found for the given OID"))
    assert(user.firstName === retrievedUser.firstName)
    assert(user.lastName === retrievedUser.lastName)
    assert(user.getCredentials === retrievedUser.getCredentials)
  }

  it should "get the same ID, if previously set" in new CreatedByOrdinaryUser {
    val user = User.builder("Joe", "blast") createdBy ordinaryUser hasCreds creds build()
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
    val bob = User.builder("bob") createdBy ordinaryUser hasCreds creds build()
    val bobId = dao << bob

    val bobAgain = dao.find(bobId).getOrElse(fail("Could not retrieve a valid user (Bob)"))
    assert(bob === bobAgain)
    assert(ordinaryUser === bobAgain.createdBy.getOrElse(fail("No creator for bobAgain")))
    val admin = bob.createdBy.get.createdBy.getOrElse(fail("No Admin creator"))
    assert(adminUser === admin)
  }

  it can "be saved with permissions set" in new CreatedByOrdinaryUser {
    val resource = new Resource("buzz", ordinaryUser)
    MongoResourceDao() << resource
    Permission.grant(Grant(), resource, ordinaryUser)
    dao << ordinaryUser
  }

  "when saving many users, they" should "be found again" in new CreatedByOrdinaryUser {
    info("Before saving many users:")
    dao.findAll() foreach (u => info(u.toString))
    info("---------------------------")
    val users = List(User.builder("alice") createdBy ordinaryUser hasCreds creds build(),
      User.builder("bob") createdBy ordinaryUser hasCreds creds build(),
      User.builder("charlie") createdBy ordinaryUser hasCreds creds build())

    users.foreach(dao << _)
    dao.findAll() map(_.firstName) should contain allOf ("alice", "bob", "charlie")
    dao.findAll() should have size 5
    info("After saving many users:")
    dao.findAll() foreach (u => info(u.toString))
    info("---------------------------")
  }
}
