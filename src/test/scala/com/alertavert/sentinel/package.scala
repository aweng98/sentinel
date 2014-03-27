package com.alertavert

import org.scalatest._

/**
 * Base class for all unit tests
 */
package object sentinel {

  abstract class UnitSpec extends FlatSpec with Matchers with OptionValues with Inside with Inspectors

}
