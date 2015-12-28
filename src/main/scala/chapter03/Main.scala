package chapter03

import model.po.MessageTable
import model.vo.{Message, Util}
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {

  // Set up the connection
  val db = Database.forConfig("h2db")

  // Helper method for running query - blocking
  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2.seconds)

  try {
    val messages = TableQuery[MessageTable]

    // Create a schema
    exec(messages.schema.create)
    // Set up some data
    exec(messages ++= Util.generateMessages())


    // 3.5.1 Methodical Inserts
    // Create a method to insert a message and return it with the id field populated.
    def persist(m: Message): Message = {
      val id = exec(messages returning messages.map(_.id) += m)
      m.copy(id = id)
    }
    println(persist(Message("Mike", "WOWOWOWOWOWOW")))


    // 3.5.2 Get to the Specifics
    // exec(messages.map(_.sender) += "HAL")
    // Rewrite the above query to include the content column.
    exec(messages.map(m => (m.sender, m.content)) += ("HAL", "WOWOWOW?"))


    // 3.5.3 Bulk All the Inserts
    // Insert the conversation below between Alice and Bob, returning the messages populated with id s.
    val conversation = List(
      Message("Bob", "Hi Alice"),
      Message("Alice", "Hi Bob"),
      Message("Bob", "Are you sure this is secure?"),
      Message("Alice", "Totally, why do you ask?"),
      Message("Bob", "Oh, nothing, just wondering."),
      Message("Alice", "Ten was too many messages"),
      Message("Bob", "I could do with a sleep"),
      Message("Alice","Let's just to to the point"),
      Message("Bob", "Okay okay, no need to be tetchy."),
      Message("Alice","Humph!")
    )
    val messagesReturningRow = messages returning messages.map(_.id) into {(message, id) => message.copy(id = id)}
    exec(messagesReturningRow ++= conversation) foreach println


    // 3.5.4 No Apologies
    // Write a query to delete messages that contain “sorry”.
    val numberOfDeletedRows = exec(messages.filter(_.content.toLowerCase like "%sorry%").delete)
    println(s"We have just deleted $numberOfDeletedRows row(s)")


    // 3.5.5 Update Using a For Comprehension
    // Rewrite the update statement below to use a for comprehension.
    //    val rowsAffected = messages.
    //      filter(_.sender === "HAL").
    //      map(msg => (msg.sender, msg.content)).
    //      update("HAL 9000", "Rebooting, please wait...")
    val updateQuery = for {
      m <- messages if m.sender === "HAL"
    } yield (m.sender, m.content)
    val rowsAffected = exec(updateQuery.update(("HAL 9000", "Rebooting, please wait...")))


    // 3.5.6
    // Selective Memory
    // Delete HAL's first two messages. This is a more difficult exercise.
    // Hint: First write a query to select the two messages. Then see if you can find a way to use it as a subquery.
    val deletedRows = exec(messages.filter(_.id in messages.filter(_.sender === "HAL 9000").sortBy(_.id).take(2).map(_.id)).delete)
    println(s"We have just deleted $deletedRows row(s)")


  } finally db.close()
}
