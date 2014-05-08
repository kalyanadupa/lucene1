/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Kalyan
 */
public class searchEngine {
    public HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
    Map<String , List<String>> searchIndex = new HashMap<String, List<String>>();
    public void parseQuery() {
        try {
            BufferedReader in = null;
            String f = "queryFile";
            
            in = new BufferedReader(new FileReader(f));
            int i = 400;
            String s = null;
            try {
                while ((s = in.readLine()) != null) {
                    StringBuilder sb = new StringBuilder();
                    //System.out.println("**"+s);
                    if(s.equals("<top>")){
                        i++;
                        s = in.readLine();
                        s = in.readLine();
                        while (!(s.equals("</top>"))){
                            s = in.readLine();
                            sb.append(s);
                        }
                        String[] tokenizedTerms = sb.toString().replaceAll("[\\W&&[^\\s]]", "").split("\\W+");
                        StringBuilder builder = new StringBuilder();
                        for(String str : tokenizedTerms){
                            builder.append(str);
                            builder.append(" ");
                        }
                        hashMap.put(i, builder.toString());
                        System.out.println(i +" : "+builder.toString());
                        builder.delete(0, builder.length());
                    }
                    //sb.append(s);
                }
            } catch (IOException ex) {
                Logger.getLogger(searchEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(searchEngine.class.getName()).log(Level.SEVERE, null, ex);        
        }
    }

    
      public static void main(String[] args) throws Exception {
          int i;
        searchEngine se = new searchEngine();
          
        se.parseQuery();
        String index = "index";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, "contents", analyzer);
        String queryString = "";
        //Query query = parser.parse(queryString); 
//        TopDocs results = searcher.search(query, 69000);
//        ScoreDoc[] hits = results.scoreDocs;
        //int numTotalHits = results.totalHits;
        //System.out.println(numTotalHits + " total matching documents");
        for(i = 401;i <= 450;i++){
           queryString = se.hashMap.get(i);
           Query query = parser.parse(queryString);
           TopDocs results = searcher.search(query, 12352580);
           ScoreDoc[] hits = results.scoreDocs;
           int numTotalHits = results.totalHits;
           List<String> idx = se.searchIndex.get(i);
            if (idx == null) {
                //System.out.println("Are you here 1?");
		idx = new LinkedList<String>();
		se.searchIndex.put(String.valueOf(i), idx);
            }
            System.out.println("for i ="+ i+"  "+ numTotalHits + " total matching documents  "+ hits.length);
            for(int j = 0;j < numTotalHits;j++){
                Document doc = searcher.doc(hits[j].doc);
                idx.add(doc.get("docno"));
            }
        }
        BufferedReader in = null;
        String f = "qrels";  
        in = new BufferedReader(new FileReader(f));        
        String str = null;
			File file = new File("output.txt");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),false);
			BufferedWriter bw = new BufferedWriter(fw);        
        while ((str = in.readLine()) != null){
            String[] terms = str.split(" ");
            //System.out.println("int k "+k + "  term0 : " + terms[0] +" \t term 2 " + terms[2] );
            List<String> idx = se.searchIndex.get(terms[0]);
            if(idx.contains(terms[2])){
                bw.write(terms[0]+" 0 "+terms[2]+ " 1"+"\n");
                //System.out.println(terms[0]+" 0 "+terms[2]+ " 1");
            }
            else
                bw.write(terms[0]+" 0 "+terms[2]+ " 0"+"\n");
        }
        bw.close();
    }
  
    
}
