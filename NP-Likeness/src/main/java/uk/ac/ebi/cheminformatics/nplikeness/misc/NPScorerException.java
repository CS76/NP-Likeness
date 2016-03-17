/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.misc;

/**
 * Exceptions for NP-likeness calculator
 *
 * @author kalai
 */
public class NPScorerException extends Exception {

    public static final String ERROR_GENERATING_ATOM_SIGNATURES = "Error in generating atom signatures for the molecule; Use TagMoleculeWithUUID class to get UUID's for molecules";
    public static final String ILLEGAL_ARGUMENT = "Incorrect input. Please enter the location of SDFile followed by true/false to reconstruct fragments with scores";

    public NPScorerException(String className, String type) {
        super(type + " Class: " + className + "");
    }
}
