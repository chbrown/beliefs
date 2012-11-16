import scala.collection.mutable.{ListBuffer,Map,Set}
import util.Random

import akka.actor._

object NoMemory extends App {

  sealed trait MemeMessage
  // messages sent to god
  case class NewDay extends MemeMessage
  case class TurnIn extends MemeMessage
  case class Report(proposition: Char, friends: Int) extends MemeMessage

  // messages sent to people
  case class Listen(proposition: Char) extends MemeMessage
  case class GoOut extends MemeMessage
  case class Summarize extends MemeMessage
  case class Meet(other: ActorRef) extends MemeMessage

  // case class Lecture(thought: Char) extends MemeMessage

  class Person(gullability: Float) extends Actor {
    val friends = Set[ActorRef]()
    var memory: Option[Char] = None // start with an empty mind

    def receive = {
      case Meet(other) =>
        friends += other
      case Listen(proposition) =>
        // we listen to a proposition, and with some probability, adopt it.
        if (Random.nextFloat < gullability)
          memory = Some(proposition)
      case GoOut =>
        memory match {
          case Some(memory_value) =>
            for (friend <- friends)
              friend ! Listen(memory_value)
          case _ =>
            // println("GoOut Failure")
        }
        // we've told it, so now we can forget it now.
        memory = None
        // tell god we're done
        sender ! TurnIn
      case Summarize =>
        val char = memory match {
          case Some(memory) => memory
          case _ => ' '
        }
        // println("[Degree %s] %s".format(friends.size, char))
        sender ! Report(char, friends.size)
    }
  }

  class God(population: Int, days: Int) extends Actor {
    val gullability = 0.8f
    val children = (0 until population).map(a => context.actorOf(Props(new Person(gullability)))).toList
    children(0) ! Listen('t')
    var day = 0
    var awake = 0
    // var reportsReceived = 0

    // add random links (edges)
    for (i <- 0 until population*2) { // pretty low degree
      val ai = Random.nextInt(population)
      val a = children(ai)
      val bi = Random.nextInt(population)
      val b = children(bi)
      if (ai != bi) {
        // introduce them
        a ! Meet(b)
        b ! Meet(a)
      }
    }

    def receive = {
      case NewDay =>
        day += 1

        println("A new day!")
        for (child <- children) {
          child ! GoOut
          awake += 1
        }

      case TurnIn =>
        awake -= 1
        if (awake == 0) {
          println("Everyone sleeps, end of day #%s".format(day))
          if (day == days - 1) {
            // is one day enough time to get reports back?
            sender ! Summarize
          }
          if (day == days) {
            println("I've heard from everybody, quitting")
            context.system.shutdown()
          }
          self ! NewDay
        }

      case Report(proposition, friends) =>
        println("[Degree %s] %s".format(friends, proposition))
    }
  }

  val akkaSystem = ActorSystem("PropSystem")
  val population = 250
  val days = 100
  val god = akkaSystem.actorOf(Props(new God(population, days)), name="god")
  god ! NewDay
}
