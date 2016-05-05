package cn.edu.bit.linc

import java.io.PrintWriter

import org.apache.spark.graphx.GraphLoader
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by wyq on 2016/5/5.
 */
class PageRank {

  def rank(): Unit ={
    System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop")
    val conf = new SparkConf().setAppName("Page Rank").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Load the edges as a graph
    val graph = GraphLoader.edgeListFile(sc, "data/graphx/links.txt")
    // Run PageRank
    val ranks = graph.pageRank(0.0001).vertices
    // Join the ranks with the usernames
    val users = sc.textFile("data/graphx/names.txt").map { line =>
      val fields = line.split(",")
      (fields(0).toLong, fields(1))
    }
    val ranksByUsername = users.join(ranks).map {
      case (id, (username, rank)) => (username, rank)
    }
    // Print the result
    //println(ranksByUsername.collect().mkString("\n"))

    val rankRes = ranksByUsername.collect().sortWith((a, b) => a._2 > b._2)
    val out = new PrintWriter("data/graphx/rank.txt")
    rankRes.foreach(out.println)
    out.close()
  }

}

object PageRank {
  def main(args: Array[String]) {
    val pageRank = new PageRank
    pageRank.rank()
  }
}
