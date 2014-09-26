// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

import com.alertavert.sentinel.model.{Resource, User}
import com.alertavert.sentinel.persistence.DataAccessManager
import com.alertavert.sentinel.persistence.mongodb.MongoUserDao
import com.alertavert.sentinel.security.{Permission, Edit, Delete}
DataAccessManager.init("mongodb:///test")






val dao = MongoUserDao()
val marco = User.builder("marco") build()

val admin = User.builder("admin") build()
dao << admin
val preciousAsset = new Resource(admin)
preciousAsset.allowedActions += Edit()
Permission.grant(List(Edit(), Delete()), preciousAsset, marco)
val id = dao << marco
println("user ID: " + id)

val mm = dao.find(id).get
println("Permissions: " + mm.perms)

