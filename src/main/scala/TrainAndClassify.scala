import com.google.gson.{GsonBuilder, JsonParser}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
/**
 * Created by victorgarcia on 24/09/15.
 */
object TrainAndClassify{
  val jsonParser = new JsonParser()
  val gson = new GsonBuilder().setPrettyPrinting().create()

  def main(args: Array[String]) {

    utils.configureTwitterCredentials()

    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val ssc = new StreamingContext(sc, Seconds(10))


    val tweetsToPredict = TwitterUtils.createStream(ssc, None)

    val tweetTable = sqlContext.read.json("./tweets/tweets_1443173580000").cache()
    tweetTable.registerTempTable("tweetTable")

    println("--- Training the model and persist it")
    val texts = sqlContext.sql("SELECT text from tweetTable").map(_.toString)
    // Cache the vectors RDD since it will be used for all the KMeans iterations.
    val vectors = texts.map(utils.featurize).cache()
    texts.count() // Calls an action on the RDD to populate the vectors cache.
    val model = KMeans.train(vectors, 10, 100)
    sc.makeRDD(model.clusterCenters, 10).saveAsObjectFile("./classif")

    tweetsToPredict.foreachRDD { t =>
      for (i <- 0 until 10) {
        println(s"\nCLUSTER $i:")
        t.map(tw => tw.getText).foreach { tweet =>
          if (model.predict(utils.featurize(tweet.toString)) == i) {
            println(tweet)
          }
        }
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
