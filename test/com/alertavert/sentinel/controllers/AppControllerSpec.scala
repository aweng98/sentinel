// Copyright 2014 (c) AlertAvert.com. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.controllers

import com.alertavert.sentinel.persistence.DataAccessManager
import controllers.AppController
import org.scalatest.{Suite, BeforeAndAfterAll}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc.{Controller, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

trait EnsureDataMgrClosed extends BeforeAndAfterAll { this: Suite =>
  override def afterAll(): Unit = {
    try super.afterAll()
    finally DataAccessManager.close()
  }
}

class AppControllerSpec extends PlaySpec with Results with OneAppPerSuite {

  class TestController() extends Controller with AppController

  "App server" should {
    "should be healthy" in {
      val controller = new TestController
      val result = controller.health().apply(FakeRequest())
      val bodyText = contentAsString(result)
      bodyText mustBe "Ok"
    }

    "should return valid status" in {
      val controller = new TestController
      val result = controller.status().apply(FakeRequest())
      val jsonResult = contentAsJson(result)
      jsonResult mustNot be (null)
      (jsonResult \ "status").as[String] must be ("running")
    }
  }

}
