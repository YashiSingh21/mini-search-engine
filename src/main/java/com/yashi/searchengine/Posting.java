package com.yashi.searchengine;

/**
 * A single entry in a term's postings list: which document the term
 * appeared in, and how many times.
 */
public class Posting {
    private final int documentId;
    private int termFrequency;

    public Posting(int documentId, int termFrequency) {
        this.documentId = documentId;
        this.termFrequency = termFrequency;
    }

    public int getDocumentId() {
        return documentId;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public void incrementFrequency() {
        this.termFrequency++;
    }

    @Override
    public String toString() {
        return "(docId=" + documentId + ", tf=" + termFrequency + ")";
    }
}
