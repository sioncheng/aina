val AkkaVersion = "2.6.20"
val LogbackVersion = "1.2.3"
val ScalaVersion = "2.13.14"
val AkkaManagementVersion = "1.1.4"
val AkkaProjectionVersion = "1.2.2"
val ScalikeJdbcVersion = "3.5.0"
val AkkaHttpVersion = "10.2.9"
val AkkaGRPC = "2.0.0"
val ScalaTest = "3.1.4"
val JacksonVersion = "2.11.4" 
val AkkaStreamAlpakka = "4.0.0"
val AkkaStreamKafka = "3.0.1"

lazy val chapter02 = project
  .in(file("chapter02"))
  .settings(
    scalaVersion := ScalaVersion,
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % ScalaTest % Test,
      )
    )

lazy val messages = project
  .in(file("messages"))
  .settings(
    scalaVersion := ScalaVersion,
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % ScalaTest % Test,
      )
    )

lazy val chapter03 = project
  .in(file("chapter03"))
  .settings(
    scalaVersion := ScalaVersion,
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % ScalaTest % Test,
      )
    )

lazy val chapter04 = project
  .in(file("chapter04"))
  .settings(
    scalaVersion := ScalaVersion,
        scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % ScalaTest % Test,
    ))

lazy val chapter05 = project
  .in(file("chapter05"))
  .settings(
    scalaVersion := ScalaVersion,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % ScalaTest % Test,
    ))


lazy val chapter06 = project
  .in(file("chapter06"))
  .settings(
    scalaVersion := ScalaVersion,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % ScalaTest % Test,
    ))

lazy val chapter07 = project
  .in(file("chapter07"))
  .settings(
    scalaVersion := ScalaVersion,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % ScalaTest % Test,
    ))

ThisBuild / watchTriggeredMessage := Watch.clearScreenOnTrigger