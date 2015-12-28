package chapter02

import model.po.MessageTable
import model.vo.Util
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


    // 2.10.1 Count the Messages
    // How would you count the number of messages?
    val messagesCount = exec(messages.length.result)


    // 2.10.2 Selecting a Message
    // Using a for comprehension, select the message with the id of 1.
    // What happens if you try to find a message with an id of 999?
    val messageOneQuery = for {
      message <- messages if message.id === 1L
    } yield message
    val messageOne = exec(messageOneQuery.result)

    val message999Query = for {
      message <- messages if message.id === 999L
    } yield message
    val message999 = exec(message999Query.result)


    // 2.10.3 One Liners
    // Re-write the query from the last exercise to not use a for comprehension.
    val thisOneIsBetter = exec(messages.filter(_.id === 1L).result)


    // 2.10.4 Checking the SQL
    // Calling the result.statements methods on a query will give you the SQL to be executed.
    // Apply that to the last exercise. What query is reported?
    // What does this tell you about the way filter has been mapped to SQL?
    messages.filter(_.id === 1L).result.statements foreach println
    // filter -> where


    // 2.10.5 Is HAL Real?
    // Find if there are any messages by HAL in the database, but only return a boolean value from the database.
    val isHalReal = exec(messages.filter(_.sender === "HAL").exists.result)


    // 2.10.6 Selecting Columns
    // Select all the messages in the database, but return just their contents.
    val allContents = exec(messages.map(_.content).result)
    // map -> projection


    // 2.10.7 First Result
    // The methods head and headOption are useful methods on a result. Find the first message that HAL sent.
    // What happens if you use head to find a message from “Alice” (note that Alice has sent no messages).
    val halsHead = exec(messages.filter(_.sender === "HAL").sortBy(_.id).result.head)
    // val thisThrowsException = exec(messages.filter(_.sender === "Alice").sortBy(_.id).result.head)


    // 2.10.8 Then the Rest
    // In the previous exercise you returned the first message HAL sent.
    // This me find the next five messages HAL sent. What messages are returned?
    exec(messages.filter(_.sender === "HAL").sortBy(_.id).drop(1).take(5).result)


    // 2.10.9 The Start of Something
    // The method startsWith on a String tests to see if the string starts with a particular sequence of characters.
    // Slick also implements this for string columns. Find the message that starts with “Open”.
    // How is that query implemented in SQL?
    exec(messages.filter(_.content startsWith "Open").result)
    // implemented with LIKE


    // 2.10.10 Liking
    // Slick implements the method like.
    // Find all the messages with “do” in their content. Can you make this case insensitive?
    exec(messages.filter(_.content like "%do%").result) // case sensitive
    exec(messages.filter(_.content.toUpperCase like "%DO%").result) // case insensitive


    // 2.10.11 Client-Side or Server-Side?
    // What does this do and why?
    exec(messages.map(_.content + "!").result) // exec(messages.map(m => m.content ++ "!").result)
  } finally db.close()
}
