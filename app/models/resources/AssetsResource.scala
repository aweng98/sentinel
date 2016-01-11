package models.resources

import com.alertavert.sentinel.errors.{NotAllowedException, NotFoundException}
import com.alertavert.sentinel.model.{Resource, User}
import com.alertavert.sentinel.persistence.mongodb.{MongoUserDao, MongoResourceDao}
import org.bson.types.ObjectId
import play.api.libs.json.JsValue

/**
 * Manages the resource layer for all API actions related to Assets
 *
 * Created by marco on 11/25/14.
 */
object AssetsResource {
  val dao = MongoResourceDao()
  val userDao = MongoUserDao()

  def getAssetById(id: ObjectId) = dao.find(id)

  def create(asset: JsValue, creator: User) = {
    // TODO: this is relatively easy and, for now, we don't need a specialized serializer
    val body = asset.as[Map[String, String]]
    val name = body.getOrElse("name", "")
    getAssetByName(name) match {
      case Some(r) => throw new NotAllowedException(s"Asset '$name' already exists")
      case _ =>
    }
    val owner = body.get("owner") match {
      // if not specified, the owner is the same as the creator
      case None => creator
      // if specified, it must be a valid user
      case Some(userId) => {
        if (!ObjectId.isValid(userId)) throw new NotAllowedException(s"Not a valid User ID: $userId")
        val oid = new ObjectId(userId)
        userDao.find(oid).getOrElse(
          throw new NotFoundException(oid, s"Resource Owner must be a valid user, none found")
        )
      }
    }
    val resource = new Resource(name, owner)
    dao << resource
    resource
  }

  def changeOwner(id: ObjectId, newOwner: User) = ???

  def update(id: ObjectId, newValues: JsValue) = {
    // Currently only the name is allowed to be changed; to change owner, see changeOwner()
    val maybeName = (newValues \ "name").asOpt[String]
    maybeName.flatMap(n => {
      val res = dao >> id
      res.map(resource => {
        resource.name = n
        dao << resource
        true
      })
    }).orElse(Some(false)).get
  }

  def remove(id: ObjectId) = dao.remove(id)

  def getAssetByName(name: String) = {
    dao.findByName(name)
  }
}
