// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.persistence

import java.util.Date
import com.alertavert.sentinel.model.User

trait HasCreator {
  var createdBy: Option[User] = None
  var createdAt: Date = new Date
}
