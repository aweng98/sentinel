package com.alertavert.sentinel

import com.alertavert.sentinel.errors.DbException
import com.alertavert.sentinel.persistence.DataAccessManager

import scala.sys.SystemProperties

/**
 * Generic test utilities.
 *
 * This Object encapsulated a set of commong test utilities that can be used
 * in all tests.
 */
object TestUtilities {
  /**
   * The db_uri for the tests must be configured via a Java property; for example:
   *
   * <pre>
   *   sbt test -Dsentinel.test.db_uri="mongodb://my.server:99999/foobar"
   * </pre>
   */
  final val DB_URI_PROPERTY = "sentinel.test.db_uri"

  val dbUri = (new SystemProperties).getOrElse(DB_URI_PROPERTY,
    throw new DbException(s"Java System property $DB_URI_PROPERTY not defined"))

  /**
   * If the [[DB_URI_PROPERTY]] is defined in the JVM, we assume that this is a test run.
   *
   * @return  ``true`` if the property is defined
   */
  def isTestRun = (new SystemProperties).get(DB_URI_PROPERTY).isDefined

  /**
   * Initializes the [[DataAccessManager]] if not already initialized, with the
   * URI that was provided via the [[DB_URI_PROPERTY]] system property.
   */
  def initDataManagerForTests() = {
    if (! DataAccessManager.isReady) DataAccessManager.init(dbUri)
  }
}
