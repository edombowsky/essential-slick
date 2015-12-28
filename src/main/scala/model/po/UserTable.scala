package model.po

import model.vo.User
import slick.driver.H2Driver.api._

final class UserTable(tag: Tag) extends Table[User](tag, "USERS") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def username = column[String]("USERNAME")
  def email = column[Option[String]]("EMAIL")

  def * = (username, email, id) <> (User.tupled, User.unapply)
}
