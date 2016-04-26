package cn.edu.bit.linc.kmeans

import java.io.PrintWriter

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors

object MyKMeans {
	def main(args: Array[String]) {
		System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop");
		val conf = new SparkConf().setAppName("k-means").setMaster("local[*]")
		val sc = new SparkContext(conf)

		// Load and parse the data
		val input_dir = args(0)
		val output_dir = args(1)

		val data = sc.textFile(input_dir)
    val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble))).cache()

		// Cluster the data into two classes using KMeans
		val numClusters = 3
		val numIterations = 20
		val clusters = KMeans.train(parsedData, numClusters, numIterations)

//    val res = clusters.predict(parsedData)
//    res.saveAsTextFile(output_dir);

		val res = parsedData.map(v => (clusters.predict(v), v)).collect()
//		res.saveAsTextFile(output_dir);
		val res_out = new PrintWriter("result.txt")
		res.foreach(res_out.println)
		res_out.close()

    val out = new PrintWriter("centers.txt")
    val centers = clusters.clusterCenters
    centers.foreach(out.println)
    out.close()

		// Evaluate clustering by computing Within Set Sum of Squared Errors
		val WSSSE = clusters.computeCost(parsedData)
		println("Within Set Sum of Squared Errors = " + WSSSE)
	}
}
