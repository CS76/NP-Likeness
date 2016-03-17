/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.scorer;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import uk.ac.ebi.cheminformatics.nplikeness.misc.NPScorerConstants;
import uk.ac.ebi.cheminformatics.nplikeness.moleculecuration.MoleculeCuration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculates atom signatures of given molecule or list of molecules. Molecules
 * must be tagged with UUID before using the methods in this class. The UUID
 * helps in identifying the signatures that belong to a particular molecule.
 *
 * @author kalai
 */
public class AtomSignatureGenerator {
    MoleculeCuration curator = new MoleculeCuration();
    private boolean inputIsSDF = true;
    ElectronDonation model = ElectronDonation.cdk();
    CycleFinder cycles = Cycles.cdkAromaticSet();
    Aromaticity aromaticity = new Aromaticity(model, cycles);

    /**
     * Takes a molecule and calculates atom signatures tagged with the UUID of
     * the molecule
     */
    public List<String> generateAtomSignatures(IAtomContainer atomContainer) {

        List<String> atomSignatures = new ArrayList<String>();
        UUID uuid;

        if (atomContainer.getProperty(NPScorerConstants.MOLECULE_ID) == null) {
            uuid = UUID.randomUUID();
            atomContainer.setProperty(NPScorerConstants.MOLECULE_ID, uuid);
        } else {
            uuid = UUID.fromString((String) atomContainer.getProperty(NPScorerConstants.MOLECULE_ID));
        }
        calculateAromaticity(atomContainer);
        for (IAtom atom : atomContainer.atoms()) {
            try {
                AtomSignature atomSignature = new AtomSignature(atom, NPScorerConstants.SIGNATURE_HEIGHT, atomContainer);
                String signature = uuid.toString() + "|" + atomSignature.toCanonicalString();
                atomSignatures.add(signature);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return atomSignatures;
    }

    private void calculateAromaticity(IAtomContainer molecule) {
        Object value = molecule.getProperty(NPScorerConstants.AROMATICITY_PERCEIVED);
        if (value == null) {
            try {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
                aromaticity.apply(molecule);
            } catch (Exception e) {
                e.printStackTrace();
            }
            molecule.setProperty(NPScorerConstants.AROMATICITY_PERCEIVED, true);
        }
    }

    /**
     * Takes list of molecules and calculates atom signatures tagged with the
     * UUID of the molecules
     *
     * @param moleculeList
     * @return
     * @throws Exception
     */
    public List<String> generateAtomSignatures(List<IAtomContainer> moleculeList) throws Exception {

        List<String> totalAtomSignatures = new ArrayList<String>();
        for (IAtomContainer atomContainer : moleculeList) {
            List<String> atomSignatures = generateAtomSignatures(atomContainer);
            totalAtomSignatures.addAll(atomSignatures);
        }
        return totalAtomSignatures;

    }

    /**
     * Takes string input of SDFile and writes out the signatures to the
     * specified output file
     *
     * @param file / smi file
     * @throws Exception
     */
    public void generateAtomSignatures(String file, String signaturesFile) {

        IteratingSDFReader reader;
        LineNumberReader smilesReader;
        FileWriter writer;


        try {
            if (inputIsSDF) {
                reader = new IteratingSDFReader(new FileReader(new File(file)), DefaultChemObjectBuilder.getInstance());
                reader.setSkip(true);
                writer = new FileWriter(new File(signaturesFile));

                System.out.println("Reading file and generating signatures... ");
                int count = 0;
                while (reader.hasNext()) {
                    IAtomContainer molecule = reader.next();
                    curateAndWrite(molecule, writer);
                    System.out.println(count++);
                }
                writer.close();
            } else {
                smilesReader = new LineNumberReader(new InputStreamReader(new FileInputStream(new File(file))));
                writer = new FileWriter(new File(signaturesFile));
                String line;
                int count = 0;

                System.out.println("Reading file and generating signatures... ");
                while ((line = smilesReader.readLine()) != null) {
                    String smiles_names = line;
                    try {
                        String[] splitted = smiles_names.split("\\s+");
                        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
                        IAtomContainer molecule = sp.parseSmiles(splitted[0]);
                        curateAndWrite(molecule, writer);
                        System.out.println(count++);
                    } catch (InvalidSmilesException ex) {
                        Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                smilesReader.close();
                writer.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void curateAndWrite(IAtomContainer molecule, FileWriter writer) {
        List<IAtomContainer> containers = curator.curateMolecule(AtomContainerManipulator.removeHydrogens(molecule));
        List<String> atomSignatures = null;
        try {
            atomSignatures = generateAtomSignatures(containers);
            for (String signature : atomSignatures) {
                writer.append(signature).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setInputIsSDF(boolean inputIsSDF) {
        this.inputIsSDF = inputIsSDF;
    }

}
