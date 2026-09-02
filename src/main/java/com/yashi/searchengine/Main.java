package com.yashi.searchengine;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * CLI entry point for the mini search engine.
 *
 * Usage:
 *   java Main <corpus-directory>
 *
 * Then type search queries at the prompt. Type 'exit' to quit.
 */
public class Main {

    public static void main(String[] args) {
        String corpusPath = args.length > 0 ? args[0] : "corpus";

        System.out.println("=== Mini Search Engine ===");
        System.out.println("Indexing documents from: " + corpusPath);

        InvertedIndex index = new InvertedIndex();

        long startTime = System.currentTimeMillis();
        try {
            index.buildFromDirectory(corpusPath, 4); // 4 threads for indexing
        } catch (IOException e) {
            System.err.println("Error reading corpus directory: " + e.getMessage());
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Indexing was interrupted: " + e.getMessage());
            return;
        }
        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("Indexed " + index.getDocumentCount() + " documents in " + elapsed + " ms.");
        System.out.println();

        SearchEngine searchEngine = new SearchEngine(index);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("search> ");
            if (!scanner.hasNextLine()) break;
            String query = scanner.nextLine().trim();

            if (query.isEmpty()) continue;
            if (query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                break;
            }

            try {
                List<SearchResult> results = searchEngine.search(query, 5);
                printResults(query, results);
            } catch (Exception e) {
                // Defensive: a malformed query should never crash the CLI.
                System.err.println("Something went wrong processing that query: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void printResults(String query, List<SearchResult> results) {
        if (results.isEmpty()) {
            System.out.println("No results found for \"" + query + "\".\n");
            return;
        }

        System.out.println("Top " + results.size() + " results for \"" + query + "\":");
        int rank = 1;
        for (SearchResult result : results) {
            Document doc = result.getDocument();
            System.out.printf("  %d. %s  (score: %.4f)%n", rank, doc.getFileName(), result.getScore());
            System.out.println("     " + doc.getSnippet(100));
            rank++;
        }
        System.out.println();
    }
}
