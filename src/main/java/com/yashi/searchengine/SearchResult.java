package com.yashi.searchengine;

/**
 * A single ranked search result: a document paired with its TF-IDF score.
 */
public class SearchResult {
    private final Document document;
    private final double score;

    public SearchResult(Document document, double score) {
        this.document = document;
        this.score = score;
    }

    public Document getDocument() {
        return document;
    }

    public double getScore() {
        return score;
    }
}
