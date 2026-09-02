# Mini Search Engine

A search engine built from scratch in Java — implements the core information
retrieval pipeline used by real search systems: tokenization, an inverted
index, and TF-IDF ranking, exposed through a simple CLI.

## Why this project

Most CRUD/JDBC projects show you can use a database. This project shows what's
actually happening *underneath* one — building a custom index and ranking
algorithm rather than relying on a library or `SELECT ... WHERE LIKE`.

## Features

- **Tokenizer** — normalizes text (lowercasing, punctuation stripping) and
  removes stopwords, used consistently at both index-time and query-time.
- **Inverted Index** — maps each term to the list of documents it appears in,
  along with term frequency (a `Posting`).
- **Multithreaded indexing** — documents are read and indexed concurrently
  across a fixed thread pool (`ExecutorService`), with thread-safe merges into
  a shared `ConcurrentHashMap`.
- **TF-IDF ranking** — scores documents by term frequency × inverse document
  frequency, so rarer, more relevant terms are weighted higher.
- **Top-N retrieval via min-heap** — uses a `PriorityQueue` to efficiently
  keep only the highest-scoring results instead of sorting the entire result
  set.
- **CLI search interface** — interactive prompt for querying the index, with
  defensive exception handling so malformed input never crashes the session.

## Project structure

```
search-engine/
├── corpus/                     # Sample text documents to index
├── src/main/java/com/yashi/searchengine/
│   ├── Document.java            # Represents an indexed document
│   ├── Tokenizer.java           # Text normalization + stopword removal
│   ├── Posting.java             # (docId, term frequency) entry
│   ├── InvertedIndex.java       # Core index + multithreaded build
│   ├── SearchResult.java        # Document + relevance score
│   ├── SearchEngine.java        # TF-IDF scoring + ranked search
│   └── Main.java                # CLI entry point
└── README.md
```

## How it works

1. **Indexing**: each `.txt` file in `corpus/` is read, tokenized, and its
   terms are added to a shared inverted index (`term -> [(docId, freq), ...]`).
   Indexing is parallelized across a thread pool since each document is
   processed independently.
2. **Querying**: the query string goes through the *same* tokenizer used at
   index time, so terms match consistently.
3. **Ranking**: for each query term, TF-IDF is computed per document and
   summed across all query terms. Results are kept in a min-heap of size N so
   only the top-N scores are retained, then sorted descending for display.

## Running it

Requires JDK 17+.

```bash
# Compile
javac -d out src/main/java/com/yashi/searchengine/*.java

# Run (defaults to indexing the corpus/ directory)
java -cp out com.yashi.searchengine.Main corpus
```

Then type a query at the `search>` prompt:

```
search> search engine
Top 1 results for "search engine":
  1. doc3.txt  (score: 0.1694)
     A search engine is a software system designed to carry out web searches...

search> exit
```

## Possible extensions

- Persist the index to disk (or MySQL via JDBC) so it doesn't rebuild on
  every run.
- Expose `/search?q=` as a REST endpoint via Spring Boot instead of a CLI.
- Add phrase search or fuzzy matching (edit distance) for typo tolerance.
- Swap the fixed stopword list for a configurable one, or add stemming.
