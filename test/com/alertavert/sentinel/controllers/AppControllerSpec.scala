// Copyright 2014 (c) AlertAvert.com. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.controllers

import controllers.AppController
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc.{Controller, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
 * Minimalist set of tests against the app correctly serving the UI.
 * Javascript UI is tested separately using Karma/Jasmine frameworks.
 */
class AppControllerSpec extends PlaySpec with Results with OneAppPerSuite {

  class TestController() extends Controller with AppController

  "App server" should {
    "be healthy" in {
      val controller = new TestController
      val result = controller.health().apply(FakeRequest())
      val bodyText = contentAsString(result)
      bodyText mustBe "Ok"
    }

    "return valid status" in {
      val controller = new TestController
      val result = controller.status().apply(FakeRequest())
      val jsonResult = contentAsJson(result)
      jsonResult mustNot be (null)
      (jsonResult \ "status").as[String] must be ("running")
    }

    "be ready and render the UI page" in {
      val testController = new TestController
      val indexResult = testController.index().apply(FakeRequest())
      contentAsString(indexResult) must include("""<a href="/web/index.html">Sentinel UI</a>""")
    }
  }
}
