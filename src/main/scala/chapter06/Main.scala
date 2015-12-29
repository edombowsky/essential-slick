package chapter06

import model2.po._
import model2.vo._
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {

  // Set up the connection
  val db = Database.forConfig("h2db")

  // Helper method for running query - blocking
  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2.seconds)

  try {
    exec(users.schema.create >> messages.schema.create)
    exec(users ++= Seq(
      User("mike", "m@email.com"),
      User("somebody", "sss@sss.com")
    ))
    exec(messages ++= Seq(
      Message("Oh. Oh!", 2L),
      Message("No.", 1L)
    ))


    // 6.9.1 Name of the Sender
    // Each message is sent by someone. That is, the messages.senderId will have a matching row via users.id .
    // • Write a monadic join to return all Message rows and the associated User record for each of them.
    val messagesAndUsers = for {
      message <- messages
      user <- message.sender
    } yield (message, user)
    exec(messagesAndUsers.result)
    // • Change your answer to just return the content of a message and the name of the sender.
    val messagesAndUsers2 = for {
      message <- messages
      user <- message.sender
    } yield (message.content, user.name)
    exec(messagesAndUsers2.result)
    // • Modify the query to return the results in name order.
    exec(messagesAndUsers2.sortBy{ case (content, name) => name }.result)
    // • Re-write the query as an applicative join.
    val messagesAndUsers4 = messages.
      join(users).on(_.senderId === _.id).
      map {case(m, u) => (m.content, u.name)}.
      sortBy {case(c, n) => n}
    exec(messagesAndUsers4.result)


    // 6.9.2 Messages of the Sender
    // Write a method to fetch all the message sent by a particular user. The signature is:
    //    def findByName(name: String): Query[Rep[Message], Message, Seq] = ???
    def findByName(name: String) = for {
      message <- messages
      user <- message.sender
      if user.name === name
    } yield message
    exec(findByName("mike").result)


    // 6.9.3 Having Many Messages
    // Modify the msgsPerUser query...
    //  val msgsPerUser =
    //    messages.join(users).on(_.senderId === _.id).
    //    groupBy { case (msg, user) => user.name }.
    //    map { case (name, group) => name -> group.length }
    // to return the counts for just those users with more than 2 messages.
    val msgsPerUser = messages.
      join(users).on(_.senderId === _.id).
      groupBy { case (m, u) => u.name }.
      map { case (name, group) => name -> group.length }.
      filter(_._2 > 2)
    exec(msgsPerUser.result)


  } finally db.close()
}
