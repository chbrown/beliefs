import akka.actor._
import akka.routing.RoundRobinRouter
// import akka.util.Duration
// import akka.util.duration._


object PiExample extends App {

  // sealed trait PiMessage
  trait PiMessage
  // these are all just plain messages. not sure why calculate is an object. (no args?)
  case class Calculate extends PiMessage
  case class Work(start: Int, elementCount: Int) extends PiMessage
  case class Result(value: Double) extends PiMessage
  case class PiApproximation(pi: Double, duration: Long)

  class Worker extends Actor {

    def calculatePiFor(start: Int, elementCount: Int): Double = {
      var acc = 0.0
      for (i ← start until (start + elementCount))
        acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
      acc
    }

    def receive = {
      case Work(start, elementCount) ⇒
        val part_of_pi = calculatePiFor(start, elementCount) // perform the work
        // seems that sender is a special variable.
        sender ! Result(part_of_pi) // report it
    }

  }

  class Master(workerCount: Int, messageCount: Int, elementCount: Int, listener: ActorRef) extends Actor {

    var pi: Double = _
    var resultCount: Int = _
    val startMillis: Long = System.currentTimeMillis // start time

    // this is a little strange
    val workerRouter = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(workerCount)), name = "workerRouter")

    def receive = {
      case Calculate =>
        for (i ← 0 until messageCount)
          workerRouter ! Work(i * elementCount, elementCount)
      case Result(value) =>
        pi += value
        resultCount += 1
        if (resultCount == messageCount) {
          // Send the result to the listener (the actor ref given to this master)
          val endMillis: Long = System.currentTimeMillis
          listener ! PiApproximation(pi, endMillis - startMillis)
          // Stops this actor and all its supervised children
          context.stop(self)
        }
        // else {
          // println("Got %s messages, not yet.\n".format(resultCount))
        // }
    }

  }

  // god, basically. outside the system. The only one with a side effect.
  class Listener extends Actor {
    def receive = {
      case PiApproximation(pi, duration) ⇒
        println("\n\tListener says: Pi approximation: \t\t%s\n\tCalculation time: \t%s".format(pi, duration))
        context.system.shutdown()
    }
  }

  calculate(1000, 10000, 100000)

  // actors and messages ...

  def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
    // Create an Akka system
    val akkaSystem = ActorSystem("PiSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = akkaSystem.actorOf(Props[Listener], name = "listener")

    // create the master
    val MasterT = new Master(nrOfWorkers, nrOfMessages, nrOfElements, listener)
    val master = akkaSystem.actorOf(Props(MasterT), name = "master")

    // start the calculation
    master ! Calculate

  }
}
