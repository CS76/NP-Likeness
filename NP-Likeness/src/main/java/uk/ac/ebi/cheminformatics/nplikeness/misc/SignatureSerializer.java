/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.misc;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility method to serialize signatures generated using AtomSignatureGenerator
 * class into a HashMap. The serialized file generated using this class then be
 * used as a training set for faster calculation of scores. SignatureTable class
 * can handle files that are produced using the methods in this class, for
 * example user dependant dataset of natural and synthetic like molecules.
 *
 * @author kalai
 */
public class SignatureSerializer implements Serializable {

    Map<String, HashSet<String>> mapSignatures = null;

    Map<String, Double> wrongMap = new HashMap<String, Double>();
    Map<String, Double> signature_Occurence_Map = null;
    HashSet<String> numberOfMolecules = null;
    int moleculeCount = 0;

    /**
     * Takes String input of .txt signatures file and output .out file location
     * and writes hashmap containing key-value of "signature-total occurrence
     * count"
     *
     * @param in
     * @param out
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public void getSerialisedSignatures(String in, String out) throws IOException {


        FileOutputStream fos = new FileOutputStream(out);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        LineNumberReader fileReader = new LineNumberReader(new BufferedReader(new FileReader(in)));
        String line;
        mapSignatures = new HashMap<String, HashSet<String>>();
        numberOfMolecules = new HashSet<String>();
        String previous_uuid = "";
        while ((line = fileReader.readLine()) != null) {
            String[] uuid_signature = line.split("\\|");
            if (uuid_signature.length == 2) {
                HashSet<String> set = null;

                String uuid = uuid_signature[0];
                String signature = uuid_signature[1];
                numberOfMolecules.add(uuid);
                //  System.out.println(numberOfMolecules.size());
                if (!mapSignatures.containsKey(signature)) {
                    set = new HashSet<String>();
                    set.add(uuid);
                    mapSignatures.put(signature, set);
         //           wrongMap.put(signature, 1.0);
                } else {
                    set = mapSignatures.get(signature);
                    set.add(uuid);
//                    Double count = wrongMap.get(signature);
//                    wrongMap.put(signature, count + 1.0);
                }
            }

        }
        System.out.println("Number of keys = " + mapSignatures.keySet().size());

        rewrite(mapSignatures);
        System.out.println("Number of keys after= " + signature_Occurence_Map.keySet().size());

        moleculeCount = numberOfMolecules.size();
        oos.writeObject(signature_Occurence_Map);
        oos.writeObject(moleculeCount);
        oos.close();

        fileReader.close();
        System.out.println("finished serializing the input signatures");

    }

    private void rewrite(Map<String, HashSet<String>> mapSignatures) {
        signature_Occurence_Map = new HashMap<String, Double>();
        for (String key : mapSignatures.keySet()) {
            Set<String> uuids = mapSignatures.get(key);
          //  System.out.println(key + " - " + uuids.size() );
            signature_Occurence_Map.put(key, new Double(uuids.size()));
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid input arguments");
            System.exit(0);
        }
        try {
            new SignatureSerializer().getSerialisedSignatures(args[0], args[1]);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SignatureSerializer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SignatureSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}