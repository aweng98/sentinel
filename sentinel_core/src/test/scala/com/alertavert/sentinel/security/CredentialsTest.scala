package com.alertavert.sentinel.security

import com.alertavert.sentinel.UnitSpec


class CredentialsTest extends UnitSpec {

  "Hashed passwords" must "match" in {
    val pwd = "azekre7"
    val salt: Long = 987654

    val hashOne = Credentials.hashPwd(pwd, salt)
    val hashTwo = Credentials.hashPwd(pwd, salt)
    assert(hashOne === hashTwo)
  }

  they must "not match, if salt values differ" in {
    val pwd = "don73ll"
    val hashOne = Credentials.hashPwd(pwd, 789)
    val hashTwo = Credentials.hashPwd(pwd, 654)
    assert(hashOne != hashTwo)
  }

  they must "match, regardless of how created" in {
    val credsOne = Credentials.createCredentials("bob", "diddly")
    val hash = Credentials.hashPwd("diddly", credsOne.salt)
    val credsTwo = new Credentials("bob", hash, credsOne.salt)
    assert(credsOne === credsTwo)
  }

  they must "not match, if username differs" in {
    val bobsCreds = Credentials.createCredentials("bob", "123456")
    val jillsCreds = Credentials.createCredentials("jill", "123456")
    assert(bobsCreds != jillsCreds)
  }

  they must "have different salts and API keys, if created independently" in {
    val credsOne = Credentials.createCredentials("Bob", "password")
    val credsTwo = Credentials.createCredentials("Bob", "password")
    assert(credsOne.salt != credsTwo.salt)
    assert(credsOne.apiKey != credsTwo.apiKey)
  }

  "API key" can "be recovered, given the right credentials" in {
    val creds = Credentials.createCredentials("Alice", "foobarwaz")
    val salt = creds.salt

    // To retrieve the API key, I need the three pieces of information: Username, Password & Salt:
    val sameCreds = new Credentials("Alice", Credentials.hashPwd("foobarwaz", salt), salt)
    assert(creds.apiKey == sameCreds.apiKey)
  }

}
