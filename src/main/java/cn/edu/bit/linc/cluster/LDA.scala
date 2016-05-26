package cn.edu.bit.linc.cluster

import java.io.PrintWriter

import scala.collection.mutable
import org.apache.spark.mllib.clustering.{DistributedLDAModel, LocalLDAModel, LDA}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by wyq on 2016/4/25.
 */
object LDA {

  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "E:\\文档\\spark\\hadoop");
    val conf = new SparkConf().setAppName("LDA").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Load documents from text files, 1 document per file
    // 一个文件一个文档，每个文档是一行
    val tfs = sc.wholeTextFiles("C:\\Users\\wyq\\Desktop\\linuxone\\word_list_new")
//    val tfs = sc.wholeTextFiles("C:\\Users\\wyq\\Desktop\\pytest\\test")
    val corpus: RDD[String] = tfs.map(_._2)
    val articles = tfs.map(a => a._1.substring(a._1.lastIndexOf("/")+1, a._1.lastIndexOf("."))).zipWithIndex().map(a => (a._2, a._1)).collect().toMap

    // Split each document into a sequence of terms (words)
    // 切分每个文档成一个个单词，RDD中的每一个元素是一篇文档
    val tokenized: RDD[Seq[String]] =
      corpus.map(_.toLowerCase.split("\\s")).map(_.filter(_.length > 3).filter(_.forall(java.lang.Character.isLetter)))

    // Choose the vocabulary.
    // 选择词汇
    //   termCounts: Sorted list of (term, termCount) pairs
    val termCounts: Array[(String, Long)] =
      tokenized.flatMap(_.map(_ -> 1L)).reduceByKey(_ + _).collect().sortBy(-_._2)
    //   vocabArray: Chosen vocab (removing common terms)
    // 去掉前20个单词
    val out = new PrintWriter("out_lda.txt")
    out.println("termCounts :" + termCounts.size)
    val numStopwords = 20
    val vocabArray: Array[String] =
      termCounts.takeRight(termCounts.size - numStopwords).map(_._1)

    //   vocab: Map term -> term index
    // 将单词和其在数组中的位置绑定  term -> term index
    val vocab: Map[String, Int] = vocabArray.zipWithIndex.toMap
    out.println("vocab :" + vocab.size)
    out.println()

    // Convert documents into term count vectors
    // 把每篇文档转化成向量
    val documents: RDD[(Long, Vector)] =
      tokenized.zipWithIndex.map { case (tokens, id) =>
        val counts = new mutable.HashMap[Int, Double]()
        tokens.foreach { term =>
          if (vocab.contains(term)) {
            val idx = vocab(term)
            counts(idx) = counts.getOrElse(idx, 0.0) + 1.0
          }
        }
        (id, Vectors.sparse(vocab.size, counts.toSeq))
      }

    // Set LDA parameters
    val numTopics = 15
    val lda = new LDA().setK(numTopics).setMaxIterations(200)

    val ldaModel = lda.run(documents)
//    val avgLogLikelihood = ldaModel.logLikelihood / documents.count()

    // Print topics, showing top-weighted 10 terms for each topic.
    val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = 2)
    topicIndices.foreach { case (terms, termWeights) =>
      out.println("TOPIC:")
      terms.zip(termWeights).foreach { case (term, weight) =>
        out.println(s"${vocabArray(term.toInt)}\t$weight")
      }
      out.println()
    }
    out.close()

    val result = ldaModel.topicDistributions.map(a => (articles.get(a._1), a._2)).map( a => {
      val arr = a._2.toArray
      val index = arr.indexOf(arr.max)
      val term = topicIndices(index)._1(0)
      (a._1, "topic " + index + ": " + vocabArray(term.toInt))
    })
    result.saveAsTextFile("output4")

  }

  def sampleLDA(): Unit ={
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

    // Save and load model.
//    ldaModel.save(sc, "myLDAModel")
//    val sameModel = DistributedLDAModel.load(sc, "myLDAModel")
  }

}
