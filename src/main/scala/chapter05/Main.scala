package chapter05

import model.po.{MessageTable, UserTable}
import model.vo.{Message, User, Util}
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {

  // Set up the connection
  val db = Database.forConfig("h2db")

  // Helper method for running query - blocking
  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2.seconds)

  try {
    val users = TableQuery[UserTable]

    // Create a schema
    exec(users.schema.create)
    // Add some users
    exec(
      users ++= Seq(User("dave", Some("dave@example.org")), User("mike", None))
    )


    // 5.3.7.1 Filtering Optional Columns
    // Working with the optional email address for a user, write a method that will take an optional value, and list rows
    //    matching that value.
    // The method signature is:
    //    def filterByEmail(email: Option[String]) = ???
    // Assume we only have two user records: one with an email address of “dave@example.org”, and one with no email address.
    // We want filterByEmail(Some("dave@example.org")) to produce one row, and filterByEmail(None) to produce two rows.
    // Tip: it’s OK to use multiple queries.
    def filterByEmail(email: Option[String]) = email match {
      case Some(x) => exec(users.filter(_.email === x).result)
      case None => exec(users.result)
    }
    require(2 == filterByEmail(None).length)
    require(1 == filterByEmail(Some("dave@example.org")).length)


    // 5.3.7.2 Inside the Option
    // Build on the last exercise to match rows that start with the supplied optional value.
    // Recall that Rep[String] defines startsWith.
    // So this time even filterByEmail(Some("dave@")).run will produce one row.
    def filterByEmail2(email: Option[String]) = email match {
      case Some(x) => exec(users.filter(_.email startsWith x).result)
      case None => exec(users.result)
    }
    require(2 == filterByEmail2(None).length)
    require(1 == filterByEmail2(Some("dave@example.org")).length)
    require(1 == filterByEmail2(Some("dave@")).length)


    // 5.3.7.3 Matching or Undecided
    // Not everyone has an email address, so perhaps when filtering it would be safer to only exclude rows that don’t
    //    match our filter criteria. That is, keep NULL addresses in the results.
    // Add Elena to the database...
    //    insert += User("Elena", Some("elena@example.org"))
    // ...and modify filterByEmail so when we search for Some("elena@example.org") we only exclude Dave, as he definitely doesn’t match that address.
    // This me you can do this in one query.
    exec(users += User("Elena", Some("elena@example.org")))
    def filterByEmail3(email: Option[String]) =
      exec(users.filter(user => user.email.isEmpty || user.email === email).result)
    require(1 == filterByEmail3(None).length)
    require(2 == filterByEmail3(Some("elena@example.org")).length)


  } finally db.close()
}
