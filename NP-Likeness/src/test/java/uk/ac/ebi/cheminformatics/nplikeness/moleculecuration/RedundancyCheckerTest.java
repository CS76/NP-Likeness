/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.moleculecuration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;

import static org.junit.Assert.*;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.templates.MoleculeFactory;

/**
 * @author kalai
 */
public class RedundancyCheckerTest {

    public RedundancyCheckerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of removeRedundantStructures method, of class RedundancyChecker.
     */
    @Test
    public void testRemoveRedundantStructures_List() throws Exception {
        IAtomContainer molecule = MoleculeFactory.makeAlphaPinene();

        System.out.println("removeRedundantStructures");
        List<IAtomContainer> molecules = new ArrayList<IAtomContainer>();
        molecules.add(molecule);
        molecules.add(molecule);
        RedundancyChecker instance = new RedundancyChecker();
        int expResult = 1;
        List result = instance.removeRedundantStructures(molecules);
        assertEquals(expResult, result.size());

    }

    /**
     * Test of removeRedundantStructures method, of class RedundancyChecker.
     */
//    @Test
    public void testRemoveRedundantStructures_String() throws Exception {
        System.out.println("removeRedundantStructures");
        URL url = RedundancyCheckerTest.class.getResource("/data/alphaPinene.sdf");
        String fileName = url.getFile();
        RedundancyChecker instance = new RedundancyChecker();
        instance.removeRedundantStructures(fileName);
        File resultfile = new File(fileName);
        String filename = resultfile.getParent() + File.separator + "_nonredundantSet.sdf";
        IAtomContainer loadresultMolecule = MoleculeFactory.loadMolecule(filename);
        assertEquals(10, loadresultMolecule.getAtomCount());

    }
//
}
