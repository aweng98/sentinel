package com.alertavert.sentinel.security

import java.util.UUID

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
  val action = new ManageSystem
}

class Create extends Action {
}

object Create {
  val action = new Create
}

class Grant extends ManageSystem {
}

object Grant {
  val action = new Grant
}

class Edit extends Action{
}

object Edit {
  val action = new Edit
}

class Delete extends Action {
}

object Delete {
  val action = new Delete
}

class View extends Action {
}

object View extends Action {
  val action = new View
}

trait Asset {
  val id = UUID.randomUUID()
}

class Resource extends Asset {
  var allowedActions: Set[Action] = Set()
}

class Permission(val action: Action, val resource: Resource) {

}
