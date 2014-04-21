package com.alertavert.sentinel.security

import com.alertavert.sentinel.persistence.{HasCreator, HasId}
import org.bson.types.ObjectId
import java.util.Date


trait Action {
  def name = {
    val fqn = getClass.getName
    val last_dot = fqn.lastIndexOf('.')
    if (last_dot > 0) fqn.substring(last_dot + 1) else fqn
  }

  override def toString = name
}

class ManageSystem extends Action {
}

object ManageSystem {
  def apply(): Action = new ManageSystem
}

class Create extends Action {
}

object Create {
  def apply(): Action = new Create
}

class Grant extends ManageSystem {
}

object Grant {
  def apply(): Action = new Grant
}

class Edit extends Action{
}

object Edit {
  def apply(): Action = new Edit
}

class Delete extends Action {
}

object Delete {
  def apply(): Action = new Delete
}

class View extends Action {
}

object View extends Action {
  def apply(): Action = new View
}

trait Asset extends HasId with HasCreator {

  /** Note that the ``owner`` of an ``asset`` may not necessarily be the ``creator`` */
  var ownerId: ObjectId = _

  /** The unique path associated with the asset */
  def path = s"/owner/$ownerId/asset/$id"

}

class Resource extends Asset {
  val allowedActions: Set[Action] = Set()
}

/**
 * A ``permission`` defines an action that can be performed (eg, ``edit``) on a Resource.
 * Permissions are immutable.
 *
 * @param action
 * @param resource
 */
class Permission(val action: Action, val resource: Resource) {

}
