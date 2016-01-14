// Copyright 2014 (c) AlertAvert.com. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.controllers

import com.alertavert.sentinel.TestUtilities
import com.alertavert.sentinel.model.{Organization, Resource, User}
import com.alertavert.sentinel.persistence.DataAccessManager
import com.alertavert.sentinel.persistence.mongodb.{MongoOrganizationDao, MongoResourceDao, MongoUserDao}
import com.alertavert.sentinel.security.Credentials
import controllers.ApiController
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc.{Controller, Results}
import play.api.test.FakeRequest

/**
 * <h1>API Tests
 *
 * <p>White-box API tests that verify that the values returned by the [[controllers.ApiController]]
 * match what is expected of them.
 *
 * <p>These are not Integration Tests, as they do not query the API as a black box.
 */
class ControllerSpec extends PlaySpec with Results with OneAppPerSuite with BeforeAndAfterAll
    with BeforeAndAfter {

  var testController: Controller with ApiController = _

  override def beforeAll() = {
    TestUtilities.initDataManagerForTests()
  }

  override def afterAll() {
    DataAccessManager.db.dropDatabase()
  }

  val TEST_PASSWORD: String = "zikret"

  /**
   * Helper method to create a few users that the individual tests can use
   * These will have usernames `user_nn` where `nn` is between `1` and `num`
   *
   * @param num the number of users to create
   * @return the sequence of users created
   */
  def makeUsers(num: Int): Seq[User] = {
    val dao = MongoUserDao()
    for (i <- 1 to num) yield {
      val username = s"user_$i"
      dao.findByName(username) match {
        case None =>
          val user = User.builder(s"User-$i") hasCreds Credentials(username,
            TEST_PASSWORD) build()
          dao << user
          user
        case Some(user) => user
      }
    }
  }


  /**
   * Helper method to create a few orgs that the individual tests can use
   */
  def makeOrgs(num: Int): Seq[Organization] = {
    for (i <- 1 to num) yield {
      val org = Organization.builder(s"Company#$i").build
      val orgId = MongoOrganizationDao() << org
      orgId mustNot be(null)
      org
    }
  }

  def makeAsset(name: String = "test-asset"): Resource = {
    val owner = makeUsers(1).head
    val simpleAsset = new Resource(name, owner)
    MongoResourceDao() << simpleAsset
    simpleAsset
  }

  trait WithControllerAndRequest {
    testController = new Controller with ApiController
    def fakeRequest(method: String = "GET",
                    route: String = "/",
                    apiKey: String = "notreallyavalidkey==") = FakeRequest(method, route)
      .withHeaders(
        ("Authorization", s"username=bob;api-key=${apiKey}")
      )
  }
}
