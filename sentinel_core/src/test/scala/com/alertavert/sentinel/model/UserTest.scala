// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.model

import com.alertavert.sentinel.errors.NotAllowedException
import com.mongodb.casbah.Imports.ObjectId

import com.alertavert.sentinel.security.{Permission, Edit, Credentials}
import com.alertavert.sentinel.UnitSpec

/**
 * Unit tests for the User class
 */
class UserTest extends UnitSpec {

  trait UserBuilder {
    val builder = User.builder("Bob")
  }

  trait UserCredentials {
    val creds = Credentials.createCredentials("anUser", "andHisPwd")
  }

  trait AuthenticatedUser extends UserBuilder with UserCredentials {
    val user = builder hasCreds creds build
  }

  "A user" must "fail if created without credentials" in new UserBuilder {
    intercept[NotAllowedException] {
      val user = builder build
    }
  }

  it can "be created with credentials" in new AuthenticatedUser  {
    assert(user != null)
    assert(user.checkCredentials(creds))
    assert(!user.isActive)
  }

  it should "have a string representation" in new AuthenticatedUser {
    val userId = new ObjectId()
    val firstName = user.firstName
    user.setId(userId)
    user.activate
    assertResult(s"[$userId] $firstName  (Active)") {
      user.toString
    }
  }

  it must "fail checks, if credentials are invalid" in new UserBuilder with UserCredentials {
    // the `salt` is not a secret, but can be used as a `challenge` and thus may be known to a hacker
    val salt = creds.salt
    val guessedHashPwd = Credentials.hashPwd("wildGuess", salt)
    val hackerCreds = new Credentials("bob", guessedHashPwd, salt)

    // We would normally retrieve the user from the DB - we'll just create it anew here
    val aUser = builder.hasCreds(creds.username, creds.hashedPassword, creds.salt).
      isActive.build()
    assert(! (aUser checkCredentials hackerCreds))
    assert(aUser checkCredentials creds)
  }

  it can "have no permissions by default" in new AuthenticatedUser  {
    user.perms shouldBe empty
  }

  it can "have permissions added" in new AuthenticatedUser {
    val editableResource = new Resource("notebook", user)
    editableResource.allowedActions += Edit()
    val edit = new Permission(Edit(), editableResource)
    edit.grantTo(user)
    user.perms should have size 1
  }

  it should "be allowed to do stuff, with permission" in new AuthenticatedUser {
    val editableResource = new Resource("foobaz", user)
    editableResource.allowedActions += Edit()
    val edit = new Permission(Edit(), editableResource)
    edit.grantTo(user)

    assert(edit.grantedTo(user))
  }

  it can "be authenticated, given username/password" in new AuthenticatedUser {
    assert(user authenticate("anUser", "andHisPwd"))
  }

  it must "fail authentication, if the password is wrong" in new AuthenticatedUser {
    assert(! user.authenticate("anUser", "foobaz"))
  }

  "users with same username and ID" should "be equal" in {
    val userId = new ObjectId
    val userA = User.builder("joe").withId(userId).hasCreds("joe", "doesntmatter", 999).build()
    val userB = User.builder("bob").withId(userId).hasCreds("joe", "reallydoesnt", 666).build()
    assert(userA === userB)

    // this should work also if no _id has been assigned
    val userC = User.builder("jill").hasCreds("lady", "doesntmatter", 999).build()
    val userD = User.builder("jane").hasCreds("lady", "reallydoesnt", 666).build()
    assert(userC === userD)
  }
}
