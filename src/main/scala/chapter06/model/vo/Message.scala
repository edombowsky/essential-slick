package chapter06.model.vo

final case class Message(
                        content: String,
                        senderId: Long,
                        id: Long = 0L
                        )
