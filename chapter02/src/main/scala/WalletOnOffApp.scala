import akka.actor.typed.ActorSystem

object WalletOnOffApp extends App {

  val guardian: ActorSystem[WalletOnOff.Command] =
    ActorSystem(WalletOnOff(), "wallet-on-off")

  guardian ! WalletOnOff.Increase(1)
  guardian ! WalletOnOff.Deactivate
  guardian ! WalletOnOff.Increase(1)
  guardian ! WalletOnOff.Activate
  guardian ! WalletOnOff.Increase(10)

  println("Press ENTER to terminate")
  scala.io.StdIn.readLine()
  guardian.terminate()
}
