package core

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class HelloSpec extends FlatSpec with Matchers {

  "Hello" should "have tests" in {
    true should === (true)
  }
}
