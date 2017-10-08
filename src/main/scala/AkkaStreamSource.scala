import slick.jdbc.H2Profile.api._
import com.bayakala.funda._
import api._

import scala.language.implicitConversions
import scala.concurrent.duration._
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.stage._
import slick.basic.DatabasePublisher
import akka._
import fs2._
import akka.stream.stage.{GraphStage, GraphStageLogic}

object KillSwitch {
  var callback: AsyncCallback[Unit] = null
  def kill = {
    if (callback != null) {
      callback.invoke(())
    }
  }
}
object Injector {
  var callback: AsyncCallback[String] = null
  def inject(m: String) = {
    if (callback != null) {
      callback.invoke(m)
    }
  }
}

class FS2Gate[T](killSwitch: KillSwitch.type, take: Int, q: fs2.async.mutable.Queue[Task,Option[T]]) extends GraphStage[SinkShape[T]] {
  val in = Inlet[T]("inport")
  val shape = SinkShape.of(in)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with InHandler {
      override def preStart(): Unit = {
        if (killSwitch != null) {
          val callback = getAsyncCallback[Unit] { (_) =>
            killStream = true
          }
          killSwitch.callback = callback
        }
        pull(in)          //initiate stream elements movement
        super.preStart()
      }
      var take_ = take
      var killStream = false
      override def onPush(): Unit = {
        if (killStream) {
          take_ = -1
          println("killing......")
        }
        q.enqueue1{
          if ( take_ > 0 )
             Some(grab(in))
          else
            None
        }.unsafeRun()
        pull(in)
        if ( take_ <= 0) completeStage()
        take_ -= 1

      }

      override def onUpstreamFinish(): Unit = {
        q.enqueue1(None).unsafeRun()
        println("end of stream !!!!!!!")
        completeStage()
      }

      override def onUpstreamFailure(ex: Throwable): Unit = {
        q.enqueue1(None).unsafeRun()
       completeStage()
      }

      setHandler(in,this)

    }
}

object AkkaStreamSource extends App {

  val aqmraw = Models.AQMRawQuery
  val db = Database.forConfig("h2db")
  // aqmQuery.result returns Seq[(String,String,String,String)]
  val aqmQuery = aqmraw.map {r => (r.year,r.state,r.county,r.value)}
  // type alias
  type RowType = (String,String,String,String)
  // user designed strong typed resultset type. must extend FDAROW
  case class TypedRow(year: String, state: String, county: String, value: String) extends FDAROW
  // strong typed resultset conversion function. declared implicit to remind during compilation
  implicit def toTypedRow(row: RowType): TypedRow =
    TypedRow(row._1,row._2,row._3,row._4)
  // construct DatabasePublisher from db.stream
  val dbPublisher: DatabasePublisher[RowType] = db.stream[RowType](aqmQuery.result)
  // construct akka source
  val source: Source[RowType,NotUsed] = Source.fromPublisher[RowType](dbPublisher)

  implicit val actorSys = ActorSystem("actor-system")
  implicit val ec = actorSys.dispatcher
  implicit val mat = ActorMaterializer()


/*
  source.take(10).map{row => toTypedRow(row)}.runWith(
    Sink.foreach(qmr => {
      println(s"州名: ${qmr.state}")
      println(s"县名：${qmr.county}")
      println(s"年份：${qmr.year}")
      println(s"取值：${qmr.value}")
      println("-------------")
    })).onComplete { case _ => println("hello!!");actorSys.terminate()}



*/
  def fs2Stream(ks: KillSwitch.type,take: Int): Stream[Task,RowType] = Stream.eval(async.boundedQueue[Task,Option[RowType]](2))
    .flatMap { q =>
      Task(source.to(new FS2Gate[RowType](ks, take, q)).run).unsafeRunAsyncFuture  //enqueue Task(new thread)
      pipe.unNoneTerminate(q.dequeue)      //dequeue in current thread
    }
     .onFinalize{Task.delay{actorSys.terminate();println("end of stream!")}}


  fs2Stream(KillSwitch,Int.MaxValue).map{row => toTypedRow(row)}
    .map { qmr =>
      if (qmr.value.toString == "15") {
        KillSwitch.kill
      }
      qmr
    }
    .map(qmr => {
      println(s"州名: ${qmr.state}")
      println(s"县名：${qmr.county}")
      println(s"年份：${qmr.year}")
      println(s"取值：${qmr.value}")
      println("-------------")
    })
     .run.unsafeRun

 // scala.io.StdIn.readLine()
 // actorSys.terminate()

}