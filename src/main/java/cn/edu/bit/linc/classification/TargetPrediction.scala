package cn.edu.bit.linc.classification

import Jama.Matrix
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by wyq on 2016/5/25.
 */
class TargetPrediction {

  def feature(u1: Matrix, u2: Matrix, t:Matrix, deg1:Int, deg2:Int): Unit ={
    val tmp = u2.minus(t)
    val f1 = tmp.times(tmp.transpose()).get(0, 0)
    val tmp2 = u1.minus(u1)
    val f2 = tmp2.times(tmp2.transpose())
    val f3 = (deg1 - 26.135165) / 293
    val t4 = (deg2 - 26.135165) / 293
    val f4 = f3 * t4
  }

  def predict(): Unit ={
    System.setProperty("hadoop.home.dir", "C:\\Users\\wyq\\Desktop\\spark\\hadoop");
    val conf = new SparkConf().setAppName("wordcount").setMaster("local[*]");
    val sc = new SparkContext(conf)
  }

}
