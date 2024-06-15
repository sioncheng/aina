package example.sharding

import akka.actor.testkit.typed.scaladsl.{LogCapturing, ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ContainerSpec
  extends ScalaTestWithActorTestKit
  with AnyWordSpecLike
  with Matchers
  with LogCapturing{

  "a sharded freight entity" should {
    "be able to add cargo "  in {

      val sharding = ClusterSharding(system)
      val entityDef = Entity(ContainerProtocol.typeKey)(createBehavior =
        entityContext => Container(entityContext.entityId))
      val shardRegion : ActorRef[ShardingEnvelope[ContainerProtocol.Command]] =
        sharding.init(entityDef)

      val containerId = "id-1"
      val cargo = ContainerProtocol.Cargo("id-c", "sack", 3)

      shardRegion ! ShardingEnvelope(containerId, ContainerProtocol.AddCargo(cargo))

      val probe: TestProbe[List[ContainerProtocol.Cargo]] = createTestProbe[List[ContainerProtocol.Cargo]]

      // visit target by entity ref
      val container: EntityRef[ContainerProtocol.Command] =
        sharding.entityRefFor(ContainerProtocol.typeKey, containerId)

      container ! ContainerProtocol.GetCargos(probe.ref)
      probe.expectMessage(List(cargo))

      // visit target by shard region
      shardRegion ! ShardingEnvelope(containerId, ContainerProtocol.GetCargos(probe.ref))
      probe.expectMessage(List(cargo))

    }
  }

}
