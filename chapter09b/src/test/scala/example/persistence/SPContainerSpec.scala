package example.persistence

import akka.actor.testkit.typed.scaladsl.{LogCapturing, ScalaTestWithActorTestKit}
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SPContainerSpec
  extends ScalaTestWithActorTestKit(
    EventSourcedBehaviorTestKit.config.withFallback(
      ConfigFactory.load("application-test")
    )
  )
  with AnyWordSpecLike
  with Matchers
  with LogCapturing {

  "a persistent entity with sharding" should {

    "be able to add container" in {
      println(s"###### $system ######")

      val sharding = ClusterSharding(system)
      val entityDef = Entity(SPContainer.typeKey)(entityCtx => SPContainer(entityCtx.entityId))
      val shardRegion: ActorRef[ShardingEnvelope[SPContainer.Command]] =
        sharding.init(entityDef)

      val containerId = "123"
      val cargo = SPContainer.Cargo("id-c", "sack", 3)

      shardRegion ! ShardingEnvelope(containerId, SPContainer.AddCargo(cargo))

      val probe = createTestProbe[List[SPContainer.Cargo]]()
      val container: EntityRef[SPContainer.Command] =
        sharding.entityRefFor(SPContainer.typeKey, containerId)
      container ! SPContainer.GetCargos(probe.ref)

      probe.expectMessage(List(cargo))
    }
  }
}
