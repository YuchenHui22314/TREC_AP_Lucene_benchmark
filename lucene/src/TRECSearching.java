import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.nio.file.Files;

public class TRECSearching {
    public static void main(String[] args) throws Exception {
        
        // We have to run 3*2*3 = 18 different experiments, each with a different indexing, stop words, and retrieval model.

        // Query path
        String topicsFilePath = "/mnt/d/yuchenxi/UDEM/diro/IFT6255/devoir1/AP_topics.1-150.txt";
        // Query processing 
        TrecTopicsReader reader = new TrecTopicsReader();
        BufferedReader br = new BufferedReader(new FileReader(topicsFilePath));
        QualityQuery[] topics = reader.readQueries(br); 
        // System.out.println(topics[29].getValue("title").substring(6));

        // convert QualityQuery[] to HashMap<String, String>
        HashMap<String, String> topicsMap = new HashMap<String, String>();
        for (QualityQuery topic : topics) {
            topicsMap.put(topic.getQueryID(), topic.getValue("title").substring(6)); // remove the beginning "Topic:"
        }

        // read stop words from file
        Path stopWordsPath = Paths.get("/mnt/d/yuchenxi/UDEM/diro/IFT6255/devoir1/stop_words.txt");
        List<String> stopWords = Files.readAllLines(stopWordsPath);

        // Define possible indexing & model options
        boolean[] stopwordsOptions = {false, true};
        String[] stemmingOptions = {"no stemming", "porter", "krovetz"};
        String[] modelOptions = {"VSM", "BM25", "LM"};

        // Define Model to be used
        Similarity VSM = new ClassicSimilarity(); 
        Similarity BM25 = new BM25Similarity();
        Similarity LM = new LMDirichletSimilarity();

        // Initialize dictionaries (Maps in Java)
        Map<Boolean, List<String>> stopwordsOptionsDict = new HashMap<>();
        stopwordsOptionsDict.put(false, null);
        stopwordsOptionsDict.put(true, stopWords);

        Map<String, Similarity> modelOptionsDict = new HashMap<>();
        modelOptionsDict.put("VSM", VSM);
        modelOptionsDict.put("BM25", BM25);
        modelOptionsDict.put("LM", LM);

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

        Map<String, String> modelDisplayDict = new HashMap<>();
        modelDisplayDict.put("VSM", "VSM");
        modelDisplayDict.put("BM25", "BM25");
        modelDisplayDict.put("LM", "LM");

        String indexPath;
        String outputPath;
        Analyzer analyzer;
        String runID;
        // for all 18 experiments
        for (String stemming : stemmingOptions) {
            for (boolean stopwords : stopwordsOptions) {
                // construct index path
                indexPath = "/mnt/d/yuchenxi/UDEM/diro/IFT6255/devoir1/AP_index_" +
                        stemmingDisplayDict.get(stemming) + "_" +
                        stopwordsDisplayDict.get(stopwords);
                // construct output path
                String outputPathRoot = "/mnt/d/yuchenxi/UDEM/diro/IFT6255/devoir1/AP_ranking/" +
                        stemmingDisplayDict.get(stemming) + "_" +
                        stopwordsDisplayDict.get(stopwords);

                analyzer = new CustomAnalyzer( 
                    stopwordsOptionsDict.get(stopwords),
                    stemmingOptionsDict.get(stemming)
                );

                for (String model : modelOptions) {
                    outputPath = outputPathRoot + "_" + modelDisplayDict.get(model) + ".txt";
                    runID = "[" + stemmingDisplayDict.get(stemming) + "]" + "[" + stopwordsDisplayDict.get(stopwords) + "]" + "[" + modelDisplayDict.get(model) + "]";

                    Similarity rankingModel = modelOptionsDict.get(model);

                    // print experiment information
                    System.out.println("running experiment with");
                    System.out.println("stemming:" + stemming);
                    System.out.println("stopwords:" + stopwords);
                    System.out.println("model:" + model);

                    // initialize the searcher object
                    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(indexPath))));

                    // set retrieval model
                    searcher.setSimilarity(rankingModel); 
                    
                    // Query parser to parse query text
                    QueryParser parser = new QueryParser("text", analyzer);
                    
                    // Prepare for output
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));

                    // for each query in 150 queries
                    for (Map.Entry<String, String> entry : topicsMap.entrySet()) {

                        // get query id and query text
                        String queryId = entry.getKey();
                        String queryString = entry.getValue();
                        Query query = parser.parse(queryString);
                        
                        // Perform search
                        ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;
                        
                        // Write results
                        StoredFields fields = searcher.storedFields(); 
                        for (int i = 0; i < hits.length; i++) {
                            Document hitDoc = fields.document(hits[i].doc);
                            String docNo = hitDoc.get("docno"); // Assuming 'docno' is the document identifier field
                            writer.write(queryId + " Q0 " + docNo + " " + (i + 1) + " " + hits[i].score + " " +runID +"\n");
                        }
                    }
        
                    // Clean up
                    writer.close();

                }
            }
        }

        
        
        // clean up
        br.close();
    }
}
