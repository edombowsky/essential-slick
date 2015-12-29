package chapter07

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


    // 7.5.1 Plain Selects
    // Write the following four queries as Plain SQL queries:
    //  • Count the number of rows in the message table.
    exec(sql"SELECT count(*) FROM message".as[Int])
    //  • Select the content from the messages table.
    exec(sql"SELECT content FROM message".as[String])
    //  • Select the length of each message (“content”) in the messages table.
    exec(sql"SELECT length(content) FROM message".as[Int])
    //  • Select the content and length of each message.
    exec(sql"SELECT content, length(content) FROM message".as[(String, Int)])


    // 7.5.2 Conversion
    // Convert the following lifted embedded query to a Plain SQL query.
    //    val whoSaidThat =
    //      messages.join(users).on(_.senderId === _.id).
    //      filter{ case (message,user) => message.content === "Open the pod bay doors, HAL."}.
    //      map{ case (message,user) => user.name }
    //    exec(whoSaidThat.result)
    //    res1: Seq[String] = Vector(Dave)
    exec(
      sql"""
           SELECT u.name
           FROM message m JOIN user u ON m.sender_id = u.id
           WHERE m.content like '%!%'
        """.as[String]
    )


    // 7.5.3 Substitution
    // Complete the implementation of this method using a Plain SQL query
    //    def whoSaid(content: String): DBIO[Seq[String]] = ???
    //    exec(whoSaid("Open the pod bay doors, HAL."))
    //    res1: Seq[String] = Vector(Dave)
    // This should be a small change to your solu on to the last exercise.
    def whoSaid(content: String): DBIO[Seq[String]] =
      sql"""
           SELECT u.name
           FROM message m JOIN user u ON m.sender_id = u.id
           WHERE m.content = $content
        """.as[String]
    exec(whoSaid("No."))


    // 7.5.5 Plain Change
    // We can use Plain SQL to modify the database. That means inserting rows, updating rows, deleting rows, and also modifying the schema.
    // Go ahead and create a new table, using Plain SQL, to store the crew’s jukebox playlist. Just store a song title.
    // Insert a row into the table.
    exec(sql"CREATE TABLE PLAYLIST (TITLE VARCHAR2(200))".asUpdate)
    exec(sql"INSERT INTO PLAYLIST (TITLE) VALUES ('Try Honesty')".asUpdate)


  } finally db.close()
}
