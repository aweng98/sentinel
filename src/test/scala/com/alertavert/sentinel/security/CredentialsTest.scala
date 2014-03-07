package com.alertavert.sentinel.security

import com.alertavert.sentinel.UnitSpec


class CredentialsTest extends UnitSpec {

  "Hashed passwords" must "match" in {
    val pwd = "azekre7"
    val salt: Long = 987654

    val hashOne = Credentials.hash(pwd, salt)
    val hashTwo = Credentials.hash(pwd, salt)
    assert(hashOne === hashTwo)
  }

  they must "not match, if salt values differ" in {
    val pwd = "don73ll"
    val hashOne = Credentials.hash(pwd, 789)
    val hashTwo = Credentials.hash(pwd, 654)
    assert(hashOne != hashTwo)
  }

  they must "match, regardless of how created" in {
    val credsOne = Credentials.createCredentials("bob", "diddly")
    val hash = Credentials.hash("diddly", credsOne.salt)
    val credsTwo = new Credentials("bob", hash, credsOne.salt)
    assert(credsOne === credsTwo)
  }

  they must "not match, if username differs" in {
    val bobsCreds = Credentials.createCredentials("bob", "123456")
    val jillsCreds = Credentials.createCredentials("jill", "123456")
    assert(bobsCreds != jillsCreds)
  }

}
