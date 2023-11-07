import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object WalletState {

  sealed trait Command
  final case class Increase(amount: Int) extends Command
  final case class Decrease(amount: Int) extends Command

  def apply(total: Int, max: Int): Behavior[Command] =
    Behaviors.receive { (context, command) =>
      command match {
        case Increase(amount) =>
          val current = total + amount
          if (current < max) {
            context.log.info(s"increasing to $current")
            apply(current, max)
          } else {
            context.log.info(
              s"I am overloaded. $current will beyond $max.")
            Behaviors.stopped
          }
        case Decrease(amount) =>
          val current = total - amount
          if (current > 0) {
            context.log.info(s"decreasing to $current")
            apply(current, max)
          } else {
            context.log.info(s"I am overloaded. $current below zero.")
            Behaviors.stopped
          }
      }
    }
}
