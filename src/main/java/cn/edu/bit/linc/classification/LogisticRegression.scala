package cn.edu.bit.linc.classification

import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by wyq on 2016/5/5.
 */
object LogisticRegression {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop")
    val conf = new SparkConf().setAppName("Page Rank").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Load training data in LIBSVM format.
    val data = MLUtils.loadLibSVMFile(sc, "vector_list")

    // Split data into training (60%) and test (40%).
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    // Run training algorithm to build the model
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(10)
      .run(training)

    // Compute raw scores on the test set.
    val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Get evaluation metrics.
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val precision = metrics.precision
    println("Precision = " + precision)

    // Save and load model
    model.save(sc, "myModelPath")
    val sameModel = LogisticRegressionModel.load(sc, "myModelPath")
  }
}
