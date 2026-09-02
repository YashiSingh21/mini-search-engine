package com.yashi.searchengine;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds and stores the inverted index: term -> list of postings.
 * Also stores document metadata needed for TF-IDF scoring.
 *
 * Index construction is parallelized across documents using a thread pool,
 * since reading/tokenizing files is independent per-document work.
 */
public class InvertedIndex {

    private final Map<String, List<Posting>> index = new ConcurrentHashMap<>();
    private final Map<Integer, Document> documents = new ConcurrentHashMap<>();
    private final Tokenizer tokenizer = new Tokenizer();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    /**
     * Reads all .txt files in the given directory and builds the index,
     * processing documents concurrently across a fixed thread pool.
     */
    public void buildFromDirectory(String directoryPath, int threadCount) throws IOException, InterruptedException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(directoryPath), "*.txt")) {
            for (Path p : stream) {
                files.add(p);
            }
        }

        if (files.isEmpty()) {
            throw new IOException("No .txt files found in directory: " + directoryPath);
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            for (Path file : files) {
                executor.submit(() -> {
                    try {
                        indexFile(file);
                    } catch (IOException e) {
                        // Don't let one bad file kill the whole index build.
                        System.err.println("Skipping file due to read error: " + file + " (" + e.getMessage() + ")");
                    }
                });
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    /**
     * Reads, tokenizes, and indexes a single file. Thread-safe: uses
     * ConcurrentHashMap and synchronizes only on the per-term postings list.
     */
    private void indexFile(Path file) throws IOException {
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException e) {
            throw new IOException("Failed to read file: " + file, e);
        }

        int docId = idGenerator.getAndIncrement();
        List<String> tokens = tokenizer.tokenize(content);

        Document doc = new Document(docId, file.getFileName().toString(), content, tokens.size());
        documents.put(docId, doc);

        // Count term frequencies within this document first, then merge into the global index.
        Map<String, Integer> localCounts = new java.util.HashMap<>();
        for (String term : tokens) {
            localCounts.merge(term, 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : localCounts.entrySet()) {
            String term = entry.getKey();
            int freq = entry.getValue();

            List<Posting> postings = index.computeIfAbsent(term, k -> new ArrayList<>());
            synchronized (postings) {
                postings.add(new Posting(docId, freq));
            }
        }
    }

    public List<Posting> getPostings(String term) {
        return index.getOrDefault(term, List.of());
    }

    public Document getDocument(int docId) {
        return documents.get(docId);
    }

    public int getDocumentCount() {
        return documents.size();
    }

    public Map<Integer, Document> getAllDocuments() {
        return documents;
    }

    public Tokenizer getTokenizer() {
        return tokenizer;
    }
}
