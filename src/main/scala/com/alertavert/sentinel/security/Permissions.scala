package com.alertavert.sentinel.security

import com.alertavert.sentinel.persistence.{HasCreator, HasId}
import org.bson.types.ObjectId
import java.util.Date
import scala.collection.mutable
import com.alertavert.sentinel.model.User


trait Action {
  def name = {
    val fqn = getClass.getName
    val last_dot = fqn.lastIndexOf('.')
    if (last_dot > 0) fqn.substring(last_dot + 1) else fqn
  }

  override def toString = name.capitalize
}

class ManageSystem extends Action {
}

object ManageSystem {
  private val action = new ManageSystem
  def apply() = action
}

class Create extends Action {
}

object Create {
  private val action = new Create
  def apply(): Action = action
}

class Grant extends ManageSystem {
}

object Grant {
  private val action = new Grant
  def apply() = action
}

class Edit extends Action{
}

object Edit {
  private val action = new Edit
  def apply(): Action = action
}

class Delete extends Action {
}

object Delete {
  private val action = new Delete
  def apply() = action
}

class View extends Action {
}

object View extends Action {
  private val action = new View
  def apply(): Action = action
}

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
  var ownerId: ObjectId = _

  /** The unique path associated with the asset */
  def path = s"/owner/$ownerId/asset/$id"

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
class Resource extends Asset {
  val allowedActions: mutable.Set[Action] = mutable.Set(Grant(), Delete())
}

/**
 * A ``permission`` defines an action that can be performed (eg, ``edit``) on a Resource.
 * Permissions are immutable.
 *
 * @param action
 * @param resource
 */
class Permission(val action: Action, val resource: Resource) {
  private var user: User = _

  def grant(user: User) {
    this.user = user
  }
}
