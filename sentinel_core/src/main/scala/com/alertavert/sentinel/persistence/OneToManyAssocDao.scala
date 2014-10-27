package com.alertavert.sentinel.persistence

import com.alertavert.sentinel.security.Permission.Role

/**
 * Models a `one-to-many` association in the DB.
 * Created by marco on 10/13/14.
 */
trait OneToManyAssocDao[One <: HasId, Many] {

  /**
   * Creates an association between `one` element and one or `many` others.
   *
   * Optionally, a [[Role]] can be added to this association (eg, between a
   * [[com.alertavert.sentinel.model.User]] and an [[com.alertavert.sentinel.model.Organization]]
   * with an `Admin` [[Role]].
   *
   * The meaning of the association (as well as its directionality) is entirely defined by the
   * concrete implementations of this trait
   *
   */
  def associate(one: One, many: Seq[Many])

  /**
   * Removes any association between the `one` and one of the `many` elements; all others are
   * unaffected.
   *
   * @param one the element that we want to remove the association for
   * @param other the other end of the association
   */
  def remove(one: One, other: Many)

  /**
   * Removes all the `many` associations for this element
   *
   * @param one the element that will be entirely removed from the association collection
   */
  def removeAll(one: One)

  /**
   * Returns whether the `one` element is associated with one of the [[Many]], the `other`.
   *
   * @return `true` if the `one` has an association with the `other`
   */
  def isAssociated(one: One, other: Many): Boolean

  /**
   * Finds all association for the element, if any.
   *
   * @param one the element whose entire set of associations we are looking up
   * @return a [[Seq]] of [[Many]] elements, which have been associated with the `one`
   */
  def findAll(one: One): Seq[Many]
}
