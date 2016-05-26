package cn.edu.bit.linc.cluster

import java.io.{File, PrintWriter}

import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by wyq on 2016/4/20.
 */
object WordCount {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "E:\\文档\\spark\\hadoop");
    val conf = new SparkConf().setAppName("wordcount").setMaster("local[*]");
    val sc = new SparkContext(conf)

    val f = sc.textFile("output4")
    val res = f.map(line => {
      val strs = line.split(",")
      val a = strs(0)
      val b = strs(1)
      val t1 = a.substring(a.lastIndexOf("(")+1, a.lastIndexOf(")"))
      val t2 = b.substring(0, b.length - 1)
      (t1, t2)
    })
    val out = new PrintWriter("articles_class.csv")
    res.collect().foreach(e => out.println(e._1 + "," + e._2))
    out.close()

    val clusterCount = res.map(e => (e._2, 1)).reduceByKey(_+_).collect()
    val out2 = new PrintWriter("cluster_count.csv")
    clusterCount.foreach(e => out2.println(e._1 + "\t" + e._2))
    out2.close()
  }

  def wordCount(): Unit ={
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
