/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import WordSimilarity.DISCOSimilarity;
import WordSimilarity.LSASimilarity;
import java.util.*;
import java.io.*;

import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

/**
 *
 * @author shrutiranjans
 */
public class LexicalSimilarity {
    
    public static ArrayList<TaggedWord> StopWordRemoval(ArrayList<TaggedWord> taggedWords) {
        ArrayList<TaggedWord> newList = new ArrayList<TaggedWord>();
        
        try {
            BufferedReader br = new BufferedReader(new FileReader("nltk_stoplist.txt"));
            String stopwordsLine = br.readLine();
            br.close();
            
            String[] stopwords = stopwordsLine.split(",");
            HashMap<String, String> stopwordsDict = new HashMap<String, String>();
            for (int i=0; i<stopwords.length; i++) {
                stopwordsDict.put(stopwords[i], stopwords[i]);
            }
            
            for (int i=0; i<taggedWords.size(); i++) {
                String word = taggedWords.get(i).word();
                String posTag = taggedWords.get(i).tag();
                
                if (!stopwordsDict.containsKey(word.toLowerCase())) {
                    String newWord, newPosTag;
                    newWord = word;
                    newPosTag = posTag;
                    newList.add(new TaggedWord(newWord, newPosTag));
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return newList;
    }
    
    public static ArrayList<TaggedWord> Preprocess(ArrayList<TaggedWord> taggedWords) {
        ArrayList<TaggedWord> newList = new ArrayList<TaggedWord>();
        taggedWords = StopWordRemoval(taggedWords);        
        
        String[] punctuations = {",", ".", "?", "!", ":", ";", "\"", "-", "--", "'"};//, "/", "\\", "<", ">", "#", "&", "*", "(", ")", "{", "}", "[", "]", "~", "|"};
        HashMap<String, String> punctuationMarks = new HashMap<String, String>();
        for (int i=0; i<punctuations.length; i++) {
            punctuationMarks.put(punctuations[i], punctuations[i]);
        }
        
        for (int i = 0; i < taggedWords.size(); i++)
        {
            String word = taggedWords.get(i).word();
            String posTag = taggedWords.get(i).tag();
            
            if (!(posTag.length()>2 && posTag.substring(0, 3).equals("NNP")))
                word = word.toLowerCase();
            
            word = word.toLowerCase();
            if (!punctuationMarks.containsKey(word)) {
                String newWord, newPosTag;    
                if(word.equals("n't"))
                    newWord = "not";
                else if(word.equals("'s"))
                    newWord = "is";
                else
                    newWord = word;
                newPosTag = posTag;
                newList.add(new TaggedWord(newWord, newPosTag));
            }                
            
        }
        return newList;
    }
    
    public static ArrayList<TaggedWord> StanfordParse(String sentence, LexicalizedParser lp) {
        
        TokenizerFactory<CoreLabel> tokenizerFactory =
		PTBTokenizer.factory(new CoreLabelTokenFactory(), "");        
        List<CoreLabel> rawWords2 =
		tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
        Tree parse = lp.apply(rawWords2);
        ArrayList<TaggedWord> taggedWords = parse.taggedYield();
                
        return taggedWords;        
                
    }
    
    public static double LexicalSimilarityScore(String sentence1, String sentence2, DISCOSimilarity discoRAM, LexicalizedParser lp) {
        
        ArrayList<TaggedWord> taggedWords1 = Preprocess(StanfordParse(sentence1, lp));
        ArrayList<TaggedWord> taggedWords2 = Preprocess(StanfordParse(sentence2, lp));
        
        //System.out.println(taggedWords1.size() + "," + taggedWords2.size());
        
        // array of edge weights with default weight 0
        int length1 = taggedWords1.size();
        int length2 = taggedWords2.size();
        int arrSize = Math.max(length1, length2);
        double[][] array = new double[arrSize][arrSize];
        for (int i=0; i<arrSize; i++) {
            for (int j=0; j<arrSize; j++) {
                array[i][j] = 0;
            }
        }
        for (int i=0; i<length1; i++) {
            for (int j=0; j<length2; j++) {
                String word1 = taggedWords1.get(i).word();
                String word2 = taggedWords2.get(j).word();
                double edgeWeight = 0;
                
                // LSA Similarity
                //edgeWeight = LSASimilarity.LSAWordSimilarity(word1, word2);
                
                // DISCO Similarity
                //DISCOSimilarity discoObj = new DISCOSimilarity();
                try {
                    if (word1.equals(word2))
                        edgeWeight = 1;
                    else {
                        //edgeWeight = discoRAM.similarity2(word1, word2);
                        edgeWeight = LSASimilarity.LSAWordSimilarity(word1, word2);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                } 
                                                
                array[i][j] = edgeWeight;
            }
        }
        
        //System.out.println("Hungarian starts " + arrSize);
        
        double finalScore;
        String sumType = "max";
        int minLength = Math.min(length1, length2);
        //finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/minLength * 5;
        finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/arrSize * 5;
        
        return finalScore;
    }
    
    public static void main(String args[]) {
        String sentence = "Ranjan's an asshole.";   
        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
        System.out.println(StanfordParse(sentence, lp));
    }
    
}
