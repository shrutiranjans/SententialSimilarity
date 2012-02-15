/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import WordNet.WordNetSense;
import WordNet.WordNetType;
import edu.smu.tspell.wordnet.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import opennlp.tools.tokenize.*;
import java.lang.String.*;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

/**
 *
 * @author shrutiranjans
 */
public class TestModules {
    
    public static String[] Preprocess(String sentence)
    {
        InputStream modelIn = null;
        String tokens[] = null;
        
        try { modelIn = new FileInputStream("en-token.bin"); }
        catch (FileNotFoundException ex) {Logger.getLogger(TestModules.class.getName()).log(Level.SEVERE, null, ex);        }

        try
        {
          TokenizerModel model = new TokenizerModel(modelIn);
          Tokenizer tokenizer = new TokenizerME(model);
          char punctuationMarks[] = {',', '.', '?', '!', ':', ';', '"'};
          sentence = sentence.trim();
          sentence = sentence.toLowerCase();
          for (int j=0; j<punctuationMarks.length; j++)
          {
            sentence = sentence.replace(punctuationMarks[j],' ');
          }
          tokens = tokenizer.tokenize(sentence);
        }
        catch (IOException e) {}
        finally
        {if (modelIn != null) { try { modelIn.close();} catch (IOException e) { }}}
        
        for (int i = 0; i < tokens.length; i++)
        {
            if(tokens[i].compareToIgnoreCase("n't")==0) tokens[i] = "not";
            //System.out.println(tokens[i]);
        }
        return tokens;
    }
    
    public static int SynsetMatching(Synset syn, Set sentence, String nv)
    {
        //sentence doesn't has the syn's word
        int score = 0;
        String[] desc = GetSynsetDesc(syn, nv);
        int scores[] = new int[desc.length];
        for(int j=0; j<desc.length;j++)
        {
            scores[j] = 0;
            String[] descrip = Preprocess(desc[j]);
            for(int i=0; i<descrip.length;i++)
            {
                if(sentence.contains(descrip[i])) scores[j] += 1;
            }
            score += scores[j];
        }        
        return score;
    }

    public static String GetSynsetDescString(Synset syn, String nv)
    {
        String[] descs = GetSynsetDesc(syn, nv);
        String desc = "";
        for(int j=0; j<descs.length;j++) desc+=descs[j];
        return desc;
    }

    public static String[] GetSynsetDesc(Synset syn, String nv)
    {
        String[] desc =  new String[6];
        for(int i=0; i<desc.length; i++) desc[i]="";
        desc[0] = syn.getDefinition();
        if(nv.equals("NOUN"))
        {
            NounSynset synN = (NounSynset)syn;
            Synset[] hyponyms = synN.getHyponyms();
            for(int i=0; i<hyponyms.length; i++) desc[1] += (hyponyms[i].getDefinition() + ". ");
            
            Synset[] hypernyms = synN.getHypernyms();
            for(int i=0; i<hypernyms.length; i++) desc[2] += (hypernyms[i].getDefinition() + ". ");

            Synset[] holonyms = synN.getSubstanceHolonyms();
            for(int i=0; i<holonyms.length; i++) desc[3] += (holonyms[i].getDefinition()+". ");

            Synset[] meronyms = synN.getSubstanceMeronyms();
            for(int i=0; i<meronyms.length; i++) desc[4] += (meronyms[i].getDefinition()+". ");

            //Synset[] usages = synN.getUsages();
            //for(int i=0; i<usages.length; i++) desc[5] += (usages[i].getDefinition()+". ");
        }
        if(nv.equals("VERB"))
        {
            VerbSynset synV = (VerbSynset)syn;
            Synset[] entails = synV.getEntailments();
            for(int i=0; i<entails.length; i++) desc[1] += (entails[i].getDefinition() + ". ");

            Synset[] hypernyms = synV.getHypernyms();
            for(int i=0; i<hypernyms.length; i++) desc[2] += (hypernyms[i].getDefinition() + ". ");

            Synset[] troponyms = synV.getTroponyms();
            for(int i=0; i<troponyms.length; i++) desc[3] += (troponyms[i].getDefinition()+". ");

            Synset[] outcomes = synV.getOutcomes();
            for(int i=0; i<outcomes.length; i++) desc[4] += (outcomes[i].getDefinition()+". ");

            //Synset[] usages = synV.getUsages();
            //for(int i=0; i<usages.length; i++) desc[5] += (usages[i].getDefinition()+". ");
        }
        if(nv.equals("ADJECTIVE"))
        {
            AdjectiveSynset synAdj = (AdjectiveSynset)syn;
            Synset[] attrs = synAdj.getAttributes();
            for(int i=0; i<attrs.length; i++) desc[1] += (attrs[i].getDefinition() + ". ");

            Synset[] similar = synAdj.getSimilar();
            for(int i=0; i<similar.length; i++) desc[2] += (similar[i].getDefinition() + ". ");

            Synset[] topics = synAdj.getTopics();
            for(int i=0; i<topics.length; i++) desc[3] += (topics[i].getDefinition()+". ");

            Synset[] related = synAdj.getRelated();
            for(int i=0; i<related.length; i++) desc[4] += (related[i].getDefinition()+". ");

            //Synset[] usages = synAdj.getUsages();
            //for(int i=0; i<usages.length; i++) desc[5] += (usages[i].getDefinition()+". ");
        }

        return desc;
    }
    
