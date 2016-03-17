/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.cheminformatics.nplikeness.scorer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static org.junit.Assert.assertEquals;
import org.junit.*;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * Test class for the NPScoreCalculator
 * @author kalai
 */
public class NPScoreCalculatorTest {

    public NPScoreCalculatorTest() {
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
     * Test of curateMoleculeAndCalculateScore method, of class
     * NPScoreCalculator.
     */
    @Test
    public void testCurateMoleculeAndCalculateScore_IMolecule() throws Exception {
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer molecule = sp.parseSmiles("CC1=CCC2CC1C2(C)(C)");
        NPScoreCalculator instance = new NPScoreCalculator();
        String expResult = "01.2910";
        String result = instance.curateAndScore(molecule);
        String[] uuid_score = result.split("\\|");
        assertEquals(expResult, uuid_score[1]);

    }

    /**
     * Test of curateMoleculeAndCalculateScore method, of class
     * NPScoreCalculator.
     */
    @Test
    public void testCurateMoleculeAndCalculateScore_IMolecule_boolean() throws Exception {
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer molecule =  sp.parseSmiles("CC1=CCC2CC1C2(C)(C)");
         NPScoreCalculator instance = new NPScoreCalculator();
        String expResult = "01.2910";
        int fragmentsSize = 0;
        String[] uuid_score = new String[2];
        Map<String, List<IAtomContainer>> resultMap = instance.curateScoreAndReconstruct(molecule);
        for (Entry e : resultMap.entrySet()) {
            String result = (String) e.getKey();
            uuid_score = result.split("\\|");
            List<IAtomContainer> fragments = (List<IAtomContainer>) e.getValue();
            fragmentsSize = fragments.size();
        }
        
        assertEquals(expResult, uuid_score[1]);
        assertEquals(fragmentsSize, 10);
    }
}
