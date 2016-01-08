// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.persistence

import com.alertavert.sentinel.persistence.mongodb.MongoUserDao
import com.alertavert.sentinel.errors.DbException
import com.alertavert.sentinel.UnitSpec

class DataAccessManagerTest extends UnitSpec {

  before {
    assume(DataAccessManager isReady)
  }

  it should "return a valid DAO" in {
    val dao = MongoUserDao()
    dao should not be null
  }

  it should "throw if we try to initialize again" in {
    intercept[DbException] {
      DataAccessManager.init("mongodb://fake.server/dummy-db")
    }
  }
}
