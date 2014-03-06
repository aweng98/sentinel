package com.alertavert.sentinel.model

import org.scalatest.FunSuite
import com.alertavert.sentinel.security.Credentials

/**
 * Created by marco on 3/5/14.
 */
class UserTest extends FunSuite {

  test("I can create a user") {
    val user = User.builder("marco").build()
    assert(user != null)
    println(user)
  }

  test("Creds can be assigned to a user") {
    val creds = Credentials.createCredentials("anUser", "andHisPwd")
    val aUser = User.builder("An", "User").hasCreds(creds.username, creds.hashedPassword, creds.salt).isActive.build
    assert(aUser != null)
    assert(aUser.checkCredentials(creds))
    assert(aUser.isActive)
  }
}
