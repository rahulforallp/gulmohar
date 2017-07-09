package controllers

import java.io.{BufferedReader, File, FileInputStream, FileReader, IOException}
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.Calendar

import play.api.data.Form
import play.api.data.Forms._
import javax.inject._

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import database.JDBCConnection
import models.{Article, Authors, ForgotPassword, Subscribe}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

import mail.Mailer
import play.api.Logger
import play.api.libs.mailer.MailerClient


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val messagesApi: MessagesApi,mailerClient:MailerClient) extends Controller with I18nSupport {

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
      "email" -> nonEmptyText,
      "password" -> default(text,s"${Random.alphanumeric take 5 mkString("")}")
    )(Authors.apply)(Authors.unapply))

  val articleForm=Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "title" -> nonEmptyText,
      "body" -> nonEmptyText,
      "postTime" -> default(text,new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(Calendar.getInstance().getTime)),
      "likes" -> default(text,"0")
    )(Article.apply)(Article.unapply)
  )

  val forgotPasswordForm = Form(
    mapping(
      "email" -> nonEmptyText
    )(ForgotPassword.apply)(ForgotPassword.unapply)
  )

  val subscribeForm = Form(
    mapping(
      "email" -> text
    )(Subscribe.apply)(Subscribe.unapply)
  )

  def index = {
    Action {
      val resultSetAuthor = JDBCConnection.executeQuery("select * from authors")
      val resultSetArticle = JDBCConnection.executeQuery("select * from articles")
      val listAuthor = getAuthors(resultSetAuthor, Nil)
      val listArticle = getArticles(resultSetArticle, Nil)
      Ok(views.html.index(listAuthor,listArticle,subscribeForm))
    }
  }

  def aboutUs = {
    Action {
      Ok(views.html.aboutUs("your application is ready"))
    }
  }

  def likeBlog(title:String)={
    Action{
    val resultSet = JDBCConnection.executeQuery("select likes from articles where title='"+title+"'")
      resultSet.next()
      val newLikes = (resultSet.getString(1).toInt+1)
    JDBCConnection.execute("update articles set likes ='"+newLikes+"' where title ='"+title+"'")
    Redirect(routes.HomeController.showBlog(title))
    }
  }

  def showBlog(title:String) = {
    Action {
      val resultSet = JDBCConnection.executeQuery("select * from articles where title='"+title+"'")
      val listArticle = getArticles(resultSet,Nil)
      Ok(views.html.blog(listArticle.head))
    }
  }

  def initauthors = {
    Action {
      JDBCConnection.execute("drop table if exists authors")
      JDBCConnection.execute("drop table if exists articles")
      JDBCConnection
        .execute(
          "create table if not exists authors(name varchar,designation varchar,about varchar, email varchar PRIMARY KEY,password varchar)")
      JDBCConnection
        .execute(
          "create table if not exists articles(email varchar,title varchar PRIMARY KEY,body varchar,blogTime varchar,likes varchar)")
      JDBCConnection
        .execute(
          "create table if not exists subscriber(email varchar PRIMARY KEY)")
      /*JDBCConnection.execute("insert into authors values('name1','designation1','about1','email1')")
      JDBCConnection.execute("insert into articles values('email1','title1','body1','postTime','0')")
      JDBCConnection.execute("insert into authors values('name2','designation2','about2','email2')")
      JDBCConnection.execute("insert into authors values('name3','designation3','about3','email3')")
      JDBCConnection.execute("insert into authors values('name4','designation4','about4','email4')")
      JDBCConnection.execute("insert into authors values('name5','designation5','about5','email5')")
      JDBCConnection.execute("insert into authors values('name6','designation6','about6','email6')")
      JDBCConnection.execute("insert into authors values('name7','designation7','about7','email7')")
      JDBCConnection.execute("insert into authors values('name8','designation8','about8','email8')")
      JDBCConnection.execute("insert into authors values('name9','designation9','about9','email9')")
*/
      Ok("You have ruined the data to initial level.")
    }
  }

  def authors = {
    Action {
      Ok(views.html.author(signUpForm,articleForm))
    }
  }
