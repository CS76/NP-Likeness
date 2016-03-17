/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.scorer;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.*;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.templates.MoleculeFactory;

/**
 *
 * @author kalai
 */
public class AtomSignatureGeneratorTest {

    public AtomSignatureGeneratorTest() {
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
     * Test of generateAtomSignatures method, of class AtomSignatureGenerator.
     */
    @Test
    public void testGenerateAtomSignatures_List() throws Exception {
        System.out.println("generateAtomSignatures");
        IAtomContainer molecule =  MoleculeFactory.makeAlphaPinene();
        IAtomContainer molecule2 = MoleculeFactory.makeAzulene();
        List<IAtomContainer> moleculeList = new ArrayList<IAtomContainer>();
        moleculeList.add(molecule);
        moleculeList.add(molecule2);
        AtomSignatureGenerator instance = new AtomSignatureGenerator();
        int expResult = 20;
        String signature5 = "[C]([C]([C,0])[C]([C]=[C])[C]([C][C][C,0]))";
        List<String> result = instance.generateAtomSignatures(moleculeList);
        String[] uuidSignature = result.get(5).split("\\|");
        assertEquals(expResult, result.size());
        assertEquals(signature5, uuidSignature[1]);
        
    }

    /**
     * Test of generateAtomSignatures method, of class AtomSignatureGenerator.
     */
    @Test
    public void testGenerateAtomSignatures_AtomContainer() throws Exception {
        System.out.println("generateAtomSignatures");
        IAtomContainer molecule =  MoleculeFactory.makeAlphaPinene();
        AtomSignatureGenerator instance = new AtomSignatureGenerator();
        int expResult = 10;
        String signature0 = "[C]([C]=[C]([C])[C]([C][C]))";
        List<String> result = instance.generateAtomSignatures(molecule);
        String[] uuidSignature = result.get(0).split("\\|");
        assertEquals(expResult, result.size());
        assertEquals(signature0, uuidSignature[1]);
    }

}
