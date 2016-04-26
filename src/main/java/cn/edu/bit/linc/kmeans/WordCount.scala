package cn.edu.bit.linc.kmeans

import java.io.PrintWriter

import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by wyq on 2016/4/20.
 */
object WordCount {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop");
    val conf = new SparkConf().setAppName("wordcount").setMaster("local[*]");
    val sc = new SparkContext(conf)

    val textFiles = sc.textFile("C:\\Users\\wyq\\Desktop\\linuxone\\wiki_paths\\plaintext_articles")
    val wordCounts = textFiles.flatMap(line => line.split("\\s")).map(word => (word, 1)).reduceByKey((a, b) => a+b)

    val out = new PrintWriter("wordcounts.txt")
    wordCounts.collect().sortWith((a, b) => a._2 > b._2).foreach(out.println)
    out.close()
  }
}
