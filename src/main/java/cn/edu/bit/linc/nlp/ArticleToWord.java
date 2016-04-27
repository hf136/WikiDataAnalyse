package cn.edu.bit.linc.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Created by wyq on 2016/4/21.
 */
public class ArticleToWord {

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        File dir = new File("C:\\Users\\wyq\\Desktop\\linuxone\\wiki_paths\\plaintext_articles");
        if(dir.isDirectory()){
            File[] files = dir.listFiles();
            for (File file : files){
                System.out.println("开始处理文章: " + file.getName());
                File outFile = new File("C:\\Users\\wyq\\Desktop\\linuxone\\word_list\\" + file.getName());
                if(outFile.exists()){
                    System.out.println("文件 " + file.getName() + " 已经存在，忽略该文件继续");
                    continue;
                }

                BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = "";
                while(line != null){
                    StringBuffer text = new StringBuffer();
                    for (int i = 0; i < 20; i++) {
                        line = br.readLine();
                        if(line != null&& !line.equals("")) text.append(line);
                        else break;
                    }

                    Annotation document = new Annotation(text.toString());
                    pipeline.annotate(document);

                    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

                    for(CoreMap sentence: sentences) {
                        // traversing the words in the current sentence
                        // a CoreLabel is a CoreMap with additional token-specific methods
                        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                            String word = token.get(CoreAnnotations.TextAnnotation.class);
                            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                            String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                            String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                            //System.out.println(lemma + "\t" + pos + "\t" + ne + "\t" + word);
                            if(pos.startsWith("NN")) {
                                bw.write(lemma + "\t" + pos + "\t" + ne + "\t" + word);
                                bw.newLine();
                            }
                        }
                    }
                }
                br.close();
                bw.close();

            }
        }

    }

}
