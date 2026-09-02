package com.yashi.searchengine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Executes ranked search queries against an InvertedIndex using TF-IDF scoring.
 *
 * TF  (term frequency)      = how often a term appears in a specific document
 * IDF (inverse doc frequency) = log(totalDocs / docsContainingTerm) — rarer terms score higher
 * Score for a document = sum of TF-IDF across all query terms present in that document
 */
public class SearchEngine {

    private final InvertedIndex index;

    public SearchEngine(InvertedIndex index) {
        this.index = index;
    }

    /**
     * Searches the index for the given query and returns the top N results,
     * ranked by TF-IDF score descending.
     */
    public List<SearchResult> search(String query, int topN) {
        List<String> queryTerms = index.getTokenizer().tokenize(query);
        if (queryTerms.isEmpty()) {
            return List.of();
        }

        Map<Integer, Double> scores = new HashMap<>();
        int totalDocs = index.getDocumentCount();

        for (String term : queryTerms) {
            List<Posting> postings = index.getPostings(term);
            if (postings.isEmpty()) continue;

            double idf = Math.log((double) totalDocs / postings.size());

            for (Posting posting : postings) {
                Document doc = index.getDocument(posting.getDocumentId());
                double tf = (double) posting.getTermFrequency() / doc.getTotalTerms();
                double tfIdf = tf * idf;

                scores.merge(posting.getDocumentId(), tfIdf, Double::sum);
            }
        }

        // Use a min-heap of size topN to efficiently keep only the best results.
        PriorityQueue<SearchResult> heap = new PriorityQueue<>(
                Comparator.comparingDouble(SearchResult::getScore)
        );

        for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
            Document doc = index.getDocument(entry.getKey());
            SearchResult result = new SearchResult(doc, entry.getValue());

            heap.offer(result);
            if (heap.size() > topN) {
                heap.poll(); // remove the lowest-scoring result
            }
        }

        List<SearchResult> results = new ArrayList<>(heap);
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore())); // descending
        return results;
    }
}
