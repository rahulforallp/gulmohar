package controllers

import java.sql.ResultSet
import java.util
import javax.inject._

import scala.collection.mutable

import database.JDBCConnection
import models.Authors
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    val resultSet = JDBCConnection.executeQuery("select * from authors")
    val listAuthor = getAuthors(resultSet, Nil)
    Ok(views.html.index(listAuthor.mkString(",")))
  }
  def aboutUs = Action {
    Ok(views.html.aboutUs("your application is ready"))
  }

  def initauthors = Action {
     JDBCConnection.execute("drop table if exists authors")
     JDBCConnection.execute("create table if not exists authors(name varchar,designation varchar,about varchar,email varchar)")
     JDBCConnection.execute("insert into authors values('name1','designation1','about1','email1')")
     JDBCConnection.execute("insert into authors values('name2','designation2','about2','email2')")
     JDBCConnection.execute("insert into authors values('name3','designation3','about3','email3')")
     JDBCConnection.execute("insert into authors values('name4','designation4','about4','email4')")
     JDBCConnection.execute("insert into authors values('name5','designation5','about5','email5')")
     JDBCConnection.execute("insert into authors values('name6','designation6','about6','email6')")
     JDBCConnection.execute("insert into authors values('name7','designation7','about7','email7')")
     JDBCConnection.execute("insert into authors values('name8','designation8','about8','email8')")
     JDBCConnection.execute("insert into authors values('name9','designation9','about9','email9')")
    Ok
  }


  def authors = Action {
    val resultSet = JDBCConnection.executeQuery("select * from authors")
    val listAuthor = getAuthors(resultSet, Nil)
    println(listAuthor.head.name+"====================")
    Ok(listAuthor.mkString(","))
  }

  def getAuthors(resultSet: ResultSet, authorList:List[Authors]): List[Authors] = {
      if (resultSet.next()) {
        val author = Authors(resultSet.getString(1),resultSet.getString(2),resultSet.getString(3),resultSet.getString(4))
        getAuthors(resultSet, authorList :+ author)
      } else {
        authorList
      }
  }

}
