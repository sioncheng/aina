package example.persistence

import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers

class CommandLineSpec extends AnyWordSpecLike with Matchers {
  "Calling Command.apply" should {
    "create the correct AddCargo command for the given input" in {
      CommandLine.Command("a b c 1") shouldBe
        CommandLine.Command.AddCargo("a", "b", "c", 1)
    }

    "create the correct Quit command for the given input" in {
      CommandLine.Command("quit") shouldBe(CommandLine.Command.Quit)
      CommandLine.Command("exit") shouldBe(CommandLine.Command.Quit)
    }
  }
}
