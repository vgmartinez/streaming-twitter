import org.apache.spark._
import com.google.gson.{JsonElement, Gson}
import java.io.File
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._
/**
 * Created by victorgarcia on 24/09/15.
 */
object TwitterWordCount {
  private var gson = new Gson()
  private var numTweetsCollected = 0L
  private var numTweetsToCollect = 1000
  def main(args: Array[String]) {
    // Configure Twitter credentials

    utils.configureTwitterCredentials()

    val ssc = new StreamingContext(new SparkContext("local[*]", "TwitterWordCount", new SparkConf()), Seconds(30))

    val tweets = TwitterUtils.createStream(ssc, None).map(gson.toJson(_))

    tweets.foreachRDD((rdd, time) => {
      if (rdd.count > 0) {
        val lang = rdd.filter(a=>utils.extract_field(a, "lang")=="es")
        val count = lang.count()
        val tweet_text = lang.map(a=>utils.extract_field(a,"text"))
        //tweet_text.foreach(println)
        val top_words = tweet_text.
          flatMap(line => line.split(" "))
          .map(word => (word,1)).
          reduceByKey(_+_).
          takeOrdered(10)(Ordering[Int].reverse.on(x=>x._2))
        top_words.foreach(println)
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
