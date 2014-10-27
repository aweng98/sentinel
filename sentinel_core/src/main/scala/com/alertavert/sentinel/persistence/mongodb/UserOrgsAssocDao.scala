package com.alertavert.sentinel.persistence.mongodb

import com.alertavert.sentinel.errors.{DbException, NotFoundException}
import com.alertavert.sentinel.model.{Organization, User}
import com.alertavert.sentinel.persistence.{DataAccessManager, OneToManyAssocDao}
import com.mongodb.DBObject
import com.mongodb.casbah.Implicits._
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import org.bson.types.ObjectId

/**
 * Implements the `one-to-many` bi-directional [[User]] to [[Organization]]s association.
 *
 * Because each user is associated with an organization in a given role the `Many` type in the
 * definition for the [[OneToManyAssocDao]] is a ([[Organization]], [[String]]) tuple, where the
 * String is the user's role for the organization (eg, "user" or "admin").
 *
 * Created by marco on 10/13/14.
 */
class UserOrgsAssocDao(val collection: MongoCollection) extends OneToManyAssocDao[User,
  (Organization, String)] {

  /**
   * Models an association between a User and several Organizations, for each of which the user
   * has a given `role`.
   */
  class UserOrgAssociation(var user: User, var organizations: Map[Organization, String]) {
    def toDbObject: MongoDBObject = {
      val orgsRoles = organizations.map(x => MongoDBObject(
          "org_id" -> x._1.id.get,
          "role" -> x._2
      ))
      MongoDBObject(
        "user_id" -> user.id.get,
        "organizations" -> orgsRoles
      )
    }
  }

  object UserOrgAssociation {
    val userDao = MongoUserDao()
    val orgsDao = MongoOrganizationDao()

    /**
     * This method converts a [[MongoDBList]] into a sequence of [[MongoDBObject]]s,
     * by iterating over the `list` and, taking advantage of yet another implicit conversion
     * (from [[DBObject]] to [[MongoDBObject]], defined in [[com.mongodb.casbah.Implicits]]),
     * creates the appropriate sequence.
     *
     * <p>Why this is not done automagically in the `casbah` library is probably a moot point,
     * but the reality is that this needs doing so that inside a `for` loop the conversion is to
     * the correct type (a [[MongoDBObject]]) as opposed to an untyped [[AnyRef]].
     *
     * @param list the [[MongoDBList]] to convert
     * @return a sequence of all the objects contained in the `list`
     */
    // TODO: this probably needs moving to a more generally reusable place
    implicit def map2DbObj(list: MongoDBList): Seq[MongoDBObject] = {
      val res = scala.collection.mutable.MutableList[MongoDBObject]()
      val iter = list.iterator
      while (iter.hasNext) {
        res += iter.next().asInstanceOf[DBObject]
      }
      res.toSeq
    }

    def fromDbObject(item: MongoDBObject): UserOrgAssociation = {
      val organizationsRole: MongoDBList = item.as[MongoDBList]("organizations")

      val user_id = item.as[ObjectId]("user_id")

      val orgRolePairs = for {
        orgRole <- map2DbObj(organizationsRole)
        org = orgsDao.find(orgRole.as[ObjectId]("org_id")).getOrElse(
            throw new NotFoundException(orgRole.as[ObjectId]("org_id"),
                s"Could not retrieve the organization associated with user $user_id")
        )
      } yield (org, orgRole.as[String]("role"))

      new UserOrgAssociation(
        userDao.find(user_id).getOrElse(
            throw new NotFoundException(user_id,
                "Could not find a valid user for the User/Org association")),
        Map(orgRolePairs: _*))
    }
  }

  def getByUserid(uid: ObjectId) = {
    val res = collection.findOne(MongoDBObject("user_id" -> uid))
    UserOrgAssociation.fromDbObject(res.getOrElse(throw new NotFoundException(uid,
      "No organizations associated with this user")))
  }

  /**
   * Returns all
   * @param user the element whose entire set of associations we are looking up
   * @return a [[Seq]] of ([[Organization]], [[String]]) elements, which have been associated
   *         with the `user`
   */
  override def findAll(user: User) = {
    val assoc = getByUserid(user.id.get)
    assoc.organizations.toSeq
  }


  override def associate(user: User, orgsRoles: Seq[(Organization, String)]): Unit = {
    val assocRecord = new UserOrgAssociation(user, Map(orgsRoles: _*))
    collection.insert(assocRecord.toDbObject)
  }

  override def remove(one: User, other: (Organization, String)): Unit = {

  }

  override def removeAll(one: User): Unit = {

  }

  override def isAssociated(one: User, other: (Organization, String)) = false

}

object UserOrgsAssocDao {
  val USER_ORGS_COLLECTION = "user_orgs"
  private var dao: UserOrgsAssocDao = _

  def apply(): UserOrgsAssocDao = dao match {
    case null => if (DataAccessManager.isReady) {
          dao = new UserOrgsAssocDao(DataAccessManager.db(USER_ORGS_COLLECTION))
        } else throw new DbException("DataAccessManager not initialized")
        dao
    case _ => dao
  }
}
