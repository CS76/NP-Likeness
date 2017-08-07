/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.scorer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.MoleculeFromSignatureBuilder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import signature.AbstractVertexSignature;
import signature.ColoredTree;
import uk.ac.ebi.cheminformatics.nplikeness.misc.NPScorerConstants;
import uk.ac.ebi.cheminformatics.nplikeness.moleculecuration.MoleculeCuration;

/**
 * This Class contains methods to break up input molecule into fragments and look them up in
 * training set and calculate scores.
 *
 * @author kalai
 */
public class SignatureTableLookUp {

    private final SignatureTable sigTable;
    final DecimalFormat df = new DecimalFormat("00.0000");
    AtomSignatureGenerator atomSignatureGenerator = null;
    MoleculeCuration curator = null;

    /**
     * Singleton constructor loads training set of signatures into memory only once
     */
    public SignatureTableLookUp() {
        sigTable = SignatureTable.getInstance();
        atomSignatureGenerator = new AtomSignatureGenerator();
        curator = new MoleculeCuration();
    }

    /**
     * Takes molecule, curate it, generate atom signatures, look up training set and calculate
     * scores.
     *
     * @param molecule
     * @return
     * @throws Exception
     */
    public String calculateScores(IAtomContainer molecule){

        String result_Score = "";
        List<String> atomSignatures = curateAndGetSignatures(molecule);
        if (atomSignatures.isEmpty()) {
            return result_Score;
        }
        String uuid = getUUID(atomSignatures.get(0));
        double score = 0d;
        int molecule_size = atomSignatures.size();
        for (String uuidSignature : atomSignatures) {
            double fragment_Weight = sigTable.getFragmentWeight(getFragment(uuidSignature));
            score += fragment_Weight;
        }
        /*
        * Normalization is done by dividing the summed up score by atom count of molecule to
        * prevent molecule having more atoms from gaining high score
        */
        if (molecule_size != 0) {
            double normalized_score = score / molecule_size;
            result_Score = uuid + "|" + df.format(normalized_score);
        }
        return result_Score;
    }

    private List<String> curateAndGetSignatures(IAtomContainer molecule) {
        List<String> atomSignatures = new ArrayList<String>();
        try {
            List<IAtomContainer> curatedMolecule = curator.curateMolecule(molecule);
            if (!curatedMolecule.isEmpty()) {
                atomSignatures = atomSignatureGenerator.generateAtomSignatures(curatedMolecule);
            } else {
                String name = (String) molecule.getProperty(CDKConstants.TITLE);
                if (name != null) {
                    System.out.println("Oops molecule " + name + " was just lost upon curation and it cannot be assigned score.");
                } else {
                    System.out.println("Oops.. a molecule was just lost upon curation and it cannot be assigned score.");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SignatureTableLookUp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return atomSignatures;
    }

    /**
     * Takes molecule, curate it, generate atom signatures, look up training set and calculate
     * scores. This method also reconstructs fragments from the atom signatures.
     */
    public Map<String, List<IAtomContainer>> getScoreAndFragments(IAtomContainer molecule) throws CDKException {


        String result_Score;
        Map<String, List<IAtomContainer>> score_and_fragments = new HashMap<String, List<IAtomContainer>>();
        ArrayList<IAtomContainer> reconstructedFragments = new ArrayList<IAtomContainer>();
        List<String> atomSignatures = curateAndGetSignatures(molecule);
        if (atomSignatures.isEmpty()) {
            return score_and_fragments;
        }
        String uuid = getUUID(atomSignatures.get(0));
        double score = 0d;
        int molecule_size = atomSignatures.size();
        for (String uuidSignature : atomSignatures) {
            String fragment = getFragment(uuidSignature);
            double fragment_Weight = sigTable.getFragmentWeight(fragment);
            score += fragment_Weight;
            IAtomContainer fragment_container = reconstruct(fragment);
            fragment_container.setProperty(NPScorerConstants.FRAGMENT_SCORE, df.format(fragment_Weight));
            fragment_container.setProperty(NPScorerConstants.MOLECULE_ID, uuid);
            reconstructedFragments.add(fragment_container);
        }
        if (score != 0.0 && molecule_size != 0) {
            double normalized_score = score / molecule_size;
            result_Score = uuid + "|" + df.format(normalized_score);
        } else {
            result_Score = uuid + "|" + 0.0;
        }
        score_and_fragments.put(result_Score, reconstructedFragments);
        return score_and_fragments;
    }

    private String getUUID(String uuidSignature) {
        String uuid = "";
        if (!uuidSignature.isEmpty()) {
            String[] split = uuidSignature.split("\\|");
            uuid = split[0];
        }
        return uuid;
    }

    private String getFragment(String uuidSignature) {
        String signature = "";
        if (!uuidSignature.isEmpty()) {
            String[] split = uuidSignature.split("\\|");
            signature = split[1];
        }
        return signature;
    }

    /**
     * Takes atom signature and reconstructs CDK molecule
     */
    public IAtomContainer reconstruct(String signature) throws CDKException {
        ColoredTree tree = AbstractVertexSignature.parse(signature);
        MoleculeFromSignatureBuilder builder = new MoleculeFromSignatureBuilder(DefaultChemObjectBuilder.getInstance());
        builder.makeFromColoredTree(tree);
        IAtomContainer generatedMolecule = builder.getAtomContainer();
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(generatedMolecule);

        ElectronDonation model = ElectronDonation.cdk();
        CycleFinder cycles = Cycles.cdkAromaticSet();
        Aromaticity aromaticity = new Aromaticity(model, cycles);
        aromaticity.apply(generatedMolecule);
        generatedMolecule.setProperty(NPScorerConstants.AROMATICITY_PERCEIVED,true);

        return generatedMolecule;
    }
}
