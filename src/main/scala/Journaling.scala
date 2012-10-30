import scala.collection.mutable.{ListBuffer,Map,Set}
import util.Random
import akka.actor._

object Journaling extends App {

  sealed trait MemeMessage
  case class NewDay extends MemeMessage
  case class WakeUp extends MemeMessage
  case class Sleep extends MemeMessage
  case class Meet(other: ActorRef) extends MemeMessage
  case class Lecture(thought: Char) extends MemeMessage
  case class Summarize extends MemeMessage
  case class Report(notebook: String, friends: Int) extends MemeMessage

  class Person extends Actor {
    val friends = Set[ActorRef]()
    val notebook = ListBuffer[Char]()

    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    def receive = {
      case WakeUp =>
        val thought = alphabet(Random.nextInt(26))
        notebook += ' '
        notebook += thought
        for (friend <- friends)
          friend ! Lecture(thought)
        sender ! Sleep
      case Meet(other) =>
        friends += other
        // "this" is a Person, "self" is an ActorRef (special, like "sender")
        // other ! Meet(self) --- oooh, bad. recursive, neverending loop.
      case Lecture(thought) =>
        notebook += thought.toString.toLowerCase.charAt(0)
      case Summarize =>
        sender ! Report(notebook.mkString, friends.size)
    }

  }

  class God(population: Int, days: Int) extends Actor {
    val children = (0 until population).map(a => context.actorOf(Props[Person], name="child"+a)).toList
    var day = 0
    var awake = 0
    var reports = 0

    // add random links (edges)
    for (i <- 0 until population*2) {
      val ai = Random.nextInt(population)
      val a = children(ai)
      val bi = Random.nextInt(population)
      val b = children(bi)
      if (ai != bi) {
        // introduce.
        a ! Meet(b)
        b ! Meet(a)
      }
    }

    def receive = {
      case NewDay =>
        day += 1

        println("A new day!")
        for (child <- children) {
          if (day <= days)
            child ! WakeUp
          else
            child ! Summarize
          awake += 1
        }

      case Sleep =>
        awake -= 1
        if (awake == 0) {
          println("Everyone sleeps, end of day #%s".format(day))
          self ! NewDay
        }

      case Report(notebook, friends) =>
        println("""[Degree %s] %s""".format(friends, notebook))
        reports += 1
        if (reports == population) {
          // Stops this actor and all its supervised children
          println("I've heard from everybody, quitting")
          context.system.shutdown()
        }
    }
  }

  val akkaSystem = ActorSystem("PiSystem")
  val god = akkaSystem.actorOf(Props(new God(25, 4)), name="god")
  god ! NewDay
}
