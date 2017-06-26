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
      .getConnection("jdbc:postgresql://ec2-54-163-254-76.compute-1.amazonaws.com:5432/dfo9pun113vlcu?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory", "afhsidxzoxwlve", "43d62666ac459b5a8b5064ba09c304229bc2e5019cba0956f27b214e47516678")

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
    val connection = getConnection()
    if (connection !=None) {
    val resultSet = connection.get.prepareStatement(string).executeQuery()
    connection.get.close()
    return resultSet
  }
  else{
    throw new Exception("Connection Refused.")
  }
  }

  def execute(string: String):Boolean={
    val connection = getConnection()
    if (connection !=None) {
      val resultSet = !(connection.get.prepareStatement(string).execute())
      connection.get.close()
      return resultSet
    }
    else{
      throw new Exception("Connection Refused.")
    }
  }

  def main(args: Array[String]): Unit = {
    getConnection()
  }

}
