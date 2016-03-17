/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.misc;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to get sorted scores given a scores file calculated using
 * NPScoreCalculator
 *
 * @author kalai
 */
public class SortScores {


    LineNumberReader reader = null;

    public List<String> getSortedScores(String file) {
        Map<String, Double> score_list = new HashMap<String, Double>();
        List<String> molecule_Score = new ArrayList<String>();
        try {

            try {
                reader = new LineNumberReader(new BufferedReader(new FileReader(file)));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SortScores.class.getName()).log(Level.SEVERE, null, ex);
            }
            String line;

            while ((line = reader.readLine()) != null) {
                String[] uuid_score = line.split(";");
                if (uuid_score.length == 2) {
                    score_list.put(uuid_score[0], Double.parseDouble(uuid_score[1]));
                } else {
                    score_list.put(uuid_score[0] + ";" + uuid_score[1], Double.parseDouble(uuid_score[2]));
                }

            }
            List<Entry<String, Double>> sorted_score_list = CollectionUtilities.sortByValue(score_list);
            for (Entry<String, Double> entry : sorted_score_list) {
                molecule_Score.add(entry.getKey() + ";" + entry.getValue());
            }
        } catch (IOException ex) {
            Logger.getLogger(SortScores.class.getName()).log(Level.SEVERE, null, ex);
        }
        return molecule_Score;
    }
}
