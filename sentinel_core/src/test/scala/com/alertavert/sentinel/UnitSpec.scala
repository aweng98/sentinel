package com.alertavert.sentinel

import org.scalatest._
import com.alertavert.sentinel.persistence.DataAccessManager

abstract class UnitSpec[T] extends FlatSpec with Matchers with OptionValues with
    Inside with Inspectors with BeforeAndAfter {

  if (! DataAccessManager.isReady) DataAccessManager.init("mongodb://localhost:27017/sentinel-test")

}