    public static WordNetSense[] Lesk(String sentence) throws IOException
    {
        String[] processedSentence = Preprocess(sentence);
        Set totalSet = new HashSet();
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        POSTaggerME posTagger = new POSTaggerME(new POSModel(new FileInputStream(new File("en-pos-maxent.bin"))));
        String[] tags = posTagger.tag(processedSentence);
        WordNetSense[] senses = new WordNetSense[processedSentence.length];
        //int[] senses = new int[processedSentence.length];
        for(int j=0; j<processedSentence.length; j++) {
            String word = processedSentence[j];
            int sense = -1;
            WordNetType type = WordNetType.DEFAULT;
            senses[j] = new WordNetSense(word, sense, type); 
            totalSet.add(word);
        }

        for(int i=0; i<tags.length; i++)
        {
            System.out.println(tags[i]);
            Set newset = new HashSet(totalSet);
            newset.remove(processedSentence[i]);
            if(tags[i].equals("VBZ")||tags[i].equals("VBG")||tags[i].equals("VBD")||tags[i].equals("VBP")||tags[i].equals("VB"))
            {
                Synset[] synsetsVerb = database.getSynsets(processedSentence[i], SynsetType.VERB);
                int bestScore = 0;
                for(int j=0; j<synsetsVerb.length;j++)
                {
                    int score = SynsetMatching(synsetsVerb[j], newset, "VERB");
                    if(score>=bestScore) {
                        bestScore = score; 
                        senses[i].sense=j;
                        senses[i].type = WordNetType.VERB;
                    }
                }
                if(senses[i].sense != -1) 
                    System.out.println(synsetsVerb[senses[i].sense].getDefinition());
            }
            else if(tags[i].equals("NN")||tags[i].equals("NNS"))
            {
                Synset[] synsetsNoun = database.getSynsets(processedSentence[i], SynsetType.NOUN);
                int bestScore = 0;
                for(int j=0; j<synsetsNoun.length;j++)
                {
                    int score = SynsetMatching(synsetsNoun[j], newset, "NOUN");
                    if(score>=bestScore) {
                        bestScore = score; 
                        senses[i].sense=j;
                        senses[i].type = WordNetType.VERB;
                    }
                }
                if(senses[i].sense != -1) 
                    System.out.println(synsetsNoun[senses[i].sense].getDefinition());
            }
//            else if(tags[i].substring(0, 2).equals("JJ"))
//            {
//                Synset[] synsetsAdjectives = database.getSynsets(processedSentence[i], SynsetType.ADJECTIVE);
//                int bestScore = 0;
//                for(int j=0; j<synsetsAdjectives.length;j++)
//                {
//                    int score = SynsetMatching(synsetsAdjectives[j], newset, "ADJECTIVE");
//                    if(score>=bestScore) {bestScore = score; senses[i]=j;}
//                }
//                if(senses[i]!=-1) System.out.println(synsetsAdjectives[senses[i]].getDefinition());
//            }
        }
        return senses;
    }
    
    public static void main(String args[]) {
        String sentence = "Ranjan is the running.";
//        try {
//            WordNetSense[] senses = Lesk(sentence);
//            for (int i=0; i<senses.length; i++)
//                System.out.println(senses[i].sense);
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
        String[] processedSentence = Preprocess(sentence);
        try {
            POSTaggerME posTagger = new POSTaggerME(new POSModel(new FileInputStream(new File("en-pos-maxent.bin"))));
            String[] tags = posTagger.tag(processedSentence);
            for (int i=0; i<tags.length; i++)
                System.out.println(processedSentence[i] + ", " + tags[i]);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
}
