/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.misc;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.cheminformatics.nplikeness.scorer.AtomSignatureGenerator;
import uk.ac.ebi.cheminformatics.nplikeness.scorer.NPScoreCalculator;

/**
 * @author kalai
 */
public class InputParser {

    final String version = "Natural Product-likeness calculator v-" + String.valueOf(NPScorerConstants.VERSION);
    final String HELP_DESCRIPTION = "\t" + version + " calculates natural product-likeness of small molecules based on open-data of natural products."
            + " Input [Options] [Targets] for the application are specified below.";
    final String OUTPUTFILE_DESC = "\tFor input and output file please specify the type using -intype and " +
            "-outtype repectively. \n\tCurrently the types can be either sdf/smi.\n\t" +
            "If my mistake you input a sdf file but give it a different format type, \n\tthe program will not complain but will finish without any output." +
            " If output file options (out/outFragments) are unspecified, \n\tauto generated output files (based on Input format) will be written to the directory of the input file.";
    final String VERBOSE = version + "\n"
            + "Author: Kalai Vanii Jayaseelan\n"
            + "European Bioinformatics Institute\n"
            + "Contact: kalai@ebi.ac.uk";
    final Options options;
    boolean reconstructFragments = false;
    boolean scoringStarted = false;
    boolean dontScore = false;
    boolean serialize = false;
    boolean generateSignatures = false;

    final CommandLineParser parser;
    final HelpFormatter formatter;
    final DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
    final Date date = new Date();
    final String dateTime;
    NPScoreCalculator scorer;
    File inFile;
    File outFile;
    File fragmentsFile;
    File serializedFragmentsFile;
    FILE_TYPE in_file_type = null;
    FILE_TYPE out_file_type = null;


    public InputParser() {
        parser = new BasicParser();
        options = new Options();
        formatter = new HelpFormatter();
        dateTime = dateFormat.format(date);
        options.addOption("help", false, "Usage information");
        options.addOption("v", false, "Application info");
        options.addOption("in", true, "Input structure file to score or to generateSignatures");
        options.addOption("out", true, "Output structure from scoring");
        options.addOption("intype", true, "Input file type: Enter sdf/smi");
        options.addOption("outtype", true, "Output file type: Enter sdf/smi/json");
        options.addOption("reconstructFragments", true, "Optional boolean argument {true/false} to reconstruct fragments. If true individual fragments with scores are written "
                + "out in SMILES format");
        options.addOption("outFragments", true, "Output .txt file for reconstructed fragments. Specify this option only if 'reconstructFragments' is true." +
                " Alternatively, this option can also be specified along with 'generateSignatures' as a output file");
        options.addOption("generateSignatures", false, "Given input sdf/smi file use this function to generate atomSignatures for training molecules.");
        options.addOption("signatureHeight", true, "Signature height to be generated; Use this along 'generateSignatures'; Default size: 2");
        options.addOption("serializeSignatures", false, "Use this function to serialize .txt signatures file generated by this program using 'generateSignatures' option." +
                "The input for this function is taken via option 'inSignaturesFile'. Alternatively, this function can be used alongside 'generateSignatures' function " +
                "to directly serialize and store the generated signatures.");
        options.addOption("inSignaturesFile", true, "To specify atomSignatures .txt file generated by this program using 'generateSignatures' option; Used as input only for 'serializeSignatures' function.");
        options.addOption("npTrainer", true, "Serialized NP atomSignatures file generated by this program");
        options.addOption("smTrainer", true, "Serialized SM atomSignatures file generated by this program");
    }

    public enum FILE_TYPE {
        sdf, smi, json;
    }

    public void parseUserInput(String[] args) throws IOException {
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("help")) {
                formatter.printHelp(HELP_DESCRIPTION + "\n", options);
                System.out.println(OUTPUTFILE_DESC);
                System.exit(0);
            }
            if (commandLine.hasOption('v')) {
                System.out.println(VERBOSE);
                System.exit(0);
            }

