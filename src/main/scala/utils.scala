import scala.io.Source
import scala.collection.mutable.HashMap
import java.io.File
import org.apache.log4j.Logger
import org.apache.log4j.Level
import sys.process.stringSeqToProcess
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
/**
 * Created by victorgarcia on 24/09/15.
 */
object utils {
  val tf = new HashingTF(2)
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  Logger.getLogger("org.apache.spark.storage.BlockManager").setLevel(Level.ERROR)

  val apiKey = "PGjvhoLeXSm1WReGHzWvyFrc8"
  val apiSecret = "ZREG4ovxA1pXZmse3omyNwKwBUSDvMLQI3Jswh5waU7Le7TU6q"
  val accessToken = "1673419831-Rd6eBipys8GWYKHrvhH971WsMbdmVPQibDGY6mE"
  val accessTokenSecret = "G8Mhw0tErA8sA8OgJgOcHOXLZtdyfqIIQx6ZwWYM9XvGJ"

  /** Configures the Oauth Credentials for accessing Twitter */
  def configureTwitterCredentials() {
    val configs = new HashMap[String, String] ++= Seq(
      "apiKey" -> apiKey, "apiSecret" -> apiSecret, "accessToken" -> accessToken, "accessTokenSecret" -> accessTokenSecret)
    println("Configuring Twitter OAuth")
    configs.foreach{ case(key, value) =>
        if (value.trim.isEmpty) {
          throw new Exception("Error setting authentication - value for " + key + " not set")
        }
        val fullKey = "twitter4j.oauth." + key.replace("api", "consumer")
        System.setProperty(fullKey, value.trim)
        println("\tProperty " + fullKey + " set as [" + value.trim + "]")
    }
    println()
  }

  /** Returns the Spark URL */
  def getSparkUrl(): String = {
    val file = new File("/root/spark-ec2/cluster-url")
    if (file.exists) {
      val url = Source.fromFile(file.toString).getLines.toSeq.head
      url
    } else if (new File("../local").exists) {
      "local[4]"
    } else {
      throw new Exception("Could not find " + file)
    }
  }

  /** Returns the HDFS URL */
  def getCheckpointDirectory(): String = {
    try {
      val name : String = Seq("bash", "-c", "curl -s http://169.254.169.254/latest/meta-data/hostname") !! ;
      println("Hostname = " + name)
      "hdfs://" + name.trim + ":9000/checkpoint/"
    } catch {
      case e: Exception => {
        "./checkpoint/"
      }
    }
  }

  def featurize(s: String): Vector = {
    tf.transform(s.sliding(2).toSeq)
  }
}
