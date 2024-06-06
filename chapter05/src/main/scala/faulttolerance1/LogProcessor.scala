package faulttolerance1

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import faulttolerance1.exception.CorruptedFileException
import akka.actor.typed.SupervisorStrategy

import java.io.File

object LogProcessor {

    sealed trait Command
    final case class LogFile(file: File) extends Command

    def apply(dbWriter: ActorRef[DbWriter.Command]): Behavior[Command] = 
        Behaviors.supervise {
            Behaviors.receiveMessage[Command] {
                case LogFile(file) => ???
            }
        }
        .onFailure[CorruptedFileException](SupervisorStrategy.resume)
}