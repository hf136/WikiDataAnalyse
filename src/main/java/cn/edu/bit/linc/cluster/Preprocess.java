package cn.edu.bit.linc.cluster;

import java.io.*;
import java.util.HashMap;

/**
 * Created by wyq on 2016/4/28.
 */
public class Preprocess {

    public static void main(String[] args){
        try {
//            getWords();
            links2Int("data/graphx/links.tsv", "data/graphx/links.txt", "data/graphx/names.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getWords() throws IOException {
        File dir = new File("C:\\Users\\wyq\\Desktop\\linuxone\\wordlist");
        if(dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\wyq\\Desktop\\linuxone\\word_list_new\\" + file.getName()));
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = "";
                while ((line = br.readLine()) != null) {
                    String[] strs = line.split("\\t");
                    bw.write(strs[0]);
                    bw.newLine();
                }
                bw.close();
                br.close();
            }
        }
    }

    public static void links2Int(String links, String edges, String names) throws IOException {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        File file = new File(links);

        int cnt = 1;
        BufferedWriter bw = new BufferedWriter(new FileWriter(edges));
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) {
            if(!line.startsWith("#")) {
                String[] strs = line.split("\\t");
                if(strs.length == 2) {
                    if (!map.containsKey(strs[0])){
                        map.put(strs[0], cnt++);
                    }
                    if(!map.containsKey(strs[1])){
                        map.put(strs[1], cnt++);
                    }
                    bw.write(map.get(strs[0]) + "\t" + map.get(strs[1]));
                    bw.newLine();
                }
            }
        }
        bw.close();
        br.close();

        BufferedWriter nbw = new BufferedWriter(new FileWriter(names));
        for(HashMap.Entry<String, Integer> entry: map.entrySet()){
            nbw.write(entry.getValue() + "," + entry.getKey());
            nbw.newLine();
        }
        nbw.close();

    }

}
