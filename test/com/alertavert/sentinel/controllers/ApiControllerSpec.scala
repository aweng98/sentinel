// Copyright 2014 (c) AlertAvert.com. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.controllers


import com.alertavert.sentinel.model.{Organization, User}
import com.alertavert.sentinel.persistence.DataAccessManager
import com.alertavert.sentinel.persistence.mongodb.{MongoOrganizationDao, MongoUserDao}
import com.alertavert.sentinel.security.Credentials
import controllers.ApiController
import models.{UserReads, oidReads, orgReads, orgsWrites}
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

    "be ready and render the index page" in new WithControllerAndRequest {
      DataAccessManager.isReady mustBe true
      DataAccessManager.db.getName mustBe "sentinel-test"
      val indexResult = testController.index().apply(fakeRequest())
      contentAsString(indexResult) must include(
        "Sentinel - REST API-driven User Management made easy")
    }

    "return all users in DB" in new WithControllerAndRequest {
      val dao = MongoUserDao()
      val users = dao.findAll()
      val apiResult = contentAsJson(testController.users.apply(fakeRequest()))
      apiResult mustNot be (null)
      val usersArray = apiResult.as[JsArray]
      val usersResponse = usersArray.value.map(v => v.as[User])
      users.foreach(usersResponse must contain(_))
    }

    "create a new user" in new WithControllerAndRequest {
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

    "fail with an incomplete request" in new WithControllerAndRequest {
      val request = fakeRequest("POST", "/user").withJsonBody(Json.parse(
        """{"first_name": "Marco",
          |  "last_name": "Mass"
          |}""".stripMargin))
      val apiResult = call(testController.createUser, request)
      status(apiResult) mustEqual BAD_REQUEST
    }

    "give me the right user back" in new WithControllerAndRequest {
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

  // --- ORG tests

  "allow me to create a new Org" in new WithControllerAndRequest {
    val request = fakeRequest("POST").withJsonBody(Json.parse(
      """{"name": "Acme, Inc.", "active": true}"""
    ))
    val apiResult = call(testController.createOrg, request)
    status(apiResult) mustEqual CREATED
    val jsonBody = contentAsJson(apiResult)
    val orgId = (jsonBody \ "id").as[ObjectId]
    orgId mustNot be (null)
    MongoOrganizationDao().remove(orgId)
  }

  "allow me to retrieve all Orgs" in new WithControllerAndRequest {
    val allOrgs = List(Organization.builder("foo") build,
        Organization.builder("bar") build,
        Organization.builder("quz") build
    )
    // save to DB and extract the UUIDs
    val orgsWithIds = for (org <- allOrgs) yield {
      val id = MongoOrganizationDao() << org
      org.setId(id)
      org
    }
    val apiResult = contentAsJson(testController.orgs.apply(fakeRequest()))
    apiResult mustNot be (null)
    val orgsArray = apiResult.as[JsArray]
    val orgsResponse = orgsArray.value.map(v => v.as[Organization])
    orgsWithIds.foreach(orgsResponse must contain(_))
  }

  "allow me to retrieve a specific Org" in new WithControllerAndRequest {
    val org: Organization = MongoOrganizationDao().findAll(limit=1).toList match {
      case Nil => fail("No organizations in DB")
      case head :: xs => head
    }
    val orgId = org.id.getOrElse(fail(s"No ID for org ${org.name}"))
    val apiResult = testController.orgById(orgId.toString).apply(fakeRequest())
    status(apiResult) mustBe OK
    val json = contentAsJson(apiResult)
    (json \ "id").as[ObjectId] must be (orgId)
  }

  "allow me to activate an Org" in new WithControllerAndRequest {
    // 1. create a new 'disabled' organization
    val org = Organization.builder("TestAcme, Inc.") build
    val orgId = MongoOrganizationDao() << org
    org.active must be (false)

    // 2. activate it
    org.activate()

    // 3. create a request to PUT the modified org
    val request = fakeRequest("PUT", s"/org/$orgId").withJsonBody(Json.toJson(org))
    val apiResult = call(testController.modifyOrg(orgId.toString), request)

    // 4. verify it was saved and then check the new state was recorded
    status(apiResult) mustBe OK
    val newOrg = MongoOrganizationDao().findByName("TestAcme, Inc.").get
    newOrg.active must be (true)
  }

  "allow me to modify an Org's name" in new WithControllerAndRequest {
    // 1. create a new organization
    val org = Organization.builder("EvilCo, Inc.") build
    val orgId = MongoOrganizationDao() << org
    orgId mustNot be (null)

    // 2. get PR to do some magic
    val polishedOrg = Organization.builder("DoGooders, PLC") withId orgId setActive(true) build

    // 3. create a request to PUT the modified org
    val request = fakeRequest("PUT", s"/org/$orgId").withJsonBody(Json.toJson(polishedOrg))
    val apiResult = call(testController.modifyOrg(orgId.toString), request)

    // 4. verify it was saved and then check the new state was recorded
    status(apiResult) mustBe OK
    val newOrg = MongoOrganizationDao().find(orgId).get
    newOrg.active must be (true)
    newOrg.name must be ("DoGooders, PLC")
  }

}
