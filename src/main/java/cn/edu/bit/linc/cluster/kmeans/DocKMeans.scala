package cn.edu.bit.linc.cluster.kmeans

import java.io.{File, PrintWriter}

import cn.edu.bit.linc.cluster.SilhouetteCoefficient
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.{KMeansModel, KMeans}
import org.apache.spark.mllib.linalg.Vectors


object DocKMeans {
	def main(args: Array[String]) {

		getBestK(2, 40, 5, args)
//		getResult(12, 100)
	}

	def getBestK(startK:Int, endK:Int, cntK:Int, args:Array[String]): Unit ={
		System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop")
		val conf = new SparkConf().setAppName("k-means getBestK").setMaster("local[*]")
		val sc = new SparkContext(conf)

		val input_dir = args(0)
		val output_dir = args(1)
		val file = new File(output_dir)
		if(!file.exists()){
			file.mkdir()
		}

		// Load and parse the data
		val data = sc.textFile(input_dir)
		val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble))).cache()

		val kwOut = new PrintWriter(output_dir + "k-w.csv")
		val kscOut = new PrintWriter(output_dir + "k-sc.csv")
		for (i <- startK to endK){
			val numClusters = i
			val numIterations = 200
			var min = 0.0
			var min_sc = 0.0

			for(j <- 1 to cntK){
				val clusters = KMeans.train(parsedData, numClusters, numIterations)
				val WSSSE = clusters.computeCost(parsedData)
				//println("Within Set Sum of Squared Errors = " + WSSSE)
				if(min == 0 || WSSSE < min) {
					min = WSSSE

					val res = parsedData.map(v => (clusters.predict(v), v)).collect()
					val res_out = new PrintWriter(output_dir + "temp_result.txt")
					res.foreach(res_out.println)
					res_out.close()

					val scRes = new SilhouetteCoefficient();
					min_sc = scRes.calcFromFile(output_dir + "temp_result.txt", numClusters)
				}
			}

			kwOut.println(numClusters + "\t" + min)
			kscOut.println(numClusters + "\t" + min_sc)
		}
		kwOut.close()
		kscOut.close()
	}

	def getResult(numClusters:Int, numIterations:Int): Unit ={
		System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop")
		val conf = new SparkConf().setAppName("k-means predict").setMaster("local[*]")
		val sc = new SparkContext(conf)

		val input_dir = "vector_list.txt"
		val output_dir = "output/"
		val file = new File(output_dir)
		if(!file.exists()){
			file.mkdir()
		}

		// Load and parse the data
		val data = sc.textFile(input_dir)
		val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble))).cache()

		var min = 0.0
		var minClusters : KMeansModel = null
		for(j <- 1 to 5){
			val clusters = KMeans.train(parsedData, numClusters, numIterations)
			val WSSSE = clusters.computeCost(parsedData)
			//println("Within Set Sum of Squared Errors = " + WSSSE)
			if(min == 0 || WSSSE < min) {
				min = WSSSE
				minClusters = clusters
			}
		}

//		val res = parsedData.map(v => (minClusters.predict(v), v)).collect()
		val data2 = sc.textFile("vector_list_with_name.txt")
		val dataWithName = data2.map(line => (line.split("\t")(0), line.split("\t")(1))).map(s => (Vectors.dense(s._1.split(' ').map(_.toDouble)), s._2.substring(0, s._2.lastIndexOf('.')))).cache()
		val res = dataWithName.map(v => (minClusters.predict(v._1), v._2)).collect()
		val resCount = dataWithName.map(v => (minClusters.predict(v._1), 1)).reduceByKey((a, b) => a+b).collect()
		resCount.foreach(s => println(s))

		val res_out = new PrintWriter(output_dir + "predict_result.txt")
		res.foreach(res_out.println)
		res_out.close()
	}

}
