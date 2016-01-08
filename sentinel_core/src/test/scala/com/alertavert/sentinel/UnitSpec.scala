// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel

import com.alertavert.sentinel.errors.DbException
import com.alertavert.sentinel.persistence.mongodb.MongoUserDao
import com.alertavert.sentinel.security.Credentials
import org.scalatest._
import com.alertavert.sentinel.persistence.DataAccessManager

import scala.sys.SystemProperties

abstract class UnitSpec[T] extends FlatSpec with Matchers with OptionValues with
    Inside with Inspectors with BeforeAndAfter {

  // The db_uri for the tests must be configured via a Java property:
  // sbt test -Dsentinel.test.db_uri="mongodb://my.server:99999/foobar"
  final val DB_URI_PROPERTY = "sentinel.test.db_uri"

  val sp = new SystemProperties
  val dbUri = sp.getOrElse(DB_URI_PROPERTY, throw new DbException(s"Java System property $DB_URI_PROPERTY not defined"))

  if (! DataAccessManager.isReady) DataAccessManager.init(dbUri)

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
