// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.security

import com.alertavert.sentinel.UnitSpec

import scala.collection.GenMap

/**
 * Unit tests for the security utility methods
 *
 * Created by marco on 9/14/14.
 */
class SecurityUtilsTest extends UnitSpec {

  "Secure randoms" must "match, if using the same seed" in {
    val MIN = 998450000L
    val MAX = 998459999L

    // We build first the mapping to ensure that the random generator does not retain 'memory'
    val pairs = for {
        x <- MIN to MAX
    } yield (x, makeRnd(x))
    val mappedRnds = scala.collection.mutable.Map[Long, Seq[Byte]]()
    pairs.foreach( mappedRnds += _ )

    // gets a random (not secure) integer in the range [MIN, MAX]
    def getRnd = Math.round(MIN + Math.random() * (MAX - MIN))

    // then, for a large (but smaller than the total population) we verify that we get the same
    // results
    for(x <- 0 until 3000) {
      val num = getRnd
      assertResult(mappedRnds.getOrElse(num, fail("Could not find mapping"))) { makeRnd(num) }
    }
  }

  they must "have the correct length" in {
    assert(32 === makeRnd(123456, 32).length)
    assert(RND_SEED_LEN == makeRnd(985632).length)
    assert(256 === makeRnd(99, 256).length)
  }

  they must "reject unreasonable arguments" in {
    intercept[IllegalArgumentException](makeRnd(9876, 0))
    intercept[IllegalArgumentException](makeRnd(9876, -1))
    intercept[IllegalArgumentException](makeRnd(9876, -98765))
  }
}
