package cn.edu.bit.linc.kmeans

import java.io.{File, PrintWriter}

import cn.edu.bit.linc.cluster.SilhouetteCoefficient
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors


object DocKMeans {
	def main(args: Array[String]) {
		System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop");
		val conf = new SparkConf().setAppName("k-means").setMaster("local[*]")
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
		for (i <- 2 to 30){
			val numClusters = i
			val numIterations = 200
			var min = 0.0;
			var min_sc = 0.0;

			for(j <- 1 to 20){
				val clusters = KMeans.train(parsedData, numClusters, numIterations)
				val WSSSE = clusters.computeCost(parsedData)
				if(min == 0 || WSSSE < min) {
					min = WSSSE;

					val res = parsedData.map(v => (clusters.predict(v), v)).collect()
					val res_out = new PrintWriter(output_dir + "result.txt")
					res.foreach(res_out.println)
					res_out.close()

					val scRes = new SilhouetteCoefficient();
					min_sc = scRes.calcFromFile(output_dir + "result.txt", numClusters)
				}
			}

			kwOut.println(numClusters + "\t" + min)
			kscOut.println(numClusters + "\t" + min_sc)
		}
		kwOut.close()
		kscOut.close()

//    val out = new PrintWriter("centers.txt")
//    val centers = clusters.clusterCenters
//    centers.foreach(out.println)
//    out.close()

//		// Evaluate clustering by computing Within Set Sum of Squared Errors
//		val WSSSE = clusters.computeCost(parsedData)
//		println("Within Set Sum of Squared Errors = " + WSSSE)

	}
}
