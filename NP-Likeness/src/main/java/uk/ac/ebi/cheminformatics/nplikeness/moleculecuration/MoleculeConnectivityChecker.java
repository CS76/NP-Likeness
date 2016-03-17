/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.moleculecuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;

/**
 * Class to discard counter ions and other small disconnected fragments
 *
 * @author kalai
 */
public class MoleculeConnectivityChecker {

    public List<IAtomContainer> checkConnectivity(IAtomContainer molecule) {

        List<IAtomContainer> curated = new ArrayList<IAtomContainer>();
        Map<Object, Object> properties = molecule.getProperties();
        IAtomContainerSet mols = ConnectivityChecker.partitionIntoMolecules(molecule);
        for (int i = 0; i < mols.getAtomContainerCount(); i++) {
            mols.getAtomContainer(i).setProperties(properties);
            curated.add(mols.getAtomContainer(i));
        }
        return curated;
    }
}
