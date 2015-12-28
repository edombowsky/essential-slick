package model.po

import model.vo.Message
import slick.driver.H2Driver.api._

final class MessageTable(tag: Tag) extends Table[Message](tag, "MESSAGE") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def sender = column[String]("SENDER")
  def content = column[String]("CONTENT")

  def * = (sender, content, id) <> (Message.tupled, Message.unapply)
}
