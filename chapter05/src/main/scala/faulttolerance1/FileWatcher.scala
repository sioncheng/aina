package faulttolerance1

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import java.io.File
import faulttolerance1.exception.CorruptedFileException
import akka.actor.typed.SupervisorStrategy

object FileWatcher {

    sealed trait Command
    final case class NewFile(file: File, timeAdded: Long)
        extends Command
    
    def apply (
        directory: String,
        logProcessor: ActorRef[LogProcessor.Command]
    ): Behavior[Command] = {
        Behaviors.supervise {
            Behaviors.setup[Command] { context =>
                Behaviors.receiveMessage[Command] {
                    case NewFile(file, timeAdded) => ???
                }
            }
        }
        .onFailure[CorruptedFileException](SupervisorStrategy.resume)
    }
}