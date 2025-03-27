/*
 ************************************************************************
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 *
 * (c) 2014.                            (c) 2014.
 * National Research Council            Conseil national de recherches
 * Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 * All rights reserved                  Tous droits reserves
 *
 * NRC disclaims any warranties         Le CNRC denie toute garantie
 * expressed, implied, or statu-        enoncee, implicite ou legale,
 * tory, of any kind with respect       de quelque nature que se soit,
 * to the software, including           concernant le logiciel, y com-
 * without limitation any war-          pris sans restriction toute
 * ranty of merchantability or          garantie de valeur marchande
 * fitness for a particular pur-        ou de pertinence pour un usage
 * pose.  NRC shall not be liable       particulier.  Le CNRC ne
 * in any event for any damages,        pourra en aucun cas etre tenu
 * whether direct or indirect,          responsable de tout dommage,
 * special or general, consequen-       direct ou indirect, particul-
 * tial or incidental, arising          ier ou general, accessoire ou
 * from the use of the software.        fortuit, resultant de l'utili-
 *                                      sation du logiciel.
 *
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 ************************************************************************
 */

package ca.nrc.cadc.nameresolver.parser;

import ca.nrc.cadc.nameresolver.Parser;
import ca.nrc.cadc.nameresolver.TargetData;
import ca.nrc.cadc.nameresolver.exception.TargetDataParsingException;
import ca.nrc.cadc.util.Log4jInit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 *
 * @author jburke
 */
public class SimbadParserTest extends AbstractParserTest
{
    private static final Logger log = Logger.getLogger(SimbadParserTest.class);
    
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        Log4jInit.setLevel("ca.nrc.cadc.nameresolver", Level.INFO);
    }
    
    @Override
    protected String getTarget()
    {
        return "m31";
    }
    
    @Override
    protected String getHost()
    {
        return "simbad.u-strasbg.fr";
    }
    
    @Override
    protected String getDatabase()
    {
        return "Simbad";
    }

    @Test
    public void testTargetNotFound()
    {
        try
        {
            String results = getTestFile("Simbad_target_not_found.txt");
            
            Parser parser = new SimbadParser(getTarget(), getHost(), results);
            assertNotNull(parser);
            
            TargetData data = parser.parse();
            assertNull(data);
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testError()
    {
        try
        {
            String results = getTestFile("Simbad_error.txt");
            
            Parser parser = new SimbadParser(getTarget(), getHost(), results);
            assertNotNull(parser);
            try
            {
                TargetData data = parser.parse();
                fail("Error message should throw TargetDataParsingException");
            }
            catch (TargetDataParsingException e)
            { }
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testValidTarget_M31()
    {
        try
        {
            String results = getTestFile("Simbad_m31.txt");

            Parser parser = new SimbadParser(getTarget(), getHost(), results);
            assertNotNull(parser);

            TargetData data = parser.parse();
            assertNotNull(data);

            assertEquals("Target did not match", getTarget(), data.getTarget());
            assertEquals("Host did not match", getHost(), data.getHost());
            assertEquals("Coordsys did not match", getCoordsys(), data.getCoordsys());
            assertEquals("Database did not match", getDatabase(), data.getDatabase());
            assertEquals("RA did not match", 10.684708D, data.getRA(), 0.0);
            assertEquals("Dec did not match", 41.268750D, data.getDEC(), 0.0);
            assertEquals("Object name did not match", "M 31", data.getObjectName());
            assertEquals("Object type did not match", "G", data.getObjectType());
            assertEquals("Morphology type should be null", "SA(s)b", data.getMorphologyType());
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testValidTarget_HD_3145()
    {
        try
        {
            String results = getTestFile("Simbad_hd_3145.txt");
            
            Parser parser = new SimbadParser(getTarget(), getHost(), results);
            assertNotNull(parser);
            
            TargetData data = parser.parse();
            assertNotNull(data);
            
            assertEquals("Target did not match", getTarget(), data.getTarget());
            assertEquals("Host did not match", getHost(), data.getHost());
            assertEquals("Coordsys did not match", getCoordsys(), data.getCoordsys());
            assertEquals("Database did not match", getDatabase(), data.getDatabase());
            assertEquals("RA did not match", 120.56640246D, data.getRA(), 0.0);
            assertEquals("Dec did not match", 02.33457005D, data.getDEC(), 0.0);
            assertEquals("Object name did not match", "HR 3145", data.getObjectName());
            assertEquals("Object type did not match", "*i*", data.getObjectType());
            assertNull("Morphology type should be null", data.getMorphologyType());
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testValidTarget_zeta_auriga()
    {
        try
        {
            String results = getTestFile("Simbad_zeta_auriga.txt");
            
            Parser parser = new SimbadParser(getTarget(), getHost(), results);
            assertNotNull(parser);
            
            TargetData data = parser.parse();
            assertNotNull(data);
            
            assertEquals("Target did not match", getTarget(), data.getTarget());
            assertEquals("Host did not match", getHost(), data.getHost());
            assertEquals("Coordsys did not match", getCoordsys(), data.getCoordsys());
            assertEquals("Database did not match", getDatabase(), data.getDatabase());
            assertEquals("RA did not match", 075.61953078D, data.getRA(), 0.0);
            assertEquals("Dec did not match", 41.07583888D, data.getDEC(), 0.0);
            assertEquals("Object name did not match", "V* zet Aur", data.getObjectName());
            assertEquals("Object type did not match", "Al*", data.getObjectType());
            assertNull("Morphology type should be null", data.getMorphologyType());
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            fail("unexpected exception: " + unexpected);
        }
    }

}
