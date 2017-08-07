/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.moleculecuration;

import org.openscience.cdk.RingSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.BondManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import uk.ac.ebi.cheminformatics.nplikeness.misc.LinearSugars;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contains method to remove linear and ring sugars
 *
 * @author kalai
 */
public class SugarRemover {

    private final LinearSugars linearSugarChains;
    IMolecularFormula molecularFormula = null;

    /**
     * Loads singleton class of linear sugar chains
     */
    public SugarRemover() {
        linearSugarChains = LinearSugars.getInstance();
        molecularFormula = new MolecularFormula();
    }

    public List<IAtomContainer> removeSugars(List<IAtomContainer> moleculeList) {


        List<IAtomContainer> sugarRemovedMolecules = new ArrayList<IAtomContainer>();
        if (!moleculeList.isEmpty()) {
            for (IAtomContainer molecule : moleculeList) {
                try {

                    IRingSet ringset = Cycles.sssr(molecule).toRingSet();
                    for (IAtomContainer one_ring : ringset.atomContainers()) {
                        molecularFormula = MolecularFormulaManipulator.getMolecularFormula(one_ring);
                        String formula = MolecularFormulaManipulator.getString(molecularFormula);
                        IBond.Order bondorder = AtomContainerManipulator.getMaximumBondOrder(one_ring);
                        if (formula.equals("C5O") | formula.equals("C4O") | formula.equals("C6O")) {
                            if (IBond.Order.SINGLE.equals(bondorder)) {
                                if (shouldRemoveRing(one_ring, molecule, ringset) == true) {
                                    for (IAtom atom : one_ring.atoms()) {
                                        {
                                            molecule.removeAtomAndConnectedElectronContainers(atom);
                                        }
                                    }
                                }

                            }
                        }
                    }
                    Map<Object, Object> properties = molecule.getProperties();
                    IAtomContainerSet molset = ConnectivityChecker.partitionIntoMolecules(molecule);
                    for (int i = 0; i < molset.getAtomContainerCount(); i++) {
                        molset.getAtomContainer(i).setProperties(properties);
                        int size = molset.getAtomContainer(i).getBondCount();
                        if (size >= 5) {
                            if (!linearSugarChains.hasSugarChains(molset.getAtomContainer(i), ringset.getAtomContainerCount())) {

                                sugarRemovedMolecules.add((IAtomContainer) molset.getAtomContainer(i));
                            }
                        }
                    }
                    //
                } catch (NullPointerException e) {
                } catch (CDKException e) {
                }
            }
        }
        return sugarRemovedMolecules;

    }

    private boolean shouldRemoveRing(IAtomContainer possibleSugarRing, IAtomContainer molecule, IRingSet sugarRingsSet) {

        boolean shouldRemoveRing = false;
        List<IAtom> allConnectedAtoms = new ArrayList<IAtom>();
        List<IBond> bonds = new ArrayList<IBond>();
        int oxygenAtomCount = 0;

        IRingSet connectedRings = sugarRingsSet.getConnectedRings((IRing) possibleSugarRing);

        /*
        * get bonds to check for bond order of connected atoms in a sugar ring
        *
        */
        for (IAtom atom : possibleSugarRing.atoms()) {
            bonds.addAll(molecule.getConnectedBondsList(atom));
        }

        if (IBond.Order.SINGLE.equals(BondManipulator.getMaximumBondOrder(bonds))
                && connectedRings.getAtomContainerCount() == 0) {

            /*
            * get connected atoms of all atoms in sugar ring to check for glycoside bond
            */
            for (IAtom atom : possibleSugarRing.atoms()) {
                List<IAtom> connectedAtoms = molecule.getConnectedAtomsList(atom);
                allConnectedAtoms.addAll(connectedAtoms);
            }

            for (IAtom connected_atom : allConnectedAtoms) {
                if (!possibleSugarRing.contains(connected_atom)) {
                    if (connected_atom.getSymbol().matches("O")) {
                        oxygenAtomCount++;
                    }
                }
            }
            if (oxygenAtomCount > 0) {
                return true;
            }
        }
        return shouldRemoveRing;
    }

    private boolean hasSugarRingsIn(IAtomContainer molecule) {
        IRingSet totalRingsInMolecule = getRingsInTheMolecule(molecule);
        for (IAtomContainer ring : totalRingsInMolecule.atomContainers()) {
            if (isSugarRing(ring)) {
                return true;
            }
        }
        return false;
    }

    private RingSet getRingsInTheMolecule(IAtomContainer molecule) {
        return (RingSet) Cycles.sssr(molecule).toRingSet();
    }

    private boolean isSugarRing(IAtomContainer ring) {
        IMolecularFormula molecularFormula = MolecularFormulaManipulator.getMolecularFormula(ring);
        String formula = MolecularFormulaManipulator.getString(molecularFormula);
        if (formula.equals("C5O") | formula.equals("C4O") | formula.equals("C6O")) {
            return true;
        }
        return false;
    }


}
