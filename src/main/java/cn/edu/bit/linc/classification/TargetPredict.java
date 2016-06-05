package cn.edu.bit.linc.classification;

import Jama.Matrix;
import scala.util.parsing.combinator.testing.Str;

import java.io.*;
import java.util.*;

/**
 * Created by wyq on 2016/5/27.
 */
public class TargetPredict {

    HashMap<String, Matrix> map = new HashMap<String, Matrix>();

    public void predict(int k, int iters) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("vector_list_with_name.txt")));
        String line = null;
        while ((line = br.readLine()) != null){
            String[] strs = line.split("\\t");
            String[] t = strs[0].split(" ");
            String name = strs[1].substring(0, strs[1].lastIndexOf("."));
            double[][] v = new double[1][50];
            for(int i=0; i<50; i++){
                v[0][i] = Double.parseDouble(t[i]);
            }
            Matrix m = new Matrix(v);
            map.put(name, m);
        }
        br.close();

        br = new BufferedReader(new FileReader(new File("data/target_prediction/paths_finished.tsv")));
        line = br.readLine();
        int cnt = 0;
        int flag = 0;
        int[] ranks = new int[iters];
        while ((line = br.readLine()) != null) {
            cnt++;
            if(cnt > iters)
                break;
            String[] paths = line.split("\\s")[3].split(";");
            if(paths.length <= k) continue;
            HashSet<String> pathsSet = new HashSet<String>();
            for(int j=0; j<k; j++){
                pathsSet.add(paths[j]);
            }
            String article = null;
            if(paths[k-1] == "<")
                continue;
            else
                article = paths[k-1];
            Matrix m = map.get(article);
            //System.out.println(m.getRowDimension() + " " + m.getColumnDimension());
            Article[] articles = new Article[4604];
            int i =0;
            for (HashMap.Entry<String, Matrix> entry : map.entrySet()) {
                if(!pathsSet.contains(entry.getKey())) {
                    Article a = new Article(entry.getKey(), similarity(m, entry.getValue()));
                    articles[i++] = a;
                }
            }
            for(;i<4604; i++){
                articles[i] = new Article("null", -2);
            }
            Arrays.sort(articles, new Comparator<Article>() {
                public int compare(Article o1, Article o2) {
                    //System.out.println(o1 + "   " + o2);
                    if (o1.value > o2.value)
                        return -1;
                    else if (o1.value == o2.value)
                        return 0;
                    return 1;
                }
            });
            for (int j=0; j<10; j++){
                //System.out.println(articles[j]);
                if(articles[j].name.compareTo(paths[paths.length-1]) == 0){
                    //System.out.println("======== rank pos:" + (j+1));
                    ranks[cnt-1] = j+1;
                    if(j < 2 && flag < 10){
                        for (String str : paths){
                            System.out.print(str + "->");
                        }
                        System.out.println();
                        for(int ii=0; ii<10; ii++){
                            System.out.println((ii+1) + " " + articles[ii]);
                        }
                        System.out.println();
                        flag++;
                    }
                }
            }
        }
        int sum = 0;
        for (int j = 0; j < ranks.length; j++) {
            if(ranks[j] != 0) {
                sum++;
                System.out.println(ranks[j]);
            }
        }
        System.out.println("=======sum====== " + sum);
        br.close();
    }

    public double similarity(Matrix m, Matrix n){
        if(n == null || m == null)
            return 0;
        // 分母可能为 0 ...
        double fm = (Math.sqrt(m.times(m.transpose()).get(0, 0)) * Math.sqrt(n.times(n.transpose()).get(0, 0)));
        if(fm == 0)
            return 0;
        return m.times(n.transpose()).get(0, 0) / fm;
    }

    public static void main(String[] args) throws IOException {
        TargetPredict tp = new TargetPredict();
        tp.predict(3, 4604);
    }

}

class Article{
    public String name;
    public double value = 0;

    public Article(String name, double value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Article{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}