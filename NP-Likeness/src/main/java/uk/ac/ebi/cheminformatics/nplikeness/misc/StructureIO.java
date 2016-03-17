/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.misc;


import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kalai
 */
public class StructureIO {

    //static final SmilesGenerator smilesGenerator = SmilesGenerator.absolute().aromatic();

    public static SDFWriter createSDFWriter(String fileToWrite) throws IOException {
        return new SDFWriter(new FileWriter(new File(fileToWrite)));
    }

    public static void writeSDF(SDFWriter writer, List<IAtomContainer> molecules) throws Exception {
        for (IAtomContainer mol : molecules) {
            writer.write(mol);
        }
        writer.close();
    }

    public static List<IAtomContainer> readSDF(String inputSDFile) throws Exception {
        List<IAtomContainer> molecules = new ArrayList<IAtomContainer>();
        IteratingSDFReader sdfReader = new IteratingSDFReader(new FileReader(new File(inputSDFile)), SilentChemObjectBuilder.getInstance());
        while (sdfReader.hasNext()) {
            molecules.add(sdfReader.next());
        }
        return molecules;
    }

    public static IAtomContainer readMol(String inputSDFile) throws Exception {
        MDLReader mdlReader = new MDLReader(new FileReader(new File(inputSDFile)));
        return mdlReader.read(new AtomContainer());
    }


//    public static String createSmiles(IAtomContainer molecule) {
//        return smilesGenerator.createSMILES(molecule);
//    }


}
