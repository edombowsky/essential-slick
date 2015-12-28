package model.vo

final case class User(
                      username: String,
                      email: Option[String],
                      id: Long = 0L
                     )
