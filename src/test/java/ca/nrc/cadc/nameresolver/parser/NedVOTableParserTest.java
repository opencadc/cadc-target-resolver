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
public class NedVOTableParserTest extends AbstractParserTest
{
    private static final Logger log = Logger.getLogger(NedVOTableParserTest.class);
    
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        Log4jInit.setLevel("ca.nrc.cadc.nameresolver", Level.INFO);
        Log4jInit.setLevel("ca.nrc.cadc.dali", Level.INFO);
    }
    
    @Override
    protected String getTarget()
    {
        return "m31";
    }
    
    @Override
    protected String getHost()
    {
        return "ned.ipac.caltech.edu";
    }
    
    @Override
    protected String getDatabase()
    {
        return "NED";
    }

    @Test
    public void testTargetNotFound()
    {
        try
        {
            String results = getTestFile("NED_VOTable_target_not_found.xml");
            
            Parser parser = new NedVOTableParser(getTarget(), getHost(), results);
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
    public void testInvalidVOTable()
    {
        try
        {
            String results = getTestFile("NED_VOTable_invalid.xml");
            
            Parser parser = new NedVOTableParser(getTarget(), getHost(), results);
            assertNotNull(parser);
            try
            {
                TargetData data = parser.parse();
                fail("Invalid VOTable should throw TargetDataParsingException");
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
            String results = getTestFile("NED_VOTable_m31.xml");
            
            Parser parser = new NedVOTableParser(getTarget(), getHost(), results);
            assertNotNull(parser);
            
            TargetData data = parser.parse();
            assertNotNull(data);
            
            assertEquals("Target did not match", getTarget(), data.getTarget());
            assertEquals("Host did not match", getHost(), data.getHost());
            assertEquals("Coordsys did not match", getCoordsys(), data.getCoordsys());
            assertEquals("Database did not match", getDatabase(), data.getDatabase());
            assertEquals("RA did not match", 10.68479D, data.getRA(), 0.0);
            assertEquals("Dec did not match", 41.26906D, data.getDEC(), 0.0);
            assertEquals("Object name did not match", "MESSIER 031", data.getObjectName());
            assertEquals("Object type did not match", "G", data.getObjectType());
            assertNull("Morphology type should be null", data.getMorphologyType());
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testValidTarget_NGC_6341()
    {
        try
        {
            String results = getTestFile("NED_VOTable_NGC_6341.xml");
            
            Parser parser = new NedVOTableParser(getTarget(), getHost(), results);
            assertNotNull(parser);
            
            TargetData data = parser.parse();
            assertNotNull(data);
            
            assertEquals("Target did not match", getTarget(), data.getTarget());
            assertEquals("Host did not match", getHost(), data.getHost());
            assertEquals("Coordsys did not match", getCoordsys(), data.getCoordsys());
            assertEquals("Database did not match", getDatabase(), data.getDatabase());
            assertEquals("RA did not match", 259.28029D, data.getRA(), 0.0);
            assertEquals("Dec did not match", 43.13652D, data.getDEC(), 0.0);
            assertEquals("Object name did not match", "MESSIER 092", data.getObjectName());
            assertEquals("Object type did not match", "*Cl", data.getObjectType());
            assertNull("Morphology type should be null", data.getMorphologyType());
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testValidTarget_NGC_7293()
    {
        try
        {
            String results = getTestFile("NED_VOTable_NGC_7293.xml");
            
            Parser parser = new NedVOTableParser(getTarget(), getHost(), results);
            assertNotNull(parser);
            
            TargetData data = parser.parse();
            assertNotNull(data);
            
            assertEquals("Target did not match", getTarget(), data.getTarget());
            assertEquals("Host did not match", getHost(), data.getHost());
            assertEquals("Coordsys did not match", getCoordsys(), data.getCoordsys());
            assertEquals("Database did not match", getDatabase(), data.getDatabase());
            assertEquals("RA did not match", 337.41071D, data.getRA(), 0.0);
            assertEquals("Dec did not match", -20.83734D, data.getDEC(), 0.0);
            assertEquals("Object name did not match", "NGC 7293", data.getObjectName());
            assertEquals("Object type did not match", "PN", data.getObjectType());
            assertNull("Morphology type should be null", data.getMorphologyType());
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            fail("unexpected exception: " + unexpected);
        }
    }
    
}