            if (commandLine.hasOption("reconstructFragments")) {
                reconstructFragments = Boolean.valueOf(commandLine.getOptionValue("reconstructFragments"));
            }
            if (commandLine.hasOption("in")) {
                if (commandLine.hasOption("intype")) {
                    inFile = new File(commandLine.getOptionValue("in"));
                    in_file_type = parseFileType(commandLine.getOptionValue("intype"));
                    checkFileValidity(in_file_type);
                } else {
                    System.out.println("Please specify input type of file using -intype option[Example: -intype sdf/ -intype smi]");
                    System.exit(0);
                }
            }
            if (commandLine.hasOption("out")) {
                if (commandLine.hasOption("outtype")) {
                    outFile = new File(commandLine.getOptionValue("out"));
                    out_file_type = parseFileType(commandLine.getOptionValue("outtype"));
                    checkFileValidity(out_file_type);
                } else {
                    System.out.println("Please specify output type of file using -outtype option[Example: -outtype sdf/ -outtype smi]");
                    System.exit(0);
                }
            }
            if (commandLine.hasOption("outtype")) {
                out_file_type = parseFileType(commandLine.getOptionValue("outtype"));
            }

            if (commandLine.hasOption("outFragments")) {
                fragmentsFile = new File(commandLine.getOptionValue("outFragments"));
            }
            if (commandLine.hasOption("generateSignatures")) {
                if (commandLine.hasOption("in")) {
                    dontScore = true;
                    generateSignatures = true;
                } else {
                    System.out.println("Please input SDFile to generate signatures");
                    System.exit(0);
                }
            }
            if (commandLine.hasOption("signatureHeight")) {
                NPScorerConstants.SIGNATURE_HEIGHT = Integer.parseInt(commandLine.getOptionValue("signatureHeight"));
            }
            if (commandLine.hasOption("serializeSignatures")) {
                dontScore = true;
                if (commandLine.hasOption("generateSignatures") || commandLine.hasOption("inSignaturesFile")) {
                    serialize = true;
                } else {
                    System.out.println("No signatures file found to serialize !! Use this option alonside generateSignatures option to serialize the output .txt file" +
                            ", or input a filename using 'inSignaturesFile' option, generated using 'generateSignatures' option.");
                    System.exit(0);
                }
            }

            if (commandLine.hasOption("inSignaturesFile")) {
                fragmentsFile = new File(commandLine.getOptionValue("inSignaturesFile"));
            }

