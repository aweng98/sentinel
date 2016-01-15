// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel

import java.io.FileInputStream
import java.util.Properties

import com.alertavert.sentinel.errors.{SentinelException, DbException}
import com.alertavert.sentinel.persistence.DataAccessManager
import com.alertavert.sentinel.persistence.mongodb.MongoUserDao
import com.alertavert.sentinel.security.Credentials
import org.scalatest._

import scala.sys.SystemProperties

abstract class UnitSpec[T] extends FlatSpec with Matchers with OptionValues with
    Inside with Inspectors with BeforeAndAfter with BeforeAndAfterAll {

  val CONFIGURATION_FILE_KEY = "config.file"
  val DEFAULT_APPLICATION_CONF = "conf/tests.conf"

  override def beforeAll() = {
    if (! DataAccessManager.isReady) DataAccessManager.init(dbUri)
  }


  val dbUri: String = {
    // TODO: consider using instead TypeSafe/config library (https://github.com/typesafehub/config)
    // See Issue #111748228 (https://www.pivotaltracker.com/story/show/111748228)
    val confFilePath = System.getProperty(CONFIGURATION_FILE_KEY, DEFAULT_APPLICATION_CONF)
    val prop: Properties = new Properties()
    prop.load(new FileInputStream(confFilePath))

    // Properties retain quotes around values; Configuration don't - however, we need to enclose
    // the URI in quotes, as it contains the `:` character, which is not allowed unquoted in
    // configuration files' values.
    // See TODO above for a good long-term solution.
    if (!prop.containsKey("db_uri")) throw new SentinelException("`db_uri` configuration required")
    prop.getProperty("db_uri").replace("\"", "")
  }

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
