// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel

import com.mongodb.casbah.Imports.ObjectId

package object model {

  def idEquals(x: Option[ObjectId], y: Option[ObjectId]) = {
    if ((x == None) && (y == None)) true
    else x.exists(n => y.exists(_ == n))

  }
}
