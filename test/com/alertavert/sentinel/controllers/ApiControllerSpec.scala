// Copyright 2014 (c) AlertAvert.com. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.controllers


import com.alertavert.sentinel.errors.AuthenticationError
import com.alertavert.sentinel.model.{Organization, User}
import com.alertavert.sentinel.persistence.DataAccessManager
import com.alertavert.sentinel.persistence.mongodb.{UserOrgsAssocDao, MongoOrganizationDao, MongoUserDao}
import com.alertavert.sentinel.security.Credentials
import controllers.ApiController
import models.{UserReads, oidReads, orgReads, orgsWrites}
import org.bson.types.ObjectId
import org.scalatest.{Ignore, BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.{JsValue, JsArray, Json}
import play.api.mvc.{Controller, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.util.Random

/**
 * <h1>API Tests
 *
 * <p>White-box API tests that verify that the values returned by the [[controllers.ApiController]]
 * match what is expected of them.
 *
 * <p>These are not Integration Tests, as they do not query the API as a black box.
 */
class ApiControllerSpec extends ControllerSpec {

  before {
    if (DataAccessManager.isReady) {
      MongoUserDao().clear()
      MongoOrganizationDao().clear()
      UserOrgsAssocDao().collection.drop()
    }
  }

  "Sentinel API" should {

    "return all users in DB" in new WithControllerAndRequest {
      val dao = MongoUserDao()
      val users = dao.findAll()
      val apiResult = contentAsJson(testController.users.apply(fakeRequest()))
      apiResult mustNot be (null)
      val usersArray = apiResult.as[JsArray]
      val usersResponse = usersArray.value.map(v => v.as[User])
      users.foreach(usersResponse must contain(_))
    }

    "fail with an incomplete request" in new WithControllerAndRequest {
      val request = fakeRequest("POST", "/user").withJsonBody(Json.parse(
        """{"first_name": "Marco",
          |  "last_name": "Mass"
          |}""".stripMargin))
      val apiResult = call(testController.createUser, request)
      status(apiResult) mustEqual BAD_REQUEST
    }

    "give caller the right user back" in new WithControllerAndRequest {
      // first create a user in the DB
      val user = User.builder("With", "Jason") hasCreds Credentials("test_9876", "zikret") build
      val id = MongoUserDao() << user
      val request = fakeRequest(route = s"/user/$id")
      val apiResult = call(testController.userById(id.toString), request)
      status(apiResult) mustEqual OK
      // TODO: use the implicit conversion JSON -> User here (UserReads?)
      val jsonBody = contentAsJson(apiResult)
      (jsonBody \ "first_name").as[String] mustEqual "With"
      (jsonBody \ "credentials" \ "username").as[String] mustEqual "test_9876"
      (jsonBody \ "credentials" \ "password").asOpt[String] must be (None)
    }
  }

  // --- ORG tests

  "allow caller to create a new Org" in new WithControllerAndRequest {
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

  "allow caller to retrieve all Orgs" in new WithControllerAndRequest {
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

  "allow caller to retrieve a specific Org" in new WithControllerAndRequest {
    makeOrgs(3)
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

  "allow caller to activate an Org" in new WithControllerAndRequest {
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

  "allow caller to modify an Org's name" in new WithControllerAndRequest {
    // 1. create a new organization
    val org = Organization.builder("EvilCo, Inc.").build
    val orgId = MongoOrganizationDao() << org
    orgId mustNot be (null)

    // 2. get PR to do some magic
    val polishedOrg = Organization.builder("DoGooders, PLC") withId orgId setActive true build

    // 3. create a request to PUT the modified org
    val request = fakeRequest("PUT", s"/org/$orgId").withJsonBody(Json.toJson(polishedOrg))
    val apiResult = call(testController.modifyOrg(orgId.toString), request)

    // 4. verify it was saved and then check the new state was recorded
    status(apiResult) mustBe OK
    val newOrg = MongoOrganizationDao().find(orgId).get
    newOrg.active must be (true)
    newOrg.name must be ("DoGooders, PLC")
  }

  // --- USR / ORG tests

  "allow caller to associate a given user with an Org" in new WithControllerAndRequest {
    // 1. create a new user and a new org
    val user = makeUsers(1)(0)
    val org = makeOrgs(1)(0)

    val usrId = user.id.get
    val orgId = org.id.get
    val request = fakeRequest("POST", s"/user/$usrId/org/$orgId").withBody(
      Json.parse("""{"role": "test-user"}"""))
    val apiResult = call(testController.assocUserOrg(usrId.toString, orgId.toString), request)
    status(apiResult) must be (CREATED)
    val assoc = UserOrgsAssocDao().getByUserid(usrId)
    assoc.organizations.map(_._1.id) must contain (Some(orgId))
  }

  "retrieve all associations for user" in new WithControllerAndRequest {
    val user = makeUsers(1)(0)
    val orgs = makeOrgs(3)
    val orgsRoles = orgs.map((_, "user"))
    UserOrgsAssocDao().associate(user, orgsRoles)
    val request = fakeRequest("POST", s"/user/${user.id.get}/org")
    val apiResult = testController.getUsersOrgs(user.id.get.toString).apply(request)

    status(apiResult) mustBe OK
    val jsonResponse = contentAsJson(apiResult)
    (jsonResponse \ "organizations").as[List[JsValue]] mustNot be (null)
    (jsonResponse \ "organizations").as[List[JsValue]] must have length 3
  }

  "create a new user" in new WithControllerAndRequest {
    val request = fakeRequest("POST", "/user").withJsonBody(Json.parse(
      s"""{"first_name": "Marco",
          |  "last_name": "Mass",
          |  "credentials": {
          |    "username": "test-user",
          |    "password": "secret"
          |  }
          |}""".stripMargin))
    val apiResult = call(testController.createUser, request)
    status(apiResult) mustEqual CREATED
    val jsonResult = contentAsJson(apiResult)
    ObjectId.isValid((jsonResult \ "id").as[String]) mustBe true

    // now get the real thing from the DB and check it was created with the correct values:
    val newbie = MongoUserDao().findByName("test-user").get
    newbie.id.get.toString mustEqual (jsonResult \ "id").as[String]
    newbie.firstName mustEqual "Marco"
  }

  "create an arbitrary number of users" in new WithControllerAndRequest {
    val users = makeUsers(10)
    val dao = MongoUserDao()
    users.foreach(u => dao.find(u.id.get) mustNot be (None))
  }

  "Login" should {
    "reject a bogus user" in new WithControllerAndRequest {
      val request = fakeRequest("POST", "/login").withJsonBody(Json.parse(
      s"""{"username": "haker",
          |"password": "123456"
          |}""".stripMargin))
      val res = call(testController.login, request)
      // The auth error is thrown by checking the status of the response (which causes the future
      // to be evaluated and the auth to fail):
      an [AuthenticationError] should be thrownBy status(res)
    }

    "accept a valid user" in new WithControllerAndRequest {
      val user = makeUsers(1)(0)
      val request = fakeRequest("POST", "/login").withJsonBody(Json.parse(
      s"""{"username": "${user.getCredentials.username}",
          |"password": "${TEST_PASSWORD}"
          |}""".stripMargin))
      val res = call(testController.login, request)
      status(res) must be (OK)
      contentAsString(res) contains "api_key"
    }
  }
}
