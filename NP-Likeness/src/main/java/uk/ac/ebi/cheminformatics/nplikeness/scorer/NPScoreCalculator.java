/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.scorer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import uk.ac.ebi.cheminformatics.nplikeness.misc.NPScorerConstants;

/**
 * Class to read SDFile and calculate NP-likeness score
 *
 * @author kalai
 */
public class NPScoreCalculator {

    private IteratingSDFReader reader = null;
    private SDFWriter moleculeWithScoreWriter = null;
    //    private IteratingSMILESReader smilesReader = null;
    private LineNumberReader smilesReader = null;
    private BufferedWriter smilesWriter = null;
    private BufferedWriter fragmentsSMILESWriter = null;
    //private BufferedWriter uuidScoreWriter = null;
    private boolean reconstructFragments = false;
    private SignatureTableLookUp tableToLookUp = null;
    public boolean inputIsSDF = true;
    public boolean outputIsSDF = true;
    private SmilesGenerator smilesGenerator = null;
    private IChemObjectBuilder iChemObjectBuilder = null;

    public NPScoreCalculator() {
        this.tableToLookUp = new SignatureTableLookUp();
        this.smilesGenerator = SmilesGenerator.generic().aromatic();
        this.iChemObjectBuilder = SilentChemObjectBuilder.getInstance();
    }

    public NPScoreCalculator(int height) {
        NPScorerConstants.SIGNATURE_HEIGHT = height;
        this.tableToLookUp = new SignatureTableLookUp();
        this.smilesGenerator = SmilesGenerator.absolute().aromatic();
        this.iChemObjectBuilder = SilentChemObjectBuilder.getInstance();
    }

    /**
     * specify if
     *
     * @param reconstructFragments
     */
    public void setReconstructFragments(boolean reconstructFragments) {
        this.reconstructFragments = reconstructFragments;
    }

    /**
     * Takes CDK atomContainer and return NP-score tagged with UUID
     *
     * @param molecule
     * @return
     * @throws Exception
     */
    public String curateAndScore(IAtomContainer molecule) {
        IAtomContainer container = AtomContainerManipulator.removeHydrogens(molecule);
        return tableToLookUp.calculateScores(container);
    }

    public String curateAndScoreIgnoringH(IAtomContainer molecule) {
        return tableToLookUp.calculateScores(molecule);
    }

    /**
     * Takes CDK atomContainer and returns molecule score and reconstructed fragments
     */
    public Map<String, List<IAtomContainer>> curateScoreAndReconstruct(IAtomContainer molecule) throws Exception {
        IAtomContainer container = AtomContainerManipulator.removeHydrogens(molecule);
        return tableToLookUp.getScoreAndFragments(container);
    }

    /**
     * Takes input of SDFile and iteratively score and write molecules to specified SDF outFile
     *
     * @param inFile
     * @param outfile
     * @throws java.io.IOException
     */
    public void process(File inFile, File outfile) throws IOException {
        createReaderWriter(inFile, outfile);
        iterate();
        closeAllWriters();
    }

    /**
     * Takes input of SDFile and iteratively score and write molecules to specified SDF outFile.
     * Use this method if interested in retrieving fragments(SMILES) of each molecule with its
     * associated fragment-score.
     *
     * @param file
     * @param outfile
     * @param outFragmentsFile
     * @throws java.io.IOException
     */
    public void process(File file, File outfile, File outFragmentsFile) {
        createAllReaderWriters(file, outfile, outFragmentsFile);
        iterate();
        closeAllWriters();

    }

    private void createReaderWriter(File inFile, File outFile) {
        if (inputIsSDF) {
            createSDFReader(inFile);
        } else {
            createSMILESReader(inFile);
        }
        if (outputIsSDF) {
            createSDFWriter(outFile);
        } else {
            createSMILESWriter(outFile);
        }
    }

