// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.persistence

import com.mongodb.casbah.Imports.ObjectId

trait HasId {
  var id: Option[ObjectId] = None

  def setId(id: ObjectId) {
    this.id = if (id != null) Some(id) else None
  }
}
