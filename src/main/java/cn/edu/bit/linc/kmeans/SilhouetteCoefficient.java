package cn.edu.bit.linc.kmeans;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wyq on 2016/4/13.
 */
public class SilhouetteCoefficient {

    // 第一位是标签, 标签 0， 1， 2， 3
    private double a_i(double[] x_i, double[][] x){
        double dist = 0;
        for(int i=0; i<x.length; i++){
            double sum = 0;
            for(int j=1; j<x[i].length; j++){
                sum += (x[i][j] - x_i[j])*(x[i][j] - x_i[j]);
            }
//            dist += Math.sqrt(sum);
            dist += sum;
        }
        dist = dist / x.length;
        return dist;
    }

    private double s_i(double[] x_i, double[][] s, int k){
        //计算 a_i
        List<double[]> list = new ArrayList<double[]>();
        for(int i=0; i<s.length; i++){
            if(s[i][0] == x_i[0]){
                list.add(s[i]);
            }
        }
        double[][] x = new double[list.size()][x_i.length];
        x = list.toArray(x);
        double a_i = a_i(x_i, x);

        //计算 b_i
        double b_i = 0;
        int flag = 0;
        for(int j=0; j<k; j++) {
            if(j == x_i[0])
                continue;
            list = new ArrayList<double[]>();
            for (int i = 0; i < s.length; i++) {
                if (s[i][0] == j) {
                    list.add(s[i]);
                }
            }
            x = new double[list.size()][x_i.length];
            x = list.toArray(x);
            double tmp = a_i(x_i, x);
            if(flag == 0){
                b_i = tmp;
                flag = 1;
            }
            else if(b_i > tmp){
                b_i = tmp;
            }
        }

        //System.out.println(a_i + " " + b_i);
        double s_i = (b_i - a_i) / Math.max(a_i, b_i);
        return  s_i;
    }

    public double calc(List<double[]> list, int k){
        if(list.size() == 0)
            return 0;
        double[][] data = new double[list.size()][list.get(0).length];
        data = list.toArray(data);

        double sum = 0;
        for(int i=0; i<data.length; i++){
            sum += s_i(data[i], data, k);
        }
        return sum / data.length;
    }


    public double calcFromFile(String filename, int k) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
        List<double[]> list = new ArrayList<double[]>();
        String line = null;
        while ((line = br.readLine()) != null){
            line = line.replaceAll("\\[|\\]|\\(|\\)", "");
            //System.out.println(line);
            String[] strs = line.split(",");
            double[] vec = new double[strs.length];
            for (int i = 0; i <strs.length; i++) {
                vec[i] = Double.parseDouble(strs[i]);
            }
            list.add(vec);
        }
        br.close();

        System.out.println("data len: " + list.size());
        return calc(list, k);
    }

    public static void main(String[] args) throws IOException {
        SilhouetteCoefficient sc = new SilhouetteCoefficient();
        System.out.println(sc.calcFromFile("user_result.txt", 3));
    }

}