    private void createSDFReader(File sdfFile) {
        try {
            reader = new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
            reader.setSkip(true);
            // uuidScoreWriter = new BufferedWriter(new FileWriter(outFile.getParent() + File.separator + FilenameUtils.getBaseName(outFile.getCanonicalPath()) + ".txt"));
        } catch (FileNotFoundException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createSDFWriter(File outFile) {
        try {
            moleculeWithScoreWriter = new SDFWriter(new FileWriter(outFile));
        } catch (FileNotFoundException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createSMILESReader(File smilesFile) {
        try {
//            smilesReader = new IteratingSMILESReader(new FileInputStream(smilesFile), DefaultChemObjectBuilder.getInstance());
//            smilesReader.setReaderMode(IChemObjectReader.Mode.RELAXED);
            smilesReader = new LineNumberReader(new InputStreamReader(new FileInputStream(smilesFile)));
            // uuidScoreWriter = new BufferedWriter(new FileWriter(outFile.getParent() + File.separator + FilenameUtils.getBaseName(outFile.getCanonicalPath()) + ".txt"));
        } catch (FileNotFoundException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createSMILESWriter(File outFile) {
        try {
            smilesWriter = new BufferedWriter(new FileWriter(outFile));
        } catch (FileNotFoundException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createAllReaderWriters(File sdfFile, File outFile, File outFragmentsFile) {
        try {
            createReaderWriter(sdfFile, outFile);
            fragmentsSMILESWriter = new BufferedWriter(new FileWriter(outFragmentsFile));
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void iterate() {
        System.out.println("** Calculating scores...");
        if (inputIsSDF) {
            iterateSDF();
        } else {
            iterateSMILES();
        }
    }

    private void iterateSDF() {
        int count = 0;
        while (reader.hasNext()) {
            try {
                IAtomContainer molecule = reader.next();
                Map properties = molecule.getProperties();
                List<IAtomContainer> fragments = score(molecule);
                System.out.println(count++);
                molecule.setProperties(properties);
                if (!outputIsSDF) {
                    writeBackSMILES(getSMILES(molecule), (String) molecule.getProperty(NPScorerConstants.NATURAL_PRODUCT_LIKENESS_SCORE), (String) molecule.getProperty(NPScorerConstants.MOLECULE_ID));
                } else {
                    writeBackSDF(molecule);
                }
                writeSMILES(fragments);
                //writeUuidScore(molecule);
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getSMILES(IAtomContainer mol) throws CDKException {
        CDKHydrogenAdder.getInstance(this.iChemObjectBuilder).addImplicitHydrogens(mol);
        return smilesGenerator.create(mol);
    }

    private void iterateSMILES() {
        System.out.println("iterating smiles");
        int count = 0;
        String line;
        try {
            while ((line = smilesReader.readLine()) != null) {
                String smiles_names = line;
                try {
                    String[] splitted = smiles_names.split("\\s+");
                    SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
                    IAtomContainer molecule = sp.parseSmiles(splitted[0]);
                    Map properties = molecule.getProperties();
                    List<IAtomContainer> fragments = score(molecule);
                    System.out.println(count++);
                    molecule.setProperties(properties);
                    if (!outputIsSDF) {
                        writeBackSMILES(splitted[0], (String) molecule.getProperty(NPScorerConstants.NATURAL_PRODUCT_LIKENESS_SCORE), (String) molecule.getProperty(NPScorerConstants.MOLECULE_ID) );
                    } else {
                        writeBackSDF(molecule);
                    }
                    writeSMILES(fragments);

                } catch (InvalidSmilesException ex) {
                    Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            smilesReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param molecule
     * @return
     * @throws Exception
     */
    public List<IAtomContainer> score(IAtomContainer molecule) throws Exception {
        String score;
        String uuid;
        List<IAtomContainer> fragments = new ArrayList<IAtomContainer>();
        if (reconstructFragments) {
            Map<String, List<IAtomContainer>> fragmentsWithScores = curateScoreAndReconstruct(molecule);
            String uuidScore = extractScore(fragmentsWithScores);
            score = getScore(uuidScore);
            uuid = getUUID(uuidScore);
            fragments = extractFragments(fragmentsWithScores);
        } else {
            String uuid_score = curateAndScore(molecule);
            score = getScore(uuid_score);
            uuid = getUUID(uuid_score);
        }
        molecule.setProperty(NPScorerConstants.NATURAL_PRODUCT_LIKENESS_SCORE, score);
        molecule.setProperty(NPScorerConstants.MOLECULE_ID, uuid);
        return fragments;
    }

    private String extractScore(Map<String, List<IAtomContainer>> fragmentsWithScores) {
        String uuid_Score = "";
        if (fragmentsWithScores.keySet().size() == 1) {
            for (Entry entry : fragmentsWithScores.entrySet()) {
                uuid_Score = (String) entry.getKey();
            }
        }
        return uuid_Score;
    }

    private List<IAtomContainer> extractFragments(Map<String, List<IAtomContainer>> fragmentsWithScores) {
        List<IAtomContainer> fragments = new ArrayList<IAtomContainer>();
        if (fragmentsWithScores.keySet().size() == 1) {
            for (Entry entry : fragmentsWithScores.entrySet()) {
                fragments = (List<IAtomContainer>) entry.getValue();
            }
        }
        return fragments;
    }

    private String getScore(String uuid_score) {
        String score = "";
        if (!uuid_score.isEmpty()) {
            String[] split = uuid_score.split("\\|");
            score = split[1];
        }
        return score;
    }

    private String getUUID(String uuid_score) {
        String uuid = "";
        if (!uuid_score.isEmpty()) {
            String[] split = uuid_score.split("\\|");
            uuid = split[0];
        }
        return uuid;
    }

    private void writeBackSDF(IAtomContainer container) {
        try {
            container.removeProperty(NPScorerConstants.AROMATICITY_PERCEIVED);
            moleculeWithScoreWriter.write(container);
        } catch (CDKException ex) {
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    private void writeBackSMILES(String smiles, String score, String molid) {
        try {
            smilesWriter.write(smiles + "\t" + score + "\t" + molid + "\n");
        } catch (Exception ex) {
            Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    private void writeSMILES(List<IAtomContainer> fragments) {
        if (reconstructFragments) {
            for (IAtomContainer fragment : fragments) {
                //System.out.print( (String)fragment.getProperty(NPScorerConstants.MOLECULE_ID) );
                try {
                    try {
                        fragmentsSMILESWriter.write( (String)fragment.getProperty(NPScorerConstants.MOLECULE_ID) + ";" + getSMILES(fragment) + ";" + (String)fragment.getProperty(NPScorerConstants.FRAGMENT_SCORE) + "\n");
                    } catch (CDKException e) {
                        e.printStackTrace();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);

                }
            }
        }
    }


    private void writeUuidScore(IAtomContainer molecule) {
        String uuid = molecule.getProperty(NPScorerConstants.MOLECULE_ID);
        String score = molecule.getProperty(NPScorerConstants.NATURAL_PRODUCT_LIKENESS_SCORE);
        try {
            if (!uuid.isEmpty() & !score.isEmpty()) {
                //  uuidScoreWriter.write(uuid + ";" + score + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isAromatic(IAtomContainer fragment) {
        Boolean value = (Boolean) fragment.getProperty(NPScorerConstants.AROMATICITY_PERCEIVED);
        if (value == null) {
            boolean isArom = false;
            try {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(fragment);
                isArom = CDKHueckelAromaticityDetector.detectAromaticity(fragment);
            } catch (CDKException ex) {
                Logger.getLogger(NPScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
            }
            return isArom;
        } else {
            fragment.removeProperty(NPScorerConstants.AROMATICITY_PERCEIVED);
            return value;
        }
    }

    private void closeAllWriters() {
        try {
            if (moleculeWithScoreWriter != null) {
                moleculeWithScoreWriter.close();
            }
            if (fragmentsSMILESWriter != null) {
                fragmentsSMILESWriter.close();
            }
            if (smilesWriter != null) {
                smilesWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setInputIsSDF(boolean inputIsSDF) {
        this.inputIsSDF = inputIsSDF;
    }

    public void setOutputIsSDF(boolean outputIsSDF) {
        this.outputIsSDF = outputIsSDF;
    }


    public boolean isInputIsSDF() {
        return inputIsSDF;
    }

    public boolean isOutputIsSDF() {
        return outputIsSDF;
    }

}
