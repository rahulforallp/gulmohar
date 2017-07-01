package models

/**
 * Created by rahul on 7/5/17.
 */
case class Authors (name:String,designation:String,about:String,email:String,password:String)
case class Article(email:String,password:String,title:String,body:String,postTime: String,likes: String)
case class ForgotPassword(email:String)