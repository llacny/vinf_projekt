package com.company;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */

/**
 * This indexer was built based on two tutorials
 * Sources:
 * 1. the core functionality (console aplication, selecting folder, loading files)
 * - http://www.lucenetutorial.com/sample-apps/textfileindexer-java.html#
 * 2. indexing over a key - value pair file, searching over a field
 * - https://stackoverflow.com/questions/9857047/lucene-net-how-to-order-and-query-keyvaluepair-data-type
 */
public class TextFileIndexer {
    private static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);

    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<File>();
    String articleTitle = "(.*)\t(.*)";
    Pattern pattern = Pattern.compile(articleTitle);

    public static void main(String[] args) throws IOException {
        System.out.println("Enter the path where the index will be created: (e.g. /tmp/index or c:\\temp\\index)");

        String indexLocation = null;
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        String s = br.readLine();

        TextFileIndexer indexer = null;
        try {
            indexLocation = s;
            indexer = new TextFileIndexer(s);
        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        //===================================================
        //read input from user until he enters q for quit
        //===================================================
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out.println("Enter the full path to add into the index (q=quit): (e.g. /home/ron/mydir or c:\\Users\\ron\\mydir)");
                System.out.println("[Acceptable file types: .xml, .html, .html, .txt]");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }

                //try to add file into the index
                indexer.indexFileOrDirectory(s);
            } catch (Exception e) {
                System.out.println("Error indexing " + s + " : " + e.getMessage());
            }
        }

        //===================================================
        //after adding, we always have to call the
        //closeIndex, otherwise the index is not created
        //===================================================
        indexer.closeIndex();

        //=========================================================
        // Now search
        //=========================================================
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
        IndexSearcher searcher = new IndexSearcher(reader);
        //TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);

        Map<String, Analyzer> analyzerPerField = new HashMap<String,Analyzer>();
        analyzerPerField.put("value", new KeywordAnalyzer());
        PerFieldAnalyzerWrapper aw = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_40),analyzerPerField);


        s = "";
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out.println("Enter the search query (q=quit):");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }
                Sort sort = new Sort(new SortField("value", SortField.Type.FLOAT, true));
                Query q = new QueryParser(Version.LUCENE_40, "title", analyzer).parse(s);
                TopDocs docs = searcher.search(q, null, 10, sort);
                //ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // 4. display results
                System.out.println("Found " + docs.totalHits + " hits.");

                for( var hit : docs.scoreDocs) {

                    int docId = hit.doc;
                    Document d = searcher.doc(docId);
                    System.out.println("Title: " + d.get("title").toString() + " Value: " + d.get("value").toString());
                }

            } catch (Exception e) {
                System.out.println("Error searching " + s + " : " + e.getMessage());
            }
        }

    }

    /**
     * Constructor
     * @param indexDir the name of the folder in which the index should be created
     * @throws java.io.IOException when exception creating index.
     */
    TextFileIndexer(String indexDir) throws IOException {
        // the boolean true parameter means to create a new index everytime,
        // potentially overwriting any existing files there.
        FSDirectory dir = FSDirectory.open(new File(indexDir));


        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);

        writer = new IndexWriter(dir, config);
    }

    /**
     * Indexes a file or directory
     * @param fileName the name of a text file or a folder we wish to add to the index
     * @throws java.io.IOException when exception
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
        //===================================================
        //gets the list of files in a folder (if user has submitted
        //the name of a folder) or gets a single file name (is user
        //has submitted only the file name)
        //===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();

                //===================================================
                // add contents of file
                //===================================================

                Document customDoc = new Document();
                Field title = new Field("title", "", Field.Store.YES, Field.Index.ANALYZED);
                Field value = new Field("value", "", Field.Store.YES, Field.Index.NOT_ANALYZED);
                customDoc.add(title);
                customDoc.add(value);

                BufferedReader bufferreader = new BufferedReader( new FileReader(f));
                String line;
                while( (line = bufferreader.readLine()) != null) {
                    Matcher m = pattern.matcher(line);
                    m.find();
                    String titleS = m.group(1);
                    String valueS = m.group(2);

                    title.setStringValue(titleS);
                    value.setStringValue(valueS);
                    writer.addDocument(customDoc);
                }

                bufferreader.close();

            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
            }
        }

        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            //===================================================
            // Only index text files
            //===================================================
            if (filename.endsWith(".htm") || filename.endsWith(".html") ||
                    filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Close the index.
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }
}