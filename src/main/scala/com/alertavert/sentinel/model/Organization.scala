package com.alertavert.sentinel.model

import com.mongodb.casbah.Imports.ObjectId
import java.util.Date
import com.alertavert.sentinel.persistence.HasId

/**
 * Models an Organization
 *
 * Orgs can have sub-orgs, and so on, to form a tree of related entities: in our model, any
 * organization permission that is granted to one, applies too to all those in the subtree.
 *
 * <p>{@link User}s can be associated with one or more organizations, either as users,
 * or administrators - if the latter, they can "manage" the {@link Organizations}, in other words
 * they can edit its properties, associate users to it and grant priviliges - they can even
 * delete the org, if that permission is granted.
 *
 */
class Organization(val name: String) extends HasId {

  private var _id: Option[ObjectId] = None

  override def id: Option[ObjectId] = _id
  override def setId(id: ObjectId) {
    this._id = Some(id)
  }

  var created_by: User = _
  var created: Date = new Date()
  var active: Boolean = false

  def activate() {
    active = true
  }

  def disable() {
    active = false
  }

  // TODO: use JSON repr
  override def toString = {
    val id_ = id match {
      case None => ""
      case Some(x) => x toString
    }
    val active_ = active match {
      case true => "Active"
      case false => "Disabled"
    }
    s"[$id_] $name ($active_)"
  }


  def canEqual(other: Any): Boolean = other.isInstanceOf[Organization]

  override def equals(other: Any): Boolean = other match {
    case that: Organization =>
      (that canEqual this) &&
        _id == that._id &&
        name == that.name
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(_id, name)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}


object Organization {
  class Builder(val name: String) {
    private val _org = new Organization(name)
    _org.created = new Date()

    def withId(id: ObjectId): Builder = {
      _org.setId(id)
      this
    }

    def createdBy(user: User) = {
      _org.created_by = user
      this
    }

    def setActive(active: Boolean = true) = {
      if (active) _org.activate() else _org.disable()
      this
    }

    def created(when: Date) = {
      _org.created = when
      this
    }

    def build: Organization = {
      _org
    }
  }

  val EmptyOrg = new Organization("")

  def builder(org_name: String): Builder = new Builder(org_name)
}
