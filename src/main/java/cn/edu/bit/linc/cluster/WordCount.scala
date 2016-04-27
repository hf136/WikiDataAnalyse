package cn.edu.bit.linc.cluster

import java.io.{File, PrintWriter}

import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by wyq on 2016/4/20.
 */
object WordCount {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop");
    val conf = new SparkConf().setAppName("wordcount").setMaster("local[*]");
    val sc = new SparkContext(conf)

    val dir = new File("C:\\Users\\wyq\\Desktop\\linuxone\\wordlist");
    if(dir.isDirectory()) {
      val files = dir.listFiles();
      for(file <- files){
        val textFiles = sc.textFile("C:\\Users\\wyq\\Desktop\\linuxone\\wordlist\\" + file.getName)

        val wordCounts = textFiles.map(line => line.split("\\s")(0)).map(word => (word, 1)).reduceByKey((a, b) => a+b)

        val out = new PrintWriter("C:\\Users\\wyq\\Desktop\\linuxone\\word_count\\" + file.getName)
        wordCounts.collect().sortWith((a, b) => a._2 > b._2).foreach(out.println)
        out.close()
      }
    }
  }
}
