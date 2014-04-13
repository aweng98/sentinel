package com.alertavert

import org.scalatest._
import com.alertavert.sentinel.persistence.mongodb.MongoDao
import com.alertavert.sentinel.persistence.DataAccessManager

/**
 * Base class for all unit tests
 */
package object sentinel {

  abstract class UnitSpec[T] extends FlatSpec with Matchers with OptionValues with
      Inside with Inspectors with BeforeAndAfter
}
