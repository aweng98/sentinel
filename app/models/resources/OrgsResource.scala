// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package models.resources

import com.alertavert.sentinel.errors.{NotFoundException, NotAllowedException}
import com.alertavert.sentinel.model.Organization
import com.alertavert.sentinel.persistence.mongodb.MongoOrganizationDao
import org.bson.types.ObjectId
import play.api.libs.json.JsValue

/**
 * API resources to manage all the Organization-related requests
 *
 * Created by marco on 9/22/14.
 */
object OrgsResource {


  val dao = MongoOrganizationDao()

  def getAllOrgs = {
    // TODO: implement extracting limit/offset from query params
    dao.findAll()
  }

  def getOrgById(id: ObjectId) = {
    dao.find(id)
  }

  def getOrgByName(name: String) = {
    dao.findByName(name)
  }

  def createOrg(request: JsValue) = {
    val org = parseOrgAndCheckName(request)
    if (org.id.nonEmpty) throw new NotAllowedException("Organization " + org.name +
      " already exists")
    dao << org
    org
  }

  def updateOrg(id: String, request: JsValue) = {
    val org = parseOrgAndCheckName(request)
    val orgId = new ObjectId(id)
    if (org.id.nonEmpty && (org.id.get != orgId)) throw new NotAllowedException(
      s"[$id] A different organization [$orgId] already exists with the new name (${org.name})"
    ) else {
      org.setId(orgId)
      dao << org
    }
    org
  }

  /**
   * Parses the JSON request into an [[Organization]] object and, if the name matches an existing
   * org in the db, it will retrieve the ID and will fill it in
   *
   * @param request a JSON representation of an [[Organization]]
   * @return the object parsed and, optionally, a matching ID
   */
  private def parseOrgAndCheckName(request: JsValue): Organization = {
    val name = (request \ "name").as[String]
    val active = (request \ "active").as[Boolean]
    val builder = Organization.builder(name).setActive(active)

    // Organization names are supposed to be unique: if this already exists, we'll retrieve its ID
    getOrgByName(name) match {
      case None => builder.build
      case Some(organization) => builder withId organization.id.get build
    }
  }
}
