package cn.edu.bit.linc.cluster;

import java.io.*;

/**
 * Created by wyq on 2016/4/28.
 */
public class Preprocess {

    public static void main(String[] args){
        try {
            getWords();
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

}
