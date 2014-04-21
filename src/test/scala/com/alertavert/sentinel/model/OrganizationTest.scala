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
    val org = builder.build
    assert(org != null)
    assertResult("Acme Evil Deeds plc") { org.name }
  }

  it must "be disabled by default" in new OrgBuilder {
    val org = Organization.builder("foo") build

    assert(! org.active)
  }

  it can "be enabled, and disabled" in new OrgBuilder {
    val org = builder setActive() build

    assert(org active)
    org.disable()
    assert(! org.active)
  }

  it can "have an ID assigned, and its string repr will reflect that" in new OrgBuilder {
    val org_id = new ObjectId
    val org = builder withId org_id setActive() build

    assert(org_id === org.id.get)
    val name = builder.name
    assert(s"[$org_id] $name (Active)" === org.toString)
  }

  "An empty org" must "not have an ID or a name" in {
    val emptyOrg = Organization.EmptyOrg
    assert(emptyOrg.id === None)
    assert(emptyOrg.name === "NewCo")
  }

}
