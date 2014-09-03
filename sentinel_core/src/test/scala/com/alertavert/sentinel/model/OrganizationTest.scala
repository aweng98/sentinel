package com.alertavert.sentinel.model

import com.alertavert.sentinel.UnitSpec
import org.bson.types.ObjectId


/**
 * Unit tests for Organization model
 *
 */
class OrganizationTest extends UnitSpec {

  trait OrgBuilder {
    val builder = Organization.builder("Acme Evil Deeds plc")
  }

  "An Organization" can "be created" in new OrgBuilder {
    val orgz = builder.build
    assert(orgz != null)
    assertResult("Acme Evil Deeds plc") {
      orgz.name
    }
  }

  it must "be disabled by default" in new OrgBuilder {
    val orgz = Organization.builder("foo") build

    assert(!orgz.active)
  }

  it can "be enabled, and disabled" in new OrgBuilder {
    val orgz = builder setActive() build

    assert(orgz active)
    orgz.disable()
    assert(!orgz.active)
  }

  it can "have an ID assigned, and its string repr will reflect that" in new OrgBuilder {
    val orgz_id = new ObjectId
    val orgz = builder withId orgz_id setActive() build

    assert(orgz_id === orgz.id.get)
    val name = builder.name
    assert(s"[$orgz_id] $name (Active)" === orgz.toString)
  }

  "An empty orgz" must "not have an ID or a name" in {
    val emptyOrg = Organization.EmptyOrg
    assert(emptyOrg.id === None)
    assert(emptyOrg.name === "NewCo")
  }

}
