package com.alertavert.sentinel.application

import org.scalatestplus.play._
import play.api.Play

class ApplicationSpec extends PlaySpec with OneAppPerSuite {
 "The OneAppPerSuite trait" must {
    "provide a FakeApplication" in {
      app.configuration.getString("db_uri") mustBe Some("mongodb://dockerdev/sentinel-test")
    }
    "start the FakeApplication" in {
      Play.maybeApplication mustBe Some(app)
    }
  }

}
