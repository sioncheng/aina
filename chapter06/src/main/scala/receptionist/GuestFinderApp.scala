package receptionist

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory

object AppRoot {

    def apply() : Behavior[ActorRef[VIPGuest.Command]] = Behaviors.setup {ctx => {
        
        Behaviors.receiveMessage[ActorRef[VIPGuest.Command]] { message => {
            ctx.log.info(s"receiveMessage ${message}")
            Behaviors.same
        }}
    }}
}

object GuestFinderApp extends App {

    private val logger = LoggerFactory.getLogger("GuestFinderApp")

    val root = ActorSystem(AppRoot(), "root")
    val vip1 = root.systemActorOf(VIPGuest(), "vip1")
    val vip2 = root.systemActorOf(VIPGuest(), "vip2")
    vip1 ! VIPGuest.EnterHotel
    vip2 ! VIPGuest.EnterHotel

    val finder = root.systemActorOf(GuestFinder(), "finder")
    finder ! GuestFinder.Find("vip", root)

    scala.io.StdIn.readLine()


    vip1 ! VIPGuest.LeaveHotel
    Thread.sleep(1000L)
    finder ! GuestFinder.Find("vip", root)

    scala.io.StdIn.readLine()

    root.terminate()
}
