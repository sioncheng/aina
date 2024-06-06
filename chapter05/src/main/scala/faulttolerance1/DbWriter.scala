package faulttolerance1

import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.PreRestart
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.SupervisorStrategy
import faulttolerance1.exception.DbBrokenConnectionException
import faulttolerance1.exception.DbNodeDownException
import akka.actor.TypedActor


object DbWriter {

  sealed trait Command
  final case class Line (time: Long, message:String, messageType:String) extends Command

  def apply(databaseUrl: String): Behavior[Command] = 
    supervisonStrategy {
        Behaviors.setup[Command] {context =>
            Behaviors.receiveMessage[Command] {
                case Line(t, m, mt) => ???
            }
            .receiveSignal {
                case(_, PostStop) => ???
                case(_, PreRestart) => ???
            }
        }
    }  

  def supervisonStrategy(beh: Behavior[Command]): Behavior[Command] = 
    Behaviors.supervise {
        Behaviors.supervise {
            beh
        }
        .onFailure[DbBrokenConnectionException](SupervisorStrategy.restart)
    }
    .onFailure[DbNodeDownException](SupervisorStrategy.stop)
}