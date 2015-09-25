import org.apache.spark._
import com.google.gson.Gson
import java.io.File
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._
/**
 * Created by victorgarcia on 24/09/15.
 */
object CollectTweets {
  private var gson = new Gson()
  private var numTweetsCollected = 0L
  private var numTweetsToCollect = 1000
  def main(args: Array[String]) {
    // Checkpoint directory
    val checkpointDir = utils.getCheckpointDirectory()
    // Configure Twitter credentials

    utils.configureTwitterCredentials()

    val ssc = new StreamingContext(new SparkContext(), Seconds(30))


    val tweets = TwitterUtils.createStream(ssc, None).map(gson.toJson(_))

    tweets.foreachRDD((rdd, time) => {
      val count = rdd.count()
      if (count > 0) {
        val outputRDD = rdd.repartition(2)
        outputRDD.saveAsTextFile("./tweets/tweets_" + time.milliseconds.toString)
        numTweetsCollected += count
        if (numTweetsCollected > numTweetsToCollect) {
          System.exit(0)
        }
      }
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
