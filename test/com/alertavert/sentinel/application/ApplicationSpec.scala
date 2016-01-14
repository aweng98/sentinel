package com.alertavert.sentinel.application

import org.scalatestplus.play._
import play.api.Play

class ApplicationSpec extends PlaySpec with OneAppPerSuite {
 "The OneAppPerSuite trait" must {
    "start the FakeApplication" in {
      Play.maybeApplication mustBe Some(app)
    }

    "find a valid configuration" in {
      app.configuration.getString("application.test.guard") mustBe Some("found")
    }

    "provide a valid MongoDB URI" in {
      val dbUri = app.configuration.getString("db_uri")
      dbUri match {
        case None => fail("No MongoDB URI found in configuration")
        case Some(s) => s.startsWith("mongodb://") must be(right = true)
      }
    }
  }
}
