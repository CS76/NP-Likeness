/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.misc;

/**
 * Constants for NP-score calculator
 * @author kalai
 */
public final class NPScorerConstants {

    public static final String MOLECULE_ID = "UNIQUE_MOLECULE_ID";
    public static final String FRAGMENT_SCORE = "ATOM_CENTERED_FRAGMENT_SCORE";
    public static final String SIGNATURE = "ATOM_SIGNATURE";
    public static final String NATURAL_PRODUCT_LIKENESS_SCORE = "NATURAL_PRODUCT_LIKENESS_SCORE";
    public static final Double VERSION = 2.1;
    public static int SIGNATURE_HEIGHT = 2;
    public static int HOSE_HEIGHT = 2;
    public static final String OCCURENCE_COUNT_NP = "OCCURENCE_COUNT_NPset";
    public static final String OCCURENCE_COUNT_SM = "OCCURENCE_COUNT_SMset";
    public static double NP_count = 0d;
    public static double SM_count = 0d;
    public static String SERIALIZED_NP_SIGNATURES_FILE = "";
    public static String SERIALIZED_SM_SIGNATURES_FILE = "";
    public static String TRAINING_SDFILE = "";
    public static final String AROMATICITY_PERCEIVED = "AROMATICITY_PERCEIVED";
    public static String  NP_TRAINING_FILE = "/signaturesdata/npsignatures.out";
    public static String  SM_TRAINING_FILE = "/signaturesdata/smsignatures.out";
    public static String  NP_TRAINING_FILE_HEIGHT_3 = "/signaturesdata/np-3.out";
    public static String  SM_TRAINING_FILE_HEIGHT_3 = "/signaturesdata/sm-3.out";
    public static boolean EXTERNAL_SM_TRAINING_DATA = false;
    public static boolean EXTERNAL_NP_TRAINING_DATA = false;

    
}
