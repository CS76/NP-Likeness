/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.moleculecuration;

import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.Atom;
import org.openscience.cdk.templates.MoleculeFactory;

/**
 *
 * @author kalai
 */
public class MoleculeConnectivityCheckerTest {
    
    public MoleculeConnectivityCheckerTest() {
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
     * Test of checkConnectivity method, of class MoleculeConnectivityChecker.
     */
    @Test
    public void testCheckConnectivity() {
        System.out.println("checkConnectivity");

        IAtomContainer molecule = (IAtomContainer) MoleculeFactory.makeAlphaPinene();
        IAtom metalAtom = new Atom("Au");
        molecule.addAtom(metalAtom);
        
        MoleculeConnectivityChecker instance = new MoleculeConnectivityChecker();
        int expResult = 2;
        List result = instance.checkConnectivity(molecule);
        assertEquals(expResult, result.size());
    }
}
