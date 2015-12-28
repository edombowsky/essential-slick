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

    exec(
      users ++= Seq(User("dave", Some("dave@example.org")), User("mike", None))
    )

    require(2 == filterByEmail(None).length)
    require(1 == filterByEmail(Some("dave@example.org")).length)

  } finally db.close()
}
