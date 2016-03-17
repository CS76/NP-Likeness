/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.moleculecuration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;

import static org.junit.Assert.*;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.templates.MoleculeFactory;

/**
 * @author kalai
 */
public class SugarRemoverTest {

    public SugarRemoverTest() {
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
     * Test of removeSugars method, of class RemoveSugars.
     */
    @Test
    public void testRemoveSugars() {


        System.out.println("removeSugars");
        SugarRemover instance = new SugarRemover();
        URL url = RedundancyCheckerTest.class.getResource("/data/glycoside2.mol");
        String fileName = url.getFile();
        IAtomContainer sugarMol = MoleculeFactory.loadMolecule(fileName);
        List<IAtomContainer> moleculeList = new ArrayList<IAtomContainer>();
        moleculeList.add(sugarMol);

        int initialRings = 6;
        SSSRFinder molecule_ring = new SSSRFinder(sugarMol);
        IRingSet ringset = molecule_ring.findSSSR();
        assertEquals(initialRings, ringset.getAtomContainerCount());


        List<IAtomContainer> result = instance.removeSugars(moleculeList);
        assertEquals(2, result.size());

        IAtomContainer molecule = result.get(0);
        molecule_ring = new SSSRFinder(molecule);
        ringset = molecule_ring.findSSSR();
        assertEquals(1, ringset.getAtomContainerCount());

        molecule = result.get(1);
        molecule_ring = new SSSRFinder(molecule);
        ringset = molecule_ring.findSSSR();
        assertEquals(1, ringset.getAtomContainerCount());
    }
}
