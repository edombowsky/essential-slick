package chapter06.model.po

import chapter06.model.vo._
import slick.driver.H2Driver.api._

final class MessageTable(tag: Tag) extends Table[Message](tag, "MESSAGE") {

  def id        = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def senderId  = column[Long]("SENDER_ID")
  def content   = column[String]("CONTENT")

  def *         = (content, senderId, id) <> (Message.tupled, Message.unapply)

  def sender    = foreignKey("FK_SENDER", senderId, users)(_.id)
}
