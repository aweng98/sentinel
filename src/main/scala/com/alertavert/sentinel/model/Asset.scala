package com.alertavert.sentinel.model

import com.alertavert.sentinel.persistence.{HasCreator, HasId}
import org.bson.types.ObjectId
import scala.collection.mutable
import com.alertavert.sentinel.security.{Delete, Grant, Action}
import com.alertavert.sentinel.errors.NotAllowedException

/**
 * An ``Asset`` represents a business object that has a meaning for the end-users of
 * Sentinel: within their platform there may be different classes and types of assets with their
 * names, descriptions and business logic; however, within the scope of the permission system,
 * there is only one class of assets, which are uniquely identified by their ``id`` and have both
 * a ``creator`` (the ``User`` who created the asset) and an ``owner`` (the user who retains the
 * ultimate access to the asset).
 *
 * <p>The ``owner`` can both ``Grant`` other users permissions on the asset,
 * as well as ``Delete`` the asset and remove it from the system.
 *
 * @see Resource
 */
trait Asset extends HasId with HasCreator {

  /** Note that the ``owner`` of an ``asset`` may not necessarily be the ``creator`` */
  val owner: User

  // by default, assume the owner is the creator too
  this.createdBy = Some(owner)

  // and an Asset MUST always have an ID - we assign an arbitrary one at creation; this can be
  // overridden during retrieval form the persistence layer
  setId(new ObjectId)

  /** The unique path associated with the asset */
  override def toString = {
    val ownerId = owner.id.getOrElse(throw new NotAllowedException("Resource owner missing ID"))
    val resId = this.id.get
    s"/owner/$ownerId/asset/$resId"
  }
}

/**
 * An ``Asset`` with permissible actions becomes a ``Resource`` that can be used,
 * by those users that are authorized to do so, to accomplish certain tasks.
 *
 * <p> The actual permissions that are granted to a given ``User`` for a given ``Asset`` are not
 * specified here, but are persisted elsewhere.
 *
 * <p>Regardless of its nature, a ``Resource`` will always allow its ``owner`` to ``Grant``
 * others (including herself) other permissions, and to ``Delete`` the resource itself.
 */
class Resource(override val owner: User) extends Asset {
  var allowedActions: Set[Action] = Set(Grant(), Delete())


  def canEqual(other: Any): Boolean = other.isInstanceOf[Resource]

  override def equals(other: Any): Boolean = other match {
    case that: Resource =>
      (that canEqual this) &&
        owner == that.owner &&
        id == that.id
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(id, owner)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
