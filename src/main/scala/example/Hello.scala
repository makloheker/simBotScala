import java.net.{HttpURLConnection, URL, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.io.Source
import org.json4s._
import org.json4s.native.JsonMethods._

object Main extends App {

  def chatBotRespon(text: String, lc: String = "id"): String = {
    val url = new URL("https://api.simsimi.vn/v1/simtalk")
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    connection.setDoOutput(true)

    val data = s"text=${URLEncoder.encode(text, StandardCharsets.UTF_8)}&lc=$lc"

    val outputStream = connection.getOutputStream
    outputStream.write(data.getBytes(StandardCharsets.UTF_8))
    outputStream.flush()
    outputStream.close()

    if (connection.getResponseCode == HttpURLConnection.HTTP_OK) {
      val response = Source.fromInputStream(connection.getInputStream).mkString
      response
    } else {
      throw new RuntimeException(s"err response: ${connection.getResponseCode}")
    }
  }

  def getMessageFromResponse(response: String): Option[String] = {
    implicit val formats = DefaultFormats
    val parsedJson = parse(response)
    (parsedJson \ "message").extractOpt[String]
  }

  def runBot(): Unit = {
    var input = ""

    while ({input = scala.io.StdIn.readLine("you>: "); input != "exit"}) {
      try {
        val response = chatBotRespon(input)
        getMessageFromResponse(response) match {
          case Some(message) => println(s"bot>: $message")
          case None => println("err msg")
        }
      } catch {
        case e: Exception => println(s"error ->>> ${e.getMessage}")
      }
    }
  }

  runBot()
}
