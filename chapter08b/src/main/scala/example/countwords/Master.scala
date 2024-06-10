package example.countwords

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior

import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.Failure
import scala.util.Success
import example.countwords.MasterProtocol.FailedJob

object Master {
  
    def apply(workersRouter: ActorRef[WorkerProtocol.Command]): Behavior[MasterProtocol.Event] =
        Behaviors.withTimers {timers =>
            timers.startTimerWithFixedDelay(MasterProtocol.Tick, MasterProtocol.Tick, 1.second)
            working(workersRouter)
        }
    
    def working(workersRouter: ActorRef[WorkerProtocol.Command],
        countedWords: Map[String, Int] = Map(),
        lag: Vector[String] = Vector()) : Behavior[MasterProtocol.Event] = 
        Behaviors.setup[MasterProtocol.Event] {ctx => 
            
            implicit val timeout: Timeout = 3.seconds
            val parallelism = ctx.system.settings.config.getInt("example.countwords.delegation-parallelism")
            
            Behaviors.receiveMessage[MasterProtocol.Event] {
                case MasterProtocol.Tick => 
                    ctx.log.debug(s"tick, current lag ${lag.size}")

                    val text = "this simulates a very simple stream, yes, really very simple"
                    val allTexts = lag :+ text
                    val (firstTexts, secondTexts) = allTexts.splitAt(parallelism)

                    firstTexts.foreach {text =>
                        ctx.ask(workersRouter, WorkerProtocol.Process(text, _)) {
                            case Success(MasterProtocol.CountedWords(map)) =>
                                MasterProtocol.CountedWords(map)
                            case Failure(exception) => 
                                MasterProtocol.FailedJob(text)
                        }
                    }
                    working(workersRouter, countedWords, secondTexts)
                case MasterProtocol.CountedWords(map) =>
                    val merged = merge(countedWords, map)
                    ctx.log.debug(s"current count ${merged.toString}")
                    working(workersRouter, merged, lag)
                case FailedJob(text) => 
                    ctx.log.debug(s"failed, adding text to lag ${lag.size}")
                    working(workersRouter, countedWords, lag :+ text)
            }
        }
    
    def merge(
      currentCount: Map[String, Int],
      newCount2Add: Map[String, Int]): Map[String, Int] =
    (currentCount.toSeq ++ newCount2Add)
      .groupMapReduce(_._1)(_._2)(_ + _)
}
