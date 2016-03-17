/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.misc;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.UUID;

/**
 * Utility class to tag CDK molecule with Universal Unique IDentifier
 *
 * @author kalai
 */
public class UUIDTagger {

      public IAtomContainer tagUUID(IAtomContainer molecule) {

            if (molecule.getProperty(NPScorerConstants.MOLECULE_ID) == null) {
                  UUID uuid = UUID.randomUUID();
                  molecule.setProperty(NPScorerConstants.MOLECULE_ID, uuid.toString());
            }
            return molecule;
      }
}