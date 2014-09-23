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

  def getOrgById(id: String) = {
    dao.find(new ObjectId(id))
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
    val name = org.name
    val orgId = org.id.getOrElse("")
    if (org.id.nonEmpty && (org.id.get != new ObjectId(id))) throw new NotAllowedException(
      s"[$id] A different organization [$orgId] already exists with the new name ($name)"
    )
    org.setId(new ObjectId(id))
    dao << org
    org
  }

  private def parseOrgAndCheckName(request: JsValue) = {
    val name = (request \ "name").as[String]
    val active = (request \ "active").as[Boolean]
    val builder = Organization.builder(name).setActive(active)

    // Organization names are supposed to be unique: if this already exists, we'll retrieve its ID
    getOrgByName(name) match {
      case None =>
      case Some(organization) => builder withId organization.id.get
    }
    builder.build
  }
}
