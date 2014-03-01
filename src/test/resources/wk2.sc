import com.alertavert.sentinel.model.{Edit, Action}
object foo {
  var perms = scala.collection.mutable.Set[Action]()
  def baz() {
    perms += Edit.action
    println(perms)
  }
}

foo baz


