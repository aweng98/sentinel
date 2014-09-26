// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.security

import com.alertavert.sentinel.model.{Resource, User}


trait Action extends Serializable {
  def name = {
    val fqn = getClass.getName
    val last_dot = fqn.lastIndexOf('.')
    if (last_dot > 0) fqn.substring(last_dot + 1) else fqn
  }

  override def toString = name.capitalize

  override def hashCode(): Int = name.hashCode
}

object Action {
  var actionsMap: Map[String, Action] = Map()

  def register(action: Action) {
    actionsMap += action.name -> action
  }
}

class ManageSystem extends Action

object ManageSystem {
  private val action = new ManageSystem
  Action.register(action)
  def apply() = action
}

class Create extends Action

object Create {
  private val action = new Create
  Action.register(action)
  def apply(): Action = action
}

class Grant extends ManageSystem

object Grant {
  private val action = new Grant
  Action.register(action)

  def apply() = action
}

class Edit extends Action

object Edit {
  private val action = new Edit
  Action.register(action)

  def apply(): Action = action
}

class Delete extends Action

object Delete {
  private val action = new Delete
  Action.register(action)

  def apply() = action
}

class View extends Action

object View extends Action {
  private val action = new View
  Action.register(action)

  def apply(): Action = action
}


/**
 * A ``permission`` defines an action that can be performed (eg, ``edit``) on a Resource.
 * Permissions are immutable.
 *
 * @param action the action that is allowed on the ``resource``
 * @param resource the ``resource`` which we are allowed to take ``action`` upon
 */
class Permission(val action: Action, val resource: Resource) {

  require(resource.allowedActions contains action)

  // TODO: this needs some guard: ie, verifying that the 'grantor' has the permission to Grant on
  // this Asset
  def grantTo(user: User) {
    user.perms = user.perms :+ this
  }

  def grantedTo(user: User) = {
    user.perms.contains(this)
  }

  def revoke(user: User) = {
    if (grantedTo(user)) {
      user.perms = user.perms.filterNot(_ != this)
    }
  }

  override def hashCode(): Int = 31 * action.hashCode() * resource.hashCode()

  override def equals(that: Any): Boolean = that match {
    case that: Permission => this.action == that.action &&
      this.resource == that.resource
    case _ => false
  }

  override def toString = {
    s"Permission: $action for $resource"
  }
}

object Permission {

  def grant(action: Action, resource: Resource, user: User) {
    val perm = new Permission(action, resource)
    perm.grantTo(user)
  }

  def grant(actions: Seq[Action], resource: Resource, user: User) {
    for (action <- actions) {
      grant(action, resource, user)
    }
  }
}
