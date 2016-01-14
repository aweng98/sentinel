// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel

import com.alertavert.sentinel.persistence.mongodb.MongoUserDao
import com.alertavert.sentinel.security.Credentials
import org.scalatest._

abstract class UnitSpec[T] extends FlatSpec with Matchers with OptionValues with
    Inside with Inspectors with BeforeAndAfter {

  TestUtilities.initDataManagerForTests()

  def getNewCreds: Credentials = {
    val suffix = Math.round(1000 * Math.random()) toString
    val prefix = "test-user"
    val newUsername = List(prefix, suffix) mkString "-"
    val dao = MongoUserDao()
    dao.findByName(newUsername) match {
      case None => Credentials(newUsername, "secret")
      case Some(user) => getNewCreds
    }
  }

}
