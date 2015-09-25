import org.apache.spark.SparkConf
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}
/**
 * Created by victorgarcia on 25/09/15.
 */
object TweetByCluster {
  def main(args: Array[String]) {

    utils.configureTwitterCredentials()

    println("Initializing Streaming Spark Context...")
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val ssc = new StreamingContext(conf, Seconds(5))

    println("Initializing Twitter stream...")
    val tweets = TwitterUtils.createStream(ssc, None)
    val statuses = tweets.map(_.getText)

    println("Initalizaing the the KMeans model...")
    val model = new KMeansModel(ssc.sparkContext.objectFile[Vector]("./classif").collect())

    val filteredTweets = statuses
      .filter(t => model.predict(utils.featurize(t)) == 2)
    filteredTweets.print()

    // Start the streaming computation
    println("Initialization complete.")
    ssc.start()
    ssc.awaitTermination()
  }
}
