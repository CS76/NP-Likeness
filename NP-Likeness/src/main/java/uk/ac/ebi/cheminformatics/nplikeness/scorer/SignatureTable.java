/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.scorer;

import uk.ac.ebi.cheminformatics.nplikeness.misc.NPScorerConstants;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class to load pre-calculated serialized training signatures
 *
 * @author kalai
 */
public class SignatureTable implements Serializable {

    //static final long serialVersionUID = 101071956009L;
    public Double getNPUUIDCount(String signature) {
        return npSignatureMap.containsKey(signature) ? npSignatureMap.get(signature) : 0d;
    }

    public Double getSMUUIDCount(String signature) {

        return smSignatureMap.containsKey(signature) ? smSignatureMap.get(signature) : 0d;
    }

    private Double ratioSM_NP_moleculesCount;
    private Map<String, Double> npSignatureMap;
    private Map<String, Double> smSignatureMap;

    /**
     * Loads serialized NP and SM training signatures
     */
    private SignatureTable() {
        try {
            System.out.println("** Loading training signatures set....");
            npSignatureMap = generateNPsignaturesTable();
            smSignatureMap = generateSMsignaturesTable();
            ratioSM_NP_moleculesCount = (double) total_SM_count / (double) total_NP_count;
            // System.out.println("Ratio: " + ratioSM_NP_moleculesCount);
            crossCheckDatasets();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignatureTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.err.println("There was a problem loading the Signature Tables: " + ex.getMessage());
        }
    }

    private void crossCheckDatasets() {
        int smInNP = 0;
        int npInSM = 0;
        for (Object obj : npSignatureMap.keySet()) {
            if (smSignatureMap.containsKey((String) obj)) {
                npInSM++;
            }
        }
        for (Object obj : smSignatureMap.keySet()) {
            if (npSignatureMap.containsKey((String) obj)) {
                smInNP++;
            }
        }
//        System.out.println("total sm in NP = " + smInNP);
//        System.out.println("total np in SM = " + npInSM);
    }

    public Double getTotalSMFrequency() {
        return ratioSM_NP_moleculesCount;
    }

    public static class SignatureTableGeneratorHolder {

        private static final SignatureTable INSTANCE = new SignatureTable();
    }

    public static SignatureTable getInstance() {
        return SignatureTableGeneratorHolder.INSTANCE;
    }

    int total_NP_count = 0;
    int total_SM_count = 0;

    public Double getFragmentWeight(String signature) {

        Double npCount = this.getNPUUIDCount(signature);
        Double smCount = this.getSMUUIDCount(signature);

        if (npCount != 0d) {

            return smCount != 0d ? Math.log10((npCount / smCount) * this.getTotalSMFrequency()) : Math.log10((npCount / 0.5) * this.getTotalSMFrequency());
        } else {
            return smCount != 0d ? Math.log10((0.5 / smCount) * this.getTotalSMFrequency()) : 0d;
        }
    }

    private Map<String, Double> generateNPsignaturesTable() throws IOException, ClassNotFoundException {

        InputStream fis = null;
        if (!NPScorerConstants.EXTERNAL_NP_TRAINING_DATA) {
            if (NPScorerConstants.SIGNATURE_HEIGHT == 2) {
                fis = SignatureTable.class.getResourceAsStream(NPScorerConstants.NP_TRAINING_FILE);
            } else if (NPScorerConstants.SIGNATURE_HEIGHT == 3) {
                fis = SignatureTable.class.getResourceAsStream(NPScorerConstants.NP_TRAINING_FILE_HEIGHT_3);
            } else {
                System.out.println("Unrecognised signature height option. Please use only 2/3 when using the training file distributed with this" +
                        " package.");
                System.exit(0);
            }
        } else {
            System.out.println("Loading user NP training..");
            fis = new FileInputStream(new File(NPScorerConstants.NP_TRAINING_FILE));
        }
        ObjectInputStream ois = new ObjectInputStream(fis);
        Map NPSignaturesMap = (Map) ois.readObject();
        Integer np_count = (Integer) ois.readObject();
        total_NP_count = np_count.intValue();
        System.out.println("Total natural product structures: " + total_NP_count);
        System.out.println("Unique NP fragments considered: " + NPSignaturesMap.keySet().size());
        ois.close();
        return NPSignaturesMap;
    }

    private Map<String, Double> generateSMsignaturesTable() throws IOException, ClassNotFoundException {


        InputStream fis = null;
        if (!NPScorerConstants.EXTERNAL_SM_TRAINING_DATA) {
            if (NPScorerConstants.SIGNATURE_HEIGHT == 2) {
                fis = SignatureTable.class.getResourceAsStream(NPScorerConstants.SM_TRAINING_FILE);
            } else if (NPScorerConstants.SIGNATURE_HEIGHT == 3) {
                fis = SignatureTable.class.getResourceAsStream(NPScorerConstants.SM_TRAINING_FILE_HEIGHT_3);
            } else {
                System.out.println("Unrecognised signature height option. Please use only 2/3 when using the training file distributed with this" +
                        " package.");
                System.exit(0);
            }
        } else {
            System.out.println("Loading user SM training..");
            fis = new FileInputStream(new File(NPScorerConstants.SM_TRAINING_FILE));
        }
        ObjectInputStream ois = new ObjectInputStream(fis);
        Map SMSignaturesMap = (Map) ois.readObject();
        Integer sm_count = (Integer) ois.readObject();
        total_SM_count = sm_count.intValue();
        System.out.println("Total synthetic structures: " + total_SM_count);
        System.out.println("Unique NP fragments considered: " + SMSignaturesMap.keySet().size());
        ois.close();
        return SMSignaturesMap;
    }
}
