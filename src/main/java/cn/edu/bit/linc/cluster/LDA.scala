package cn.edu.bit.linc.cluster

import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by wyq on 2016/4/25.
 */
object LDA {

  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop");
    val conf = new SparkConf().setAppName("k-means").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Load and parse the data
    val data = sc.textFile("C:\\Users\\wyq\\Desktop\\linuxone\\wiki_paths\\plaintext_articles\\1st_century.txt")
    val parsedData = data.map(s => Vectors.dense(s.trim.split(' ').map(_.toDouble)))
    // Index documents with unique IDs
    val corpus = parsedData.zipWithIndex.map(_.swap).cache()

    // Cluster the documents into three topics using LDA
    val ldaModel = new LDA().setK(3).run(corpus)

    // Output topics. Each is a distribution over words (matching word count vectors)
    println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
    val topics = ldaModel.topicsMatrix
    for (topic <- Range(0, 3)) {
      print("Topic " + topic + ":")
      for (word <- Range(0, ldaModel.vocabSize)) { print(" " + topics(word, topic)); }
      println()
    }

//    // Save and load model.
//    ldaModel.save(sc, "myLDAModel")
//    val sameModel = DistributedLDAModel.load(sc, "myLDAModel")
  }
}