var description="Something went wrong."
  def errorPage = {
    Action {
      Ok(views.html.errorPage(description))
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

  def getAllSubscribers = {
    Action {
      Logger.info("Retrieving All Subscribers.")
      val resultSet = JDBCConnection.executeQuery("select * from subscriber")
      val listSubscriber = getSubscribers(resultSet, Nil)
      Ok(listSubscriber.mkString(","))
    }
  }

  def doSubscribe: Action[AnyContent] = {
    Action.async {
      implicit request =>
        Logger.debug("Subscription in progress. ")
        subscribeForm.bindFromRequest.fold(
          formWithErrors => {
            Logger.error("Subscription badRequest.")
            Future(BadRequest(views.html.errorPage("Something went wrong")))
          },
          validData => {
            try {
              if (JDBCConnection
                .execute(
                  "insert into subscriber values('" + validData.email + "')")) {
                Logger.info("Subscriber recorded successfully.")
                new Mailer(mailerClient)
                  .sendEmail(validData.email, "[सफल] गुलमोहर सदस्यता पंजीकरण", "", "kchbhi")
                Future.successful(Redirect(routes.HomeController.index()))
              }
              else {
                Logger.error("Subscriber not recorded successfully.")
                description = "Subscriber not recorded successfully."
                Future.successful(Redirect(routes.HomeController.errorPage()))
              }
            }
            catch {
              case ex: Exception => {
                description = "Subscriber not recorded successfully.User Already Exists."
                Future.successful(Redirect(routes.HomeController.errorPage()))
              }
            }
          })
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
            Future(BadRequest(views.html.errorPage("Something went wrong")))
          },
          validData => {
            println(validData.name + " : " + validData.email+" : "+validData.designation+" : "+validData.about)
            try {
              if (JDBCConnection
                .execute(
                  "insert into authors values('" + validData.name + "','" + validData.designation +
                  "','" + validData.about + "','" + validData.email + "','" + validData.password +
                  "')")) {
                Logger.info("Author recorded successfully.")
                new Mailer(mailerClient)
                  .sendEmail(validData.email, "[सफल] गुलमोहर पंजीकरण", validData.password,"regSuc")
                Future.successful(Redirect(routes.HomeController.authors()))
              }
              else {
                Logger.error("Author not recorded successfully.")
                description = "Author not recorded successfully."
                Future.successful(Redirect(routes.HomeController.errorPage()))
              }
            }
            catch {
              case ex: Exception => {
                description = "Author not recorded successfully.User Already Exists."
                Future.successful(Redirect(routes.HomeController.errorPage()))
              }

            }
          }
        )
    }
  }

  def sendMail(to:String,subject:String,content:String):Action[AnyContent] = {
    Action{
      new Mailer(mailerClient).sendEmail(to,subject,content,"")
      Ok("")
    }
  }

  def showForgotPassword:Action[AnyContent]={
    Action{
      Ok(views.html.forgotPassword(forgotPasswordForm))
    }
  }
  def forgotPassword:Action[AnyContent] = {
    Action.async {
      implicit request =>
        forgotPasswordForm.bindFromRequest.fold(
          formWithErrors => {
            Future(BadRequest(views.html.errorPage("Something went wrong.")))
          },
          validData => {
            val password = s"${Random.alphanumeric take 5 mkString("")}"
            if (JDBCConnection.execute("update authors set password = '"+password+"' where email ='"+validData.email+"'")) {
              new Mailer(mailerClient)
                .sendEmail(validData.email, "[सफलता] पासवर्ड रीसेट सफलता", password,"resPas")
              Future.successful(Redirect(routes.HomeController.authors()))
            }
            else {
              description = "Password Can't changed successfully.Please Try again later."
              Future.successful(Redirect(routes.HomeController.errorPage()))
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
            Future(BadRequest(views.html.errorPage("Something went wrong.")))
          },
          validData => {
            val valdateemail = JDBCConnection.executeQuery("select * from authors where email='"+validData.email+"' and password='"+validData.password+"'")
            if(!(valdateemail.next())){
              Logger.info("Invalid Author.")
              description="Invalid Author. Author not found. Please register you email-id."
              Future.successful(Redirect(routes.HomeController.errorPage()))
            }
            else {
              Logger.info("Valid Author.")
              try {
              if (JDBCConnection
                .execute(
                  "insert into articles values('" + validData.email + "','" + validData.title +
                  "','" + validData.body + "','" + validData.postTime + "','" + validData.likes +
                  "')")) {
                Logger.info("Article recorded successfully.")
                new Mailer(mailerClient)
                  .sendEmail(validData.email, "[सफल] लेख अपलोड सफलता", validData.title,"artUp")
                val f: Future[List[Unit]] =Future{
                  val resultSet = JDBCConnection.executeQuery("select * from subscriber")
                  val listSubscriber = getSubscribers(resultSet, Nil)
                  val mailer = new Mailer(mailerClient)
                    listSubscriber.map{
                      subscriber => mailer.sendEmail(subscriber.email, "[गुलमोहर] नया लेख अपलोड किया गया", validData.title,"subsNot")
                    }
                }
                import scala.util.{Success, Failure}
                f onComplete {
                  case Success(posts) =>Logger.info("Mail Sent to subscriber for "+validData.title)
                  case Failure(t) => Logger.info("Mail Sent Failure for "+validData.title)
                }
                Future.successful(Redirect(routes.HomeController.authors()))
              }
              else {
                Logger.error("Article not recorded successfully.")
                description = "Article not recorded successfully."
                Future.successful(Redirect(routes.HomeController.errorPage()))
              }
              }
              catch {
                case ex: Exception => {
                  description = "Article not recorded successfully.Article with this Title Already Exists."
                  Future.successful(Redirect(routes.HomeController.errorPage()))
                }

              }
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
        resultSet.getString(4),"")
      getAuthors(resultSet, authorList :+ author)
    } else {
      authorList
    }
  }

  def getArticles(resultSet: ResultSet, articleList: List[Article]): List[Article] = {
    if (resultSet.next()) {
      val article = Article(resultSet.getString(1),"",
        resultSet.getString(2),
        resultSet.getString(3),
        resultSet.getString(4),
        resultSet.getString(5)
      )
      getArticles(resultSet, articleList :+ article)
    } else {
      articleList
    }
  }

  def getSubscribers(resultSet: ResultSet, subscriberList: List[Subscribe]): List[Subscribe] = {
    if (resultSet.next()) {
      val subscriber = Subscribe(resultSet.getString(1)
      )
      getSubscribers(resultSet, subscriberList :+ subscriber)
    } else {
      subscriberList
    }
  }

}
