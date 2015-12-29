package model2

import slick.driver.H2Driver.api._

package object po {

  lazy val messages = TableQuery[MessageTable]
  lazy val users    = TableQuery[UserTable]
}
