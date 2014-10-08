// Copyright 2014 (c) AlertAvert.com. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.controllers


import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.persistence.DataAccessManager
import com.alertavert.sentinel.persistence.mongodb.MongoUserDao
import com.alertavert.sentinel.security.Credentials
import controllers.ApiController
import models.UserReads
import org.bson.types.ObjectId
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Controller, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.util.Random

/**
 * <h1>API Tests
 *
 * <p>White-box API tests that verify that the values returned by the [[controllers .A p i C o n t r o l l e r]]
 * match what is expected of them.
 *
 * <p>These are not Integration Tests, as they do not query the API as a black box.
 */
class ApiControllerSpec extends PlaySpec with Results with OneAppPerSuite {

  trait WithControllerAndRequest {
    val testController = new Controller with ApiController

    def fakeRequest(method: String = "GET", route: String = "/") = FakeRequest(method, route)
      .withHeaders(
        ("Date", "2014-10-05T22:00:00"),
        ("Authorization", "username=bob;hash=foobar==")
    )
  }

  "Sentinel API" should {

    "should be ready and render the index page" in new WithControllerAndRequest {
      DataAccessManager.isReady mustBe true
      DataAccessManager.db.getName mustBe "sentinel-test"
      val indexResult = testController.index().apply(fakeRequest())
      contentAsString(indexResult) must include(
        "Sentinel - REST API-driven User Management made easy")
    }

    "should return all users in DB" in new WithControllerAndRequest {
      val dao = MongoUserDao()
      val users = dao.findAll()
      val apiResult = contentAsJson(testController.users.apply(fakeRequest()))
      apiResult mustNot be(null)
      val usersArray = apiResult.as[JsArray]
      val usersResponse = usersArray.value.map(v => v.as[User])
      users.foreach(usersResponse must contain(_))
    }

    "should create a new user" in new WithControllerAndRequest {
      // Avoid conflicts with existing usernames (if run multiple times before the collection
      // gets wiped out by the next set of DAO tests)
      val rnd = Random.nextInt(1000)
      val request = fakeRequest("POST", "/user").withJsonBody(Json.parse(
        s"""{"first_name": "Marco",
          |  "last_name": "Mass",
          |  "credentials": {
          |    "username": "marco_$rnd",
          |    "password": "secret"
          |  }
          |}""".stripMargin))
      val apiResult = call(testController.createUser, request)
      status(apiResult) mustEqual CREATED
      val jsonResult = contentAsJson(apiResult)
      ObjectId.isValid((jsonResult \ "id").as[String]) mustBe true

      // now get the real thing from the DB and check it was created with the correct values:
      val newbie = MongoUserDao().findByName(s"marco_$rnd").get
      newbie.id.get.toString mustEqual (jsonResult \ "id").as[String]
      newbie.firstName mustEqual "Marco"
    }

    "should fail with an incomplete request" in new WithControllerAndRequest {
      val request = fakeRequest("POST", "/user").withJsonBody(Json.parse(
        s"""{"first_name": "Marco",
          |  "last_name": "Mass"
          |}""".stripMargin))
      val apiResult = call(testController.createUser, request)
      status(apiResult) mustEqual BAD_REQUEST
    }

    "should give me the right user back" in new WithControllerAndRequest {
      // first create a user in the DB
      val user = User.builder("With", "Jason") hasCreds Credentials("test_9876", "zikret") build
      val id = MongoUserDao() << user
      val request = fakeRequest(route = s"/user/$id")
      val apiResult = call(testController.userById(id.toString), request)
      status(apiResult) mustEqual OK
      val jsonBody = contentAsJson(apiResult)
      (jsonBody \ "first_name").as[String] mustEqual "With"
      (jsonBody \ "credentials" \ "username").as[String] mustEqual "test_9876"
      (jsonBody \ "credentials" \ "password").asOpt[String] must be (None)
    }
  }

}
