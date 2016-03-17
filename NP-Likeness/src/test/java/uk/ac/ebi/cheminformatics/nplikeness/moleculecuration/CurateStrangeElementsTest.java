/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.moleculecuration;

import java.util.ArrayList;
import java.util.List;

import org.junit.*;

import static org.junit.Assert.*;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.Atom;
import org.openscience.cdk.templates.MoleculeFactory;

/**
 * @author kalai
 */
public class CurateStrangeElementsTest {

    public CurateStrangeElementsTest() {
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
     * Test of removeStrangeElements method, of class CurateStrangeElements.
     */
    @Test
    public void testRemoveStrangeElements() {
        System.out.println("removeStrangeElements");
        IAtomContainer molecule = (IAtomContainer) MoleculeFactory.makeAlphaPinene();
        IAtom metalAtom = new Atom("Au");
        molecule.addAtom(metalAtom);
        List<IAtomContainer> moleculeList = new ArrayList<IAtomContainer>();
        moleculeList.add(molecule);
        StrangeElementsCurator instance = new StrangeElementsCurator();
        boolean containsSelenium = false;
        int moleculeCount = 0;
        assertEquals(!containsSelenium, molecule.contains(metalAtom));
        List result = instance.removeStrangeElements(moleculeList);
        System.out.println("The molecule contains metal, so the molecule is lost");
        assertEquals(moleculeCount, result.size());

    }
}
