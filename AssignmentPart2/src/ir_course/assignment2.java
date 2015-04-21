/*
 * Assignment 2 Information Retrieval
 * Comparison scenario 3: Video Tagging
 * 
 * Authors:
 * Antti Partanen ID: 295967
 * Vik Kamath ID: 440819
 * 
 */
package ir_course;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import java.io.*;


class assignment2 
{
	public Directory directory = new RAMDirectory();
	public IndexWriter idxWriter = null; 
	public StandardAnalyzer analyzer = null;
	IndexSearcher isearcher = null; 
	public assignment2() {}

	// Standard analyzer 
    private static Analyzer analyzerStandardSW = new StandardAnalyzer(Version.LUCENE_42);
    private static Directory directoryStandarSW = new RAMDirectory();
    
    // Standard analyzer without stop words
    private static Analyzer analyzerStandardNSW = new StandardAnalyzer(Version.LUCENE_42,CharArraySet.EMPTY_SET);
    private static Directory directoryStandarNSW = new RAMDirectory();
    
    // Standard analyzer using PorterStemmer
    private static Analyzer analyzerStandardStem = new StandardAnalyzer(Version.LUCENE_42);
    private static Directory directoryStandarStem = new RAMDirectory();
    
    /* Method index
     * 
     * List<DocumentInCollection> docs - documents to be indexed
     * Analyzer analyzer - analyzer being used
     * Directory directory - directory where the idexing is saved
     * boolean doStem - true is you want to use PorterStemmer
     * 
     * Index documents using the analyzer desired.
     * 
     */
    public void index(List<DocumentInCollection> docs, Analyzer analyzer, Directory directory, boolean doStem)
    {
        try 
        {
            // Create index
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, analyzer);
            IndexWriter iwriter = new IndexWriter(directory, config);
            // Add documents to the index
            for (DocumentInCollection doc : docs) 
            {
                Document document = new Document();
                String abstractText = doc.getAbstractText();
                String titleText = doc.getTitle();
                // Use PorterStemmer for the analysis
                // First stem the text then add it to the document
                if(doStem) 
                {
                    PorterStemmer stem = new PorterStemmer();
                    // Stemmer for Abstract text
                    // Replace all characters that are not letters or white space
                    // then make the string to lower case and split with white spaces
                    String[] words = abstractText.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
                    String abstractTextTemp = ""; 
                    // Stem each word since stemming a sentence returns the same sentence
                    for(int i = 0; i<words.length; i++) 
                    {
                        if(words.length -1 == i) 
                        {
                            stem.setCurrent(words[i]);
                            stem.stem();
                            String r = stem.getCurrent();
                            abstractTextTemp += r;
                        }//end of if
                        else 
                        { // adding a space between words
                            stem.setCurrent(words[i]);
                            stem.stem();
                            String r = stem.getCurrent();
                            abstractTextTemp += r + " ";
                        }//end of else
                    }//end of for
                    
                    // Stemmer for Title text
                    // Same logic as above
                    words = titleText.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
                    String titleTextTemp = ""; 
                    for(int i = 0; i<words.length; i++) 
                    {
                        stem.setCurrent(words[i]);
                        stem.stem();
                        String r = stem.getCurrent();
                        if(words.length -1 == i) 
                        { 
                            titleTextTemp += r;
                        }//end of if
                        else 
                        {
                            titleTextTemp += r + " ";
                        }//end of else
                    }//end of for
                    titleText = titleTextTemp;
                    abstractText = abstractTextTemp;
                }//end of if
                document.add(new Field("title", titleText, TextField.TYPE_STORED)); 
                document.add(new Field("abstract", abstractText, TextField.TYPE_STORED));
                document.add(new Field("realTitle", doc.getTitle(), TextField.TYPE_STORED));
                // Retrieve the relevance of the document and convert it to string format
                String relevant;
                if (doc.isRelevant()) 
                {
                    relevant = "1";
                }//end of if
                else 
                {
                    relevant = "0";
                }//end of else
                document.add(new Field("relevant", relevant, TextField.TYPE_STORED));
                iwriter.addDocument(document);
            }//end of for loop
            iwriter.close();
        }//end of try block 
        catch (IOException ex) 
        {
            Logger.getLogger(assignment2.class.getName()).log(Level.SEVERE, null, ex);
        }//end of catch block
    }//end of method 'index'
	
    /*
     * Method search
     * 
     * List<String> query - query to be made
     * Directory directory - directory to search
     * boolean doBM25 - true to use BM25
     * boolean doStem - true to user PorterStemmer
     * 
     * Do the search. Search in title and abstract
     * 
     * return - HashMap<Float, Document> results - results of the query
     * 
     */
    public HashMap<Float, Document> search(List<String> query, Directory directory, boolean doBM25, boolean doStem) 
    {
        HashMap<Float, Document> results = new HashMap<>(); // user score as key, title as value
        try 
        {
            DirectoryReader ireader = DirectoryReader.open(directory);
            IndexSearcher isearcher = new IndexSearcher(ireader);
            // Set similarity to BM25 if needed
            if(doBM25) 
            {
                isearcher.setSimilarity(new BM25Similarity());
            }//end of if
            // Create the boolean query
            BooleanQuery bq = new BooleanQuery();
            for (String s : query) 
            {
                // User PorterStemmer for each word in the query to match the index
                if(doStem) 
                {
                    PorterStemmer stem = new PorterStemmer();
                    String word = s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
                    stem.setCurrent(word);
                    stem.stem();
                    s = stem.getCurrent();
                }//end of if
                TermQuery tqtitle = new TermQuery(new Term("title", s)); // query for the title
                TermQuery tqabs = new TermQuery(new Term("abstract", s)); // query for the abstract
                // Add queries to boolean query as an or
                bq.add(tqtitle, Occur.SHOULD);
                bq.add(tqabs, Occur.SHOULD);
            }//end of for
            // Compute query
            ScoreDoc[] hits = isearcher.search(bq, 1000).scoreDocs;
            // Copy results to the HashMap 
            for (ScoreDoc sd : hits) 
            {
                Document hitDoc = isearcher.doc(sd.doc);
                results.put(sd.score, hitDoc);
            }//end of for
            ireader.close();
        }//end of try 
        catch (IOException ex) 
        {
            Logger.getLogger(assignment2.class.getName()).log(Level.SEVERE, null, ex);
        }//end of catch block
        return results;
    }//end of method 'search
    
    /*
     * Method printResultsByScore
     * 
     * HashMap<Float, Document> map - map with the results 
     * 
     * Order the map by score and reverse it to print it from highest to lowest
     * 
     */
    public void printResultsByScore(HashMap<Float, Document> map) 
    {
        if (map.size() > 0) 
        {
        	int count = 0;
            Map<Float, Document> sorted = new TreeMap<>(map); // Order the map
            Iterator iterator = sorted.keySet().iterator();
            List<Float> keys = new LinkedList<>(); // Keys in ascendant order
            while (iterator.hasNext()) 
            {
                keys.add((float) iterator.next());
            }//end of while
            // Read the keys from the bottom
            for (int i = keys.size()-1; i >= 0; i--) 
            {
                Document doc = sorted.get(keys.get(i));
                System.out.println(keys.get(i) + "," + doc.get("realTitle") + "," + doc.get("relevant")); // For csv format
                //System.out.println("\t\t"+keys.get(i) + " " + doc.get("realTitle") + " " + doc.get("relevant"));
                count = count + 1;
            }//end of for
            System.out.println("The Number of Results is:"+count);
        }//end of if
        else 
        {
            System.out.println(" no results");
        }//end of else
    }//end of method 'printResultsByScore'
    
	/*
     * Method printResults
     * 
     * List<String> query - query to be made
     * Directory directory - directory to search
     * boolean doStem - true to use PorterStemmer
     * 
     * Just print results
     */
    public void printResults(List<String> query, Directory directory, boolean doStem) 
    {
        HashMap<Float, Document> resultsVSM;
        HashMap<Float, Document> resultsBM25F;
        System.out.println("Search (" + query + ");");
        resultsVSM = search(query, directory, false, doStem);
        resultsBM25F = search(query, directory, true, doStem);
        System.out.println("\tResults ranked with VSM hits:" + resultsVSM.size());
        printResultsByScore(resultsVSM);
        System.out.println("\tResults ranked with BM25 hits:" + resultsBM25F.size());
        printResultsByScore(resultsBM25F);
    }//end of method 'printResults'
    
    
	/*
     * Method main
     * 
     * args[0] must be the document to be parsed
     * 
     */
	public static void main(String[] args) 
	{
        
        if (args.length > 0) 
        {
            assignment2 engine = new assignment2();

            // Parse the documents
            DocumentCollectionParser parser = new DocumentCollectionParser();
            parser.parse(args[0]);
            List<DocumentInCollection> docs = parser.getDocuments();

            // Index with Standard Analyzer StopWords
            engine.index(docs, analyzerStandardSW, directoryStandarSW, false);
            
            // Index with Standard Analyzer No Stop Words
            engine.index(docs, analyzerStandardNSW, directoryStandarNSW, false);
            
            // Index with English Stemmer
            engine.index(docs, analyzerStandardStem, directoryStandarStem, true);
            
            List<String> query1; // Content based video annotation
            List<String> query2; // Automatic or semiautomatic video tagging
            List<String> query3; // feature based Multimedia annotation
            
            // Content based video annotation
            query1 = new LinkedList<>();
            query1.add("content");
            query1.add("based");
            query1.add("video");
            query1.add("annotation");
            
            // Automatic or semiautomatic video tagging
            query2 = new LinkedList<>();
            query2.add("automatic");
            query2.add("semiautomatic");
            query2.add("video");
            query2.add("tagging");

            // feature based Multimedia annotation
            query3 = new LinkedList<>();
            query3.add("feature");
            query3.add("based");
            query3.add("multimedia");
            query3.add("annotation");
            
            System.out.println("Using Standard Analyzer with Stop Words");
            engine.printResults(query1, directoryStandarSW, false);
            engine.printResults(query2, directoryStandarSW, false);
            engine.printResults(query3, directoryStandarSW, false);
            
            System.out.println();
            System.out.println("Using Standard Analyzer without Stop Words");
            engine.printResults(query1, directoryStandarNSW, false);
            engine.printResults(query2, directoryStandarNSW, false);
            engine.printResults(query3, directoryStandarNSW, false);
            
            System.out.println();
            System.out.println("Using Standard Analyzer with PorterStemmer");
            engine.printResults(query1, directoryStandarStem, true);
            engine.printResults(query2, directoryStandarStem, true);
            engine.printResults(query3, directoryStandarStem, true);
       
        }//end of if block
        else 
        {
        	System.out.println("ERROR: the path of a RSS Feed file has to be passed as a command line argument.");
        }//end of 'else'
    }//end of function 'main
}//end of class assignment2