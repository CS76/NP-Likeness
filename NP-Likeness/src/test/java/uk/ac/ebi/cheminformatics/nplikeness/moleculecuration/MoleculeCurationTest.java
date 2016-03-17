/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.moleculecuration;

import java.net.URL;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.templates.MoleculeFactory;

/**
 *
 * @author kalai
 */
public class MoleculeCurationTest {

    public MoleculeCurationTest() {
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
     * Test of curateMolecule method, of class MoleculeCuration.
     */
    @Test
    public void testCurateMolecule() throws Exception {
       
        System.out.println("curateMolecule");
        URL url = RedundancyCheckerTest.class.getResource("/data/glycoside2.mol");
        String fileName = url.getFile();
        IAtomContainer molecule =  MoleculeFactory.loadMolecule(fileName);
        MoleculeCuration instance = new MoleculeCuration();
        int expResult = 2;
        List result = instance.curateMolecule(molecule);
        assertEquals(expResult, result.size());
    }
}
