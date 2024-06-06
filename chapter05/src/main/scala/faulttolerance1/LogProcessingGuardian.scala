package faulttolerance1

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import akka.actor.typed.Terminated


object LogProcessingGuardian {

    def apply(sources: Vector[String], databaseUrl: String) = 
        Behaviors.setup[Nothing] { context =>
            sources.foreach { source => 
                val dbWriter: ActorRef[DbWriter.Command] = 
                    context.spawnAnonymous(DbWriter(databaseUrl))
                val logProcessor: ActorRef[LogProcessor.Command] = 
                    context.spawnAnonymous(LogProcessor(dbWriter))
                val fileWatcher: ActorRef[FileWatcher.Command] = 
                    context.spawnAnonymous(FileWatcher(source, logProcessor))
                
                context.watch(fileWatcher)
            }
            Behaviors.receiveMessage[Nothing] {
                case _: Any => 
                    Behaviors.ignore
            }.receiveSignal {
                case(context, Terminated(actorRef)) =>
                    Behaviors.same
            }
        }
}