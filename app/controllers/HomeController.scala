package controllers

import java.io.{BufferedReader, File, FileInputStream, FileReader, IOException}
import java.sql.ResultSet

import play.api.data.Form
import play.api.data.Forms._
import javax.inject._

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import database.JDBCConnection
import models.{Article, Authors}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Logger


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  val signUpForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "designation" -> nonEmptyText,
      "about" -> nonEmptyText,
      "email" -> nonEmptyText
    )(Authors.apply)(Authors.unapply))

  val articleForm=Form(
    mapping(
      "email" -> nonEmptyText,
      "title" -> nonEmptyText,
      "body" -> nonEmptyText
    )(Article.apply)(Article.unapply)
  )
  def index = {
    Action {
      val resultSetAuthor = JDBCConnection.executeQuery("select * from authors")
      val resultSetArticle = JDBCConnection.executeQuery("select * from articles")
      val listAuthor = getAuthors(resultSetAuthor, Nil)
      val listArticle = getArticles(resultSetArticle, Nil)
      Ok(views.html.index(listAuthor,listArticle))
    }
  }

  def aboutUs = {
    Action {
      Ok(views.html.aboutUs("your application is ready"))
    }
  }

  def initauthors = {
    Action {
      JDBCConnection.execute("drop table if exists authors")
      JDBCConnection
        .execute(
          "create table if not exists authors(name varchar,designation varchar,about varchar," +
          "email " +
          "varchar)")
      JDBCConnection
        .execute(
          "create table if not exists articles(email varchar,title varchar,body varchar)")
      JDBCConnection.execute("insert into authors values('name1','designation1','about1','email1')")
      JDBCConnection.execute("insert into articles values('email1','title1','body1')")
     /* JDBCConnection.execute("insert into authors values('name2','designation2','about2','email2')")
      JDBCConnection.execute("insert into authors values('name3','designation3','about3','email3')")
      JDBCConnection.execute("insert into authors values('name4','designation4','about4','email4')")
      JDBCConnection.execute("insert into authors values('name5','designation5','about5','email5')")
      JDBCConnection.execute("insert into authors values('name6','designation6','about6','email6')")
      JDBCConnection.execute("insert into authors values('name7','designation7','about7','email7')")
      JDBCConnection.execute("insert into authors values('name8','designation8','about8','email8')")
      JDBCConnection.execute("insert into authors values('name9','designation9','about9','email9')")*/

      new File("/tmp/article").mkdir()
      Ok
    }
  }

  def authors = {
    Action {
      Ok(views.html.author(signUpForm,articleForm))
    }
  }

  def errorPage = {
    Action {
      Ok(views.html.errorPage())
    }
  }

  def getAllAuthors = {
    Action {
      Logger.info("Retrieving All Authors.")
      val resultSet = JDBCConnection.executeQuery("select * from authors")
      val listAuthor = getAuthors(resultSet, Nil)
      Ok(listAuthor.mkString(","))
    }
  }

  def getAllArticles = {
    Action {
      Logger.info("Retrieving All Articles.")
      val resultSet = JDBCConnection.executeQuery("select * from articles")
      val listArticles = getArticles(resultSet, Nil)
      Ok(listArticles.mkString(","))
    }
  }

  /**
   * Create an Action for signup option
   */
  def signUp: Action[AnyContent] = {
    Action.async {
      implicit request =>
        Logger.debug("signingUp in progress. ")
        signUpForm.bindFromRequest.fold(
          formWithErrors => {
            Logger.error("Sign-up badRequest.")
            Future(BadRequest(views.html.errorPage()))
          },
          validData => {
            println(validData.name + " : " + validData.email+" : "+validData.designation+" : "+validData.about)
            if (JDBCConnection
              .execute(
                "insert into authors values('" + validData.name + "','" + validData.designation +
                "','" + validData.about + "','" + validData.email + "')")) {
              Logger.info("Author recorded successfully.")
              Future.successful(Redirect(routes.HomeController.authors()))
            }
            else {
              Logger.error("Author not recorded successfully.")
              Future.successful(Redirect(routes.HomeController.authors()))
            }
          }
        )
    }
  }

  /**
   * Create an Action for aricle upload option
   */
  def articleUpload: Action[AnyContent] = {
    Action.async {
      implicit request =>
        Logger.debug("aricle upload in progress. ")
        articleForm.bindFromRequest.fold(
          formWithErrors => {
            Logger.error("aricle upload badRequest.")
            Future(BadRequest(views.html.errorPage()))
          },
          validData => {
            if (JDBCConnection
              .execute(
                "insert into articles values('" + validData.email + "','" + validData.title +
                "','" + validData.body + "')")) {
              Logger.info("Article recorded successfully.")
              Future.successful(Redirect(routes.HomeController.authors()))
            }
            else {
              Logger.error("Article not recorded successfully.")
              Future.successful(Redirect(routes.HomeController.authors()))
            }
          }
        )
    }
  }

  def getAuthors(resultSet: ResultSet, authorList: List[Authors]): List[Authors] = {
    if (resultSet.next()) {
      val author = Authors(resultSet.getString(1),
        resultSet.getString(2),
        resultSet.getString(3),
        resultSet.getString(4))
      getAuthors(resultSet, authorList :+ author)
    } else {
      authorList
    }
  }

  def getArticles(resultSet: ResultSet, articleList: List[Article]): List[Article] = {
    if (resultSet.next()) {
      val article = Article(resultSet.getString(1),
        resultSet.getString(2),
        resultSet.getString(3))
      getArticles(resultSet, articleList :+ article)
    } else {
      articleList
    }
  }

}
