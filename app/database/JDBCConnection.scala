package database

import java.sql.{Connection, DriverManager, ResultSet, SQLException}

/**
 * Created by rahul on 7/5/17.
 */
object JDBCConnection {
  def getConnection():Option[Connection]={
  try
    Class.forName("org.postgresql.Driver")

  catch {
    case e: ClassNotFoundException => {
      System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!")
      e.printStackTrace()
      return None
    }
  }

  System.out.println("PostgreSQL JDBC Driver Registered!")

  var connection: Connection = null

  try
    connection = DriverManager
      .getConnection("jdbc:postgresql://localhost:5432/gulmohar", "postgres", "postgres")

  catch {
    case e: SQLException => {
      System.out.println("Connection Failed! Check output console")
      e.printStackTrace()
return None
    }
  }

  if (connection != null) {
    println("You made it, take control your database now!")
    return Some(connection)
  }
  else {
    println("Failed to make connection!")
return None
  }
}

  def executeQuery(string: String):ResultSet={
    return getConnection().get.prepareStatement(string).executeQuery()
  }

  def execute(string: String):Boolean={
    return getConnection().get.prepareStatement(string).execute()
  }

  def main(args: Array[String]): Unit = {
    getConnection()
  }

}
