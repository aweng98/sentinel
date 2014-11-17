package com.alertavert.sentinel.security

import com.alertavert.sentinel.UnitSpec

class PermissionsTest extends UnitSpec {

  "Actions" should "be created by name" in {
    val edit = Edit()
    Action.valueOf("Edit").get should be (edit)
  }

  it should "not be affected by capitalization" in {
    val sys = ManageSystem()
    Action.valueOf("ManageSystem").get should be (sys)
  }

}
