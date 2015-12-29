package model2.po

import model2.vo._
import slick.driver.H2Driver.api._

final class UserTable(tag: Tag) extends Table[User](tag, "USER") {

  def id    = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def name  = column[String]("NAME")
  def email = column[String]("EMAIL")

  def *     = (name, email, id) <> (User.tupled, User.unapply)
}
