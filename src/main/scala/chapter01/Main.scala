package chapter01

import model.po.MessageTable
import model.vo.Util
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {

  // Base query for querying from messages table
  lazy val messages = TableQuery[MessageTable]

  // Set up the connection
  val db = Database.forConfig("h2db")

  // Helper method for running query - blocking
  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2.seconds)

  try {
    // Create a schema
    exec(messages.schema.create)

    // Insert some data
    exec(messages ++= Util.generateMessages())

    // Select messages where sender == 'Dave'
    val senderIsDave = messages.filter(_.sender === "Dave")
    exec(messages.filter(_.sender === "Dave").result) foreach println

  } finally db.close()

}
