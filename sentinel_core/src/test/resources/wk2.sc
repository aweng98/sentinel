
val s ="user=marco;hash=fferzfer=="

val pairs = scala.collection.mutable.Map[String, String]()
val segments = s.split(";").map(t => t.split("=")).foreach(pair =>
  pairs += (pair(0) -> pair(1)))

pairs

val foos = List("fofo", "babo", "cuco")

("" /: foos)(_ + _.toUpperCase)


