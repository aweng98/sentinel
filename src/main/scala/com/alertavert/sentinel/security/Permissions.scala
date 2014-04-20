package com.alertavert.sentinel.security

import com.alertavert.sentinel.persistence.HasId
import org.bson.types.ObjectId
import java.util.Date

/**
 * Created by marco on 2/23/14.
 */

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

trait Asset extends HasId {
  private var id_ = Some(new ObjectId())

  var path: String = s"/asset/$id"

  var ownerId: ObjectId = _

  var createdBy = ownerId

  var createdAt: Date = new Date

  def id = id_

  def setId(id: ObjectId) {
    id_ = Some(id)
  }
}

class Resource extends Asset {
  var allowedActions: Set[Action] = Set()
}

class Permission(val action: Action, val resource: Resource) {

}
