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
                        System.err.println("error： " + file.getName());
                        System.err.println("line: " + line);
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
                if(o1.getValue().tf_idf < o2.getValue().tf_idf){
                    return 1;
                }
                else {
                    return -1;
                }
            }
        });

        BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\wyq\\Desktop\\linuxone\\wordrank.txt"));
        for(HashMap.Entry<String, Entry> w: list){
            bw.write(w.getKey() + "\t" + String.format("%.8f", w.getValue().tf_idf));
            bw.newLine();
        }
        bw.close();
    }

    public static void main(String[] args){
        TFIDF tfidf = new TFIDF();
        try {
            tfidf.count();
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

