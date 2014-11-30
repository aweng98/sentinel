package com.alertavert.sentinel.controllers

import com.alertavert.sentinel.model.Resource
import com.alertavert.sentinel.persistence.mongodb.{MongoUserDao, MongoResourceDao}
import models.resourceReads
import org.bson.types.ObjectId
import play.api.libs.json.Json
import play.api.libs.openid.Errors.BAD_RESPONSE
import play.api.test.Helpers._

class ResourceControllerSpec extends ControllerSpec {

  "Resources" should {
    "be easy to store in the DB" in new WithControllerAndRequest {
      val assetName = "test-asset"
      val owner = makeUsers(1)(0)

      val request = fakeRequest("POST", "/asset ").
        withJsonBody(Json.parse( s"""{"name": "$assetName"}""")).
        withHeaders("Authorization" -> s"username=${owner.getCredentials.username};hash=fwaz9987==")
      val response = call(testController.createAsset, request)
      status(response) mustEqual CREATED
      val asset = MongoResourceDao().findByName(assetName)
      asset mustNot be (None)
      // we use flatMap() here because asset is an Option and id is too - using map() would
      // return an Option wrapped in an option: Some(Some(ObjectId(123456)))
      header("Location", response).get mustEqual s"/asset/${asset.flatMap(_.id).get toString}"
    }
  }

  "not be stored for an invalid owner" in new WithControllerAndRequest {
    val fakeId = new ObjectId() toString
    val request = fakeRequest("POST", "/asset ").
        withJsonBody(Json.parse( s"""{"name": "doomed", "owner": "$fakeId"}"""))

    val response = call(testController.createAsset, request)
    status(response) mustEqual FORBIDDEN
  }

  "fail for invalid requests" in new WithControllerAndRequest {
    val request = fakeRequest("POST", "/asset ").
        withJsonBody(Json.parse( s"""{"name": "nonsense", "owner": "deadbeef-99876"}"""))
    val response = call(testController.createAsset, request)
    status(response) mustEqual BAD_REQUEST
  }

  "be safe to store in bulk" in new WithControllerAndRequest {
    // let's give an ice-cream to any of the 10 newly created users
    val newUsers = makeUsers(10)
    val requests = for (user <- newUsers) yield {
      fakeRequest("POST", "/asset ").
        withJsonBody(Json.parse( s"""{"name": "ice-cream", "owner": "${user.id.get}"}"""))
    }
    requests.foreach(r => status(call(testController.createAsset, r)) mustEqual CREATED)
  }

  "be retrieved correctly" in new WithControllerAndRequest {
    val owner = makeUsers(1)(0)
    val simpleAsset = new Resource("test-res", owner)
    val rid = MongoResourceDao() << simpleAsset
    val response = call(testController.assetById(rid toString), fakeRequest(route = s"/asset/$rid"))
    status(response) mustEqual OK
    val jsonBody = contentAsJson(response)
    var resource = jsonBody.as[Resource]
    val ownerId = resource.owner.id.get
    val owner2 = MongoUserDao() >> ownerId
    resource.owner = owner2.get
    resource mustEqual simpleAsset
  }

  "fail for invalid ID" in new WithControllerAndRequest {
    val rid = new ObjectId toString()
    status(call(testController.assetById(rid), fakeRequest(route = s"/asset/$rid"))) mustEqual
      NOT_FOUND
  }

  "fail for bad input" in new WithControllerAndRequest {
    status(call(testController.assetById("1234-3345"),
      fakeRequest(route = s"/asset/1234-3345"))) mustEqual BAD_REQUEST
  }

  "can be deleted without much fuss" in new WithControllerAndRequest {
    val doomedAsset = makeAsset()
    val id = doomedAsset.id.map(_.toString).get
    status(call(testController.removeAsset(id), fakeRequest("DELETE", s"/asset/$id"))) mustBe
      NO_CONTENT
  }

  "cannot be deleted if it does not exist" in new WithControllerAndRequest {
    val id = new ObjectId toString()
    status(call(testController.removeAsset(id), fakeRequest("DELETE", s"/asset/$id"))) mustBe
      NOT_FOUND
  }

  "cannot be deleted if ID is malformed" in new WithControllerAndRequest {
    val id = "abcde-8765-deadbeef"
    status(call(testController.removeAsset(id), fakeRequest("DELETE", s"/asset/$id"))) mustBe
      BAD_REQUEST
  }

  "can be updated" in new WithControllerAndRequest {
    val asset: Resource = makeAsset()
    val newName = "new name"
    val request = fakeRequest("PUT", s"/asset/${asset.id.get}").
                    withJsonBody(Json.parse( s"""{"name": "$newName"}"""))
    val response = call(testController.updateAsset(asset.id.get toString), request)
    status(response) mustEqual NO_CONTENT
    // we use flatMap() here because asset is an Option and id is too - using map() would
    // return an Option wrapped in an option: Some(Some(ObjectId(123456)))
    header("Location", response).get mustEqual s"/asset/${asset.id.get toString}"

    // we can also verify that it was stored with the updated name
    val actual = MongoResourceDao().findByName(newName)
    actual mustNot be (None)
    actual.flatMap(_.id) mustEqual asset.id
  }

  "cannot update what doesn't exist" in new WithControllerAndRequest {
    val id = new ObjectId() toString
    val response = call(testController.updateAsset(id),
        fakeRequest("PUT", s"/asset/$id").
          withJsonBody(Json.parse( s"""{"name": "rosa stat pristina nomine suo"}"""))
    )
    status(response) mustEqual NOT_FOUND
  }
}
