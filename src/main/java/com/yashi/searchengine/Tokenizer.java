package com.yashi.searchengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles text preprocessing: lowercasing, punctuation removal,
 * stopword filtering, and tokenization.
 *
 * The SAME tokenizer is used at index time and query time so that
 * terms match consistently.
 */
public class Tokenizer {

    // A small, common English stopword list. Kept intentionally short
    // so the index still has enough terms to demonstrate ranking well.
    private static final Set<String> STOPWORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "in", "on", "at", "to", "for", "of", "and", "or", "as", "by",
            "with", "that", "this", "it", "its", "from", "has", "have",
            "had", "not", "but", "can", "will", "than", "then", "into",
            "used", "use", "using", "typically"
    );

    /**
     * Tokenizes raw text into a list of normalized terms.
     * Steps: lowercase -> strip punctuation -> split on whitespace -> drop stopwords/empties.
     */
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] rawWords = normalized.split("\\s+");

        for (String word : rawWords) {
            if (word.isBlank()) continue;
            if (STOPWORDS.contains(word)) continue;
            tokens.add(word);
        }
        return tokens;
    }
}
