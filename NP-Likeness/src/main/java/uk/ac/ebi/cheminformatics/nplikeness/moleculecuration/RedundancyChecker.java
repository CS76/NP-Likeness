/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.moleculecuration;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jniinchi.INCHI_OPTION;
import net.sf.jniinchi.INCHI_RET;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

/**
 * Convenient class to check redundancy of molecule using InChI
 *
 * @author kalai
 */
public class RedundancyChecker {

    HashSet<String> inchiHash = null;

    /**
     * Takes list of CDK molecules checks for redundancy and returns
     * non-redundant molecule list
     *
     * @param molecules
     * @return
     * @throws CDKException
     */
    public List<IAtomContainer> removeRedundantStructures(List<IAtomContainer> molecules) throws CDKException {

        inchiHash = new HashSet<String>();
        List<net.sf.jniinchi.INCHI_OPTION> list = new ArrayList<net.sf.jniinchi.INCHI_OPTION>();
        list.add(INCHI_OPTION.SNon);
        list.add(INCHI_OPTION.FixedH);
        InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
        List<IAtomContainer> nonRedundantStructures = new ArrayList<IAtomContainer>();

        for (IAtomContainer molecule : molecules) {
            InChIGenerator gen = factory.getInChIGenerator(molecule, list);
            INCHI_RET ret = gen.getReturnStatus();
            if (ret == INCHI_RET.WARNING) {
                System.out.println("InChI warning: " + gen.getMessage());
            } else if (ret != INCHI_RET.OKAY) {
                throw new CDKException("InChI failed: " + ret.toString()
                        + " [" + gen.getMessage() + "]");
            }
            String inchi = gen.getInchi();
            inchiHash.add(inchi);
        }
        for (String inchi : inchiHash) {

            InChIToStructure intostruct = factory.getInChIToStructure(
                    inchi, DefaultChemObjectBuilder.getInstance());

            INCHI_RET ret = intostruct.getReturnStatus();
            if (ret == INCHI_RET.WARNING) {
                System.out.println("InChI warning: " + intostruct.getMessage());
            } else if (ret != INCHI_RET.OKAY) {
                throw new CDKException("Structure generation failed failed: " + ret.toString()
                        + " [" + intostruct.getMessage() + "]");
            }

            IAtomContainer container = intostruct.getAtomContainer();
            nonRedundantStructures.add(container);
        }
        return nonRedundantStructures;
    }

    public void removeRedundantStructures(String file) throws IOException {

        try {
            IteratingSDFReader reader = null;
            SDFWriter writer = null;
            List<net.sf.jniinchi.INCHI_OPTION> list = new ArrayList<net.sf.jniinchi.INCHI_OPTION>();
            list.add(INCHI_OPTION.SNon);
            list.add(INCHI_OPTION.FixedH);
            InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
            inchiHash = new HashSet<String>();
            File queryFile = new File(file);

            try {
                // TODO code application logic here
                reader = new IteratingSDFReader(new FileReader(queryFile), DefaultChemObjectBuilder.getInstance());
                reader.setSkip(true);
                writer = new SDFWriter(new FileWriter(queryFile.getParent() + File.separator + "_nonredundantSet.sdf"));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(RedundancyChecker.class.getName()).log(Level.SEVERE, null, ex);
            }

            assert reader != null;
            while (reader.hasNext()) {

                IAtomContainer molecule = reader.next();
                InChIGenerator gen = factory.getInChIGenerator(molecule, list);
                INCHI_RET ret = gen.getReturnStatus();
                if (ret == INCHI_RET.WARNING) {
                    System.out.println("InChI warning: " + gen.getMessage());
                } else if (ret != INCHI_RET.OKAY) {
                    throw new CDKException("InChI failed: " + ret.toString()
                            + " [" + gen.getMessage() + "]");
                }
                String inchi = gen.getInchi();
                inchiHash.add(inchi);
            }
            for (String inchi : inchiHash) {

                InChIToStructure intostruct = factory.getInChIToStructure(
                        inchi, DefaultChemObjectBuilder.getInstance());

                INCHI_RET ret = intostruct.getReturnStatus();
                if (ret == INCHI_RET.WARNING) {
                    System.out.println("InChI warning: " + intostruct.getMessage());
                } else if (ret != INCHI_RET.OKAY) {
                    throw new CDKException("Structure generation failed failed: " + ret.toString()
                            + " [" + intostruct.getMessage() + "]");
                }

                IAtomContainer container = intostruct.getAtomContainer();
                writer.write(container);

            }
            assert writer != null;
            writer.close();
            System.out.println("Finished writing non-redundant structures file to : " + queryFile.getParent());
        } catch (CDKException ex) {
            Logger.getLogger(RedundancyChecker.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid input");
            System.exit(0);
        }
        try {
            new RedundancyChecker().removeRedundantStructures(args[0]);
        } catch (IOException ex) {
            Logger.getLogger(RedundancyChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
