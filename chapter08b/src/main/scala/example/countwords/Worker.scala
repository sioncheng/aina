package example.countwords

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.Receptionist

object Worker {

    def apply() = Behaviors.setup[WorkerProtocol.Command] {ctx =>
        ctx.log.debug(s"${ctx.self} subscribing to ${WorkerProtocol.RegistrationKey}")
        ctx.system.receptionist ! Receptionist.Register(WorkerProtocol.RegistrationKey, ctx.self)

        Behaviors.receiveMessage {
            case WorkerProtocol.Process(text, replyTo) =>
                ctx.log.debug(s"processing $text")
                replyTo ! MasterProtocol.CountedWords(processTask(text))
                Behaviors.same
        }
    }

    def processTask(text: String): Map[String, Int] = {
        text
            .split("\\W+")
            .foldLeft(Map.empty[String, Int]) { (acc, word) => acc + (word -> (acc.getOrElse(word, 0) + 1))}
    }
}
