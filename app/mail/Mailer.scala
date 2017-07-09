package mail

import play.api.libs.mailer._
import javax.inject.Inject
import java.io.File
import org.apache.commons.mail.EmailAttachment

/**
 * Created by rahul on 1/7/17.
 */
class Mailer @Inject() (mailerClient:MailerClient) {

  def sendEmail(to:String,subject:String,content:String,mailType:String) {
    val email = mailType match {
      case "regSuc" => Email(
      subject,
      "gulmohar.noreply@gmail.com",
      Seq(to),
      bodyHtml = Some(s"""<html><body><p><center><table border=”0″ cellpadding=”0″ cellspacing=”0″ width=”600″ bgcolor=”#ffffff” ><tr bgcolor=”#c63a56”><td><font size="6">गुलमोहर में आपका स्वागत है.</font></tr><tr bgcolor=”#c63a56”><td><font size="6">आपका पंजीकरण गुलमोहर पर सफल रहा है</font></tr><tr bgcolor=”#c63a56”><td><font size="6">आपका पासवर्ड है : </font><font size="3"><b> $content </b></font></tr><tr bgcolor=”#c63a56”><td><font size="6">धन्यवाद</font></tr></table><center></p></body></html>""")
    )
      case "resPas" =>Email(
          subject,
          "gulmohar.noreply@gmail.com",
          Seq(to),
        bodyHtml = Some(s"""<html><body><p><center><table border=”0″ cellpadding=”0″ cellspacing=”0″ width=”600″ bgcolor=”#ffffff” ><tr bgcolor=”#c63a56”><td><font size="6"> गुलमोहर में आपका स्वागत है.</font></tr><tr bgcolor=”#c63a56”><td><font size="6">आपका नया पासवर्ड है : </font><font size="3"><b> $content </b></font></tr><tr bgcolor=”#c63a56”><td><font size="6">धन्यवाद</font></tr></table><center></p></body></html>""")
          )
      case "artUp" =>Email(
          subject,
          "gulmohar.noreply@gmail.com",
          Seq(to),
        bodyHtml = Some(s"""<html><body><p><center><table border=”0″ cellpadding=”0″ cellspacing=”0″ width=”600″ bgcolor=”#ffffff” ><tr bgcolor=”#c63a56”><td><font size="6">आपके अनुच्छेद को सफलतापूर्वक अपलोड किया गया है.</font></tr><tr bgcolor=”#c63a56”><td><font size="6">आपके अनुच्छेद का शीर्षक है : </font><font size="4"><b> $content </b></font></tr><tr bgcolor=”#c63a56”><td><font size="6">धन्यवाद</font></tr></table><center></p></body></html>""")
          )
      case "subsNot" =>Email(
          subject,
          "gulmohar.noreply@gmail.com",
          Seq(to),
        bodyHtml = Some(s"""<html><body><p><center><table border=”0″ cellpadding=”0″ cellspacing=”0″ width=”600″ bgcolor=”#ffffff” ><tr bgcolor=”#c63a56”><td><font size="6">नये अनुच्छेद को अपलोड किया गया है.</font></tr><tr bgcolor=”#c63a56”><td><font size="6">अनुच्छेद का शीर्षक है : </font><font size="4"><b> $content </b></font></tr><tr bgcolor=”#c63a56”><td><font size="6">धन्यवाद</font></tr></table><center></p></body></html>""")
          )
      case _ =>Email(
          subject,
          "gulmohar.noreply@gmail.com",
          Seq(to),
        bodyHtml = Some(s"""<html><body><p><center><table border=”0″ cellpadding=”0″ cellspacing=”0″ width=”600″ bgcolor=”#ffffff” ><tr bgcolor=”#c63a56”><td><font size="6"> गुलमोहर में आपका स्वागत है.</font></tr></table><center></p></body></html>""")
          )
    }
    mailerClient.send(email)
  }

}