            if (commandLine.hasOption("npTrainer")) {
                File file = new File(commandLine.getOptionValue("npTrainer"));
                if (file.exists()) {
                    NPScorerConstants.NP_TRAINING_FILE = file.getCanonicalPath();
                    NPScorerConstants.EXTERNAL_NP_TRAINING_DATA = true;
                }
            }
            if (commandLine.hasOption("smTrainer")) {
                File file = new File(commandLine.getOptionValue("smTrainer"));
                if (file.exists()) {
                    NPScorerConstants.SM_TRAINING_FILE = file.getCanonicalPath();
                    NPScorerConstants.EXTERNAL_SM_TRAINING_DATA = true;
                }
            }
            if (commandLine.getArgList().size() > 0) {
                System.out.println("Unrecognised input option. Please Check the input options using -help");
            }
        } catch (UnrecognizedOptionException ure) {
            System.out.println("Unrecognised input option. Please Check the input options using -help");
            System.exit(0);
        } catch (ParseException ex) {
            Logger.getLogger(InputParser.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Unrecognised input option. Please Check the input options using -help");
            System.exit(0);
        }
        doWork();
    }

    private FILE_TYPE parseFileType(String value) {
        if (value.equalsIgnoreCase("sdf")) {
            return FILE_TYPE.sdf;
        } else if (value.equalsIgnoreCase("smi")) {
            return FILE_TYPE.smi;
        } else if (value.equalsIgnoreCase("json")) {
            return FILE_TYPE.json;
        }

        return null;
    }

    private void checkFileValidity(FILE_TYPE type) {
        if (type == null) {
            System.out.println("Unrecognised file format type. Please run with -help");
            System.exit(0);
        }

    }

    private void doWork() throws IOException {
        if (!dontScore) {
            passInputForScoreCalculation();
            printOutput();
        }
        if (generateSignatures) {
            AtomSignatureGenerator generator = new AtomSignatureGenerator();
            if (in_file_type == FILE_TYPE.smi) {
                generator.setInputIsSDF(false);
            }
            generator.generateAtomSignatures(inFile.getCanonicalPath(), getSignaturesOutFile().getCanonicalPath());
        }
        if (serialize) {
            new SignatureSerializer().getSerialisedSignatures(fragmentsFile.getCanonicalPath(), getSerializedSignaturesOutFile().getCanonicalPath());
        }

    }

    public void passInputForScoreCalculation() {
        try {
            if (inFile != null) {
                scorer = new NPScoreCalculator();
                if (in_file_type == FILE_TYPE.smi) {
                    scorer.setInputIsSDF(false);
                }
                if (reconstructFragments) {
                    scorer.setReconstructFragments(reconstructFragments);
                    scorer.process(inFile, getOutFile(), getFragmentsOutFile());
                    scoringStarted = true;
                } else {
                    scorer.process(inFile, getOutFile());
                    scoringStarted = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getOutFile() {
        if (outFile != null) {
            if (out_file_type == FILE_TYPE.smi) {
                scorer.setOutputIsSDF(false);
                scorer.setOutputIsJSON(false);
            }else if (out_file_type == FILE_TYPE.json){
                scorer.setOutputIsSMILES(false);
                scorer.setOutputIsSDF(false);
            }else{
                scorer.setOutputIsSMILES(false);
                scorer.setOutputIsJSON(false);
            }
            if (outFile.exists()) {
                return outFile;
            } else {
                outFile.getParentFile().mkdirs();
                return outFile;
            }
        }
        if (inFile != null) {
            String output = inFile.getParent() + File.separator + "Score_" + dateTime;
//            String basename = FilenameUtils.getExtension(String.valueOf(inFile));
//            if (!basename.isEmpty()) {
//                output += "." + basename;
//            }
            outFile = new File(output);
            if (out_file_type == null) {
                if (in_file_type == FILE_TYPE.smi) {
                    scorer.setOutputIsSDF(false);
                }
            } else {
                if (out_file_type == FILE_TYPE.smi) {
                    scorer.setOutputIsSDF(false);
                    scorer.setOutputIsJSON(false);
                }else if(out_file_type == FILE_TYPE.sdf){
                    scorer.setOutputIsSMILES(false);
                    scorer.setOutputIsJSON(false);
                }else{
                    scorer.setOutputIsSMILES(false);
                    scorer.setOutputIsSDF(false);
                }
            }

        }
        return outFile;
    }

    private File getFragmentsOutFile() {
        if (fragmentsFile != null) {
            if (fragmentsFile.exists()) {
                return fragmentsFile;
            } else {
                fragmentsFile.getParentFile().mkdirs();
                return fragmentsFile;
            }
        }
        if (inFile != null) {
            String output = inFile.getParent() + File.separator + "FragmentsWithScores_" + dateTime + ".txt";
            fragmentsFile = new File(output);
        }
        return fragmentsFile;
    }

    private File getSignaturesOutFile() throws IOException {
        if (fragmentsFile != null) {
            if (fragmentsFile.exists()) {
                return fragmentsFile;
            } else {
                fragmentsFile.getParentFile().mkdirs();
                return fragmentsFile;
            }
        }
        if (inFile != null) {
            String output = inFile.getParent() + File.separator + FilenameUtils.getBaseName(inFile.getCanonicalPath()) + "fragments" + dateTime + ".txt";
            fragmentsFile = new File(output);
        }
        return fragmentsFile;
    }

    private File getSerializedSignaturesOutFile() throws IOException {
        if (fragmentsFile != null) {
            String output = fragmentsFile.getParent() + File.separator + FilenameUtils.getBaseName(fragmentsFile.getCanonicalPath()) + "serialized" + ".out";
            serializedFragmentsFile = new File(output);
        }
        return serializedFragmentsFile;
    }

    public void printOutput() {
        if (scoringStarted) {
            System.out.println("** Finished scoring ** " + "\n");
            System.out.println("** " + outFile.toString() + "\n"
                    + "Input molecules from the SDFile are written again with\n"
                    + "natural product likeness score as a property\n");
            if (reconstructFragments) {
                System.out.println("** " + fragmentsFile.toString() + "\n"
                        + "Fragments from the input molecules are written with individual fragment scores.\n"
                        + "Fragments from the same molecule have same UUID. ");
            }
            System.out.println("\n** Note: Molecules that are lost upon curation, if any, are not given any scores and the score property is left empty.");
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Run with -help for usage information");
                System.exit(0);
            }
            long start = System.currentTimeMillis();
            System.out.println(Arrays.asList(args));
            InputParser parser = new InputParser();
            parser.parseUserInput(args);
            long end = System.currentTimeMillis();
            System.out.println("Finished in " + (end - start) + " ms");

        } catch (Exception ex) {
            Logger.getLogger(InputParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
