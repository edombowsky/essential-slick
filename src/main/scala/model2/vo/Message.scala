package model2.vo

final case class Message(
                        content: String,
                        senderId: Long,
                        id: Long = 0L
                        )
