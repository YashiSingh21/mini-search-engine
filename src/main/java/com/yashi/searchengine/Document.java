package com.yashi.searchengine;

/**
 * Represents a single document in the corpus.
 */
public class Document {
    private final int id;
    private final String fileName;
    private final String content;
    private final int totalTerms;

    public Document(int id, String fileName, String content, int totalTerms) {
        this.id = id;
        this.fileName = fileName;
        this.content = content;
        this.totalTerms = totalTerms;
    }

    public int getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }

    public int getTotalTerms() {
        return totalTerms;
    }

    /**
     * Returns a short snippet of the document content for display in search results.
     */
    public String getSnippet(int maxLength) {
        String trimmed = content.trim().replaceAll("\\s+", " ");
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength) + "...";
    }

    @Override
    public String toString() {
        return "Document{id=" + id + ", fileName='" + fileName + "'}";
    }
}
