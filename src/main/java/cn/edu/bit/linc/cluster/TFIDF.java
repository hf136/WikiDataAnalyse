package cn.edu.bit.linc.cluster;

import java.io.*;
import java.util.*;

/**
 * Created by wyq on 2016/4/26.
 */
public class TFIDF {
    HashMap<String, Entry> TFC = new HashMap<String, Entry>();
    long totalWord = 0;
    long totalDoc = 0;

    public void count() throws IOException {
        File dir = new File("C:\\Users\\wyq\\Desktop\\linuxone\\word_count");
        if(dir.isDirectory()) {
            File[] files = dir.listFiles();
            totalDoc = files.length;
            for (File file : files) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = "";
                while((line = br.readLine()) != null){
                    String[] strs = line.replaceAll("\\(|\\)", "").split(",");
                    String word = strs[0];
                    try {
                        long value = Long.parseLong(strs[1]);
                        totalWord += value;

                        if (TFC.containsKey(word)) {
                            TFC.get(word).df += 1;
                            TFC.get(word).tf += value;
                        } else {
                            TFC.put(word, new Entry(value, 1));
                        }
                    }
                    catch (Exception e){
                        //System.err.println("error： " + file.getName());
                        //System.err.println("line: " + line);
                    }
                }
                br.close();
            }
        }

        System.out.println("total word: " + totalWord);
        System.out.println("total document: " + totalDoc);

        for(String word : TFC.keySet()){
            Entry entry = TFC.get(word);
            double tf = entry.tf * 1.0 / totalWord;
            double idf = Math.log(totalDoc/entry.df);
            entry.tf_idf = tf * idf * 100;
        }

        List<HashMap.Entry<String, Entry>> list = new ArrayList<Map.Entry<String, Entry>>(TFC.entrySet());
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(list, new Comparator<HashMap.Entry<String, Entry>>() {
            public int compare(HashMap.Entry<String, Entry> o1, HashMap.Entry<String, Entry> o2) {
                if (o1.getValue().tf_idf < o2.getValue().tf_idf) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\wyq\\Desktop\\linuxone\\wordrank.txt"));
        for(HashMap.Entry<String, Entry> w: list){
            if(w.getKey().length() > 1) {
                bw.write(w.getKey() + "\t" + String.format("%.8f", w.getValue().tf_idf));
                bw.newLine();
            }
        }
        bw.close();
    }

    public void buildVectors(int size) throws IOException {
        BufferedReader brs = new BufferedReader(new FileReader("C:\\Users\\wyq\\Desktop\\linuxone\\wordrank.txt"));
        String w = "";
        String[] words = new String[size];
        for (int i = 0; i < size && w != null; i++) {
            w = brs.readLine();
            words[i] = w.split("\\s")[0];
            //System.out.println(words[i]);
        }
        brs.close();

        BufferedWriter bw = new BufferedWriter(new FileWriter("vector_list.txt"));
        BufferedWriter vectorNameWriter = new BufferedWriter(new FileWriter("vector_list_with_name.txt"));
        File dir = new File("C:\\Users\\wyq\\Desktop\\linuxone\\word_count");
        if(dir.isDirectory()) {
            File[] files = dir.listFiles();
            int cnt = 0;
            for (File file : files) {
                HashMap<String, Long> map = new HashMap<String, Long>();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = "";
                while((line = br.readLine()) != null){
                    String[] strs = line.replaceAll("\\(|\\)", "").split(",");
                    String word = strs[0];
                    try {
                        long value = Long.parseLong(strs[1]);
                        map.put(word, value);
                    }
                    catch (Exception e){

                    }
                }
                br.close();

                int flag = 0;
                for (int i = 0; i < size; i++) {
                    if(map.containsKey(words[i])){
                        flag = 1;
                        if(i == 0) {
                            bw.write(map.get(words[i]).toString());
                            vectorNameWriter.write(map.get(words[i]).toString());
                        }
                        else{
                            bw.write(" " + map.get(words[i]).toString());
                            vectorNameWriter.write(" " + map.get(words[i]).toString());
                        }
                    }
                    else{
                        if(i == 0){
                            bw.write("0");
                            vectorNameWriter.write("0");
                        }
                        else{
                            bw.write(" 0");
                            vectorNameWriter.write(" 0");
                        }
                    }
                }
                if(flag == 0){
                    cnt++;
                    System.out.println("zero: " + file.getName());
                }
                vectorNameWriter.write("\t" + file.getName());
                vectorNameWriter.newLine();
                bw.newLine();
            }
            System.out.println("total zero vector: " + cnt);
        }
        bw.close();
        vectorNameWriter.close();

    }

    public static void main(String[] args){
        TFIDF tfidf = new TFIDF();
        try {
            tfidf.count();
            tfidf.buildVectors(50);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class Entry{
    long tf = 0;
    long df = 0;
    double tf_idf = 0;

    public Entry(long tf, long df) {
        this.tf = tf;
        this.df = df;
    }
}

