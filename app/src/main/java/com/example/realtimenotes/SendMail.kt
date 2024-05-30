
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SendMail(to: String, subject: String, body: String) {
    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {

                val props = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        val username = "devarshmehta2005@gmail.com"
                        val password = "xvitxphvagejnlam"
                        return PasswordAuthentication(username, password)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress("devarshmehta2005@gmail.com"))
                    addRecipient(Message.RecipientType.TO, InternetAddress(to))
                    setSubject(subject)
                    setText(body)
                }
                Transport.send(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
