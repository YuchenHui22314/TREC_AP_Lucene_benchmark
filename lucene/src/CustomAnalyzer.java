import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;

import java.util.List;

/**
 * CustomAnalyzer that filters WhitespaceTokenizer with LowerCaseFilter and optionally StopFilter.
 * It also allows specifying stemming filter: PorterStemFilter, KStemFilter, or no stemming.
 */
public class CustomAnalyzer extends Analyzer {
    private CharArraySet stopWords;
    private String stemmingOption;

    /**
     * Constructor for CustomAnalyzer.
     * @param stopWords List of stop words to be used. If null or empty, stop filtering is not applied.
     * @param stemmingOption Specifies the stemming filter to use: "porter" for PorterStemFilter,
     *                       "Krovetz" for KStemFilter, and "no stemming" for no stemming filter.
     */
    public CustomAnalyzer(List<String> stopWords, String stemmingOption) {
        this.stopWords = stopWords == null ? CharArraySet.EMPTY_SET : new CharArraySet(stopWords, true);
        this.stemmingOption = stemmingOption;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new WhitespaceTokenizer();
        TokenStream result = new LowerCaseFilter(source);
        
        // Apply StopFilter if stop words are provided
        if (!stopWords.isEmpty()) {
            result = new StopFilter(result, stopWords);
        }
        
        // Apply stemming based on the specified option
        switch (stemmingOption.toLowerCase()) {
            case "porter":
                result = new PorterStemFilter(result);
                break;
            case "krovetz":
                result = new KStemFilter(result);
                break;
            case "no stemming":
                // No stemming filter applied
                break;
            default:
                throw new IllegalArgumentException("Unsupported stemming option: " + stemmingOption);
        }
        
        return new TokenStreamComponents(source, result);
    }
}
