import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TRECIndexing {

    public static void main(String[] args) throws Exception {


        // read stop words from file
        Path stopWordsPath = Paths.get("/mnt/d/yuchenxi/UDEM/diro/IFT6255/devoir1/stop_words.txt");
        List<String> stopWords = Files.readAllLines(stopWordsPath);

        // Define possible indexing & model options
        boolean[] stopwordsOptions = {false, true};
        String[] stemmingOptions = {"no stemming", "porter", "krovetz"};

        // Initialize dictionaries (Maps in Java)
        Map<Boolean, List<String>> stopwordsOptionsDict = new HashMap<>();
        stopwordsOptionsDict.put(false, null);
        stopwordsOptionsDict.put(true, stopWords);

        Map<String, String> stemmingOptionsDict = new HashMap<>();
        stemmingOptionsDict.put("no stemming", "no stemming");
        stemmingOptionsDict.put("porter", "porter");
        stemmingOptionsDict.put("krovetz", "krovetz");

        // display dictionaries
        Map<Boolean, String> stopwordsDisplayDict = new HashMap<>();
        stopwordsDisplayDict.put(false, "No_stopwords");
        stopwordsDisplayDict.put(true, "Stopwords");

        Map<String, String> stemmingDisplayDict = new HashMap<>();
        stemmingDisplayDict.put("no stemming", "No_stemming");
        stemmingDisplayDict.put("porter", "Porter_stemming");
        stemmingDisplayDict.put("krovetz", "Krovetz_stemming");


        // Specify index path and create directory
        // Create analyzer with or without stop words
        Path indexPath;
        Analyzer analyzer;


        // for all 6 indexing choices 
        for (String stemming : stemmingOptions) {
            for (boolean stopwords : stopwordsOptions) {

                // print indexing information
                System.out.println("Indexing with " + stemming + " stemming and " + (stopwords ? "stopwords" : "no stopwords"));

                // construct index path
                String indexPath_String = "/mnt/d/yuchenxi/UDEM/diro/IFT6255/devoir1/AP_index_" +
                        stemmingDisplayDict.get(stemming) + "_" +
                        stopwordsDisplayDict.get(stopwords);
                indexPath = Paths.get(indexPath_String);

                // get analyzer
                analyzer = new CustomAnalyzer( 
                    stopwordsOptionsDict.get(stopwords),
                    stemmingOptionsDict.get(stemming)
                );

                // Cresate a config Objrect via analyzer for index writer.
                Directory directory = FSDirectory.open(indexPath);
                IndexWriterConfig config = new IndexWriterConfig(analyzer);

                // Create ## index writer ## by reading the documents and adding them to the index.
                // the preprocessing strategy is specified by config.
                try (IndexWriter iwriter = new IndexWriter(directory, config)) {
                    Path docDirPath = Paths.get("/mnt/d/yuchenxi/UDEM/diro/IFT6255/devoir1/AP");
                    Files.walk(docDirPath)
                        .filter(Files::isRegularFile)
                        .forEach(filePath -> {
                            try {
                                String content = new String(Files.readAllBytes(filePath));
                                Pattern pattern = Pattern.compile("<DOC>(.*?)</DOC>", Pattern.DOTALL);
                                Matcher matcher = pattern.matcher(content);
                                while (matcher.find()) {
                                    String docContent = matcher.group(1);
                                    Document doc = new Document();
                                    // Extract DOCNO
                                    String docNo = extractTagValue(docContent, "DOCNO");
                                    // Extract TEXT
                                    String text = extractTagValue(docContent, "TEXT");
                                    doc.add(new StringField("docno", docNo, Field.Store.YES));
                                    doc.add(new TextField("text", text, Field.Store.YES));
                                    // !!!!!!!!!!! add document to index ！！！！！！！！！！！！
                                    iwriter.addDocument(doc);
                                    // !!!!!!!!!!! add document to index ！！！！！！！！！！！！
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                } // iwriter is auto-closed here
                directory.close();
        

            }
        }

    }

    private static String extractTagValue(String content, String tagName) {
        Pattern tagPattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = tagPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
