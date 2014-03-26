package com.alertavert.sentinel.model

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
class Organization(val name: String) {

}


object Organization {
  class Builder {
    var name: String = _

    def build: Organization = {
      new Organization(name)
    }
  }

  val EmptyOrg = new Organization("empty org")

  def builder: Builder = new Builder
}
