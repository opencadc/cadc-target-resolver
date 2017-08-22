/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2017.                            (c) 2017.
 *  Government of Canada                 Gouvernement du Canada
 *  National Research Council            Conseil national de recherches
 *  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 *  All rights reserved                  Tous droits réservés
 *
 *  NRC disclaims any warranties,        Le CNRC dénie toute garantie
 *  expressed, implied, or               énoncée, implicite ou légale,
 *  statutory, of any kind with          de quelque nature que ce
 *  respect to the software,             soit, concernant le logiciel,
 *  including without limitation         y compris sans restriction
 *  any warranty of merchantability      toute garantie de valeur
 *  or fitness for a particular          marchande ou de pertinence
 *  purpose. NRC shall not be            pour un usage particulier.
 *  liable in any event for any          Le CNRC ne pourra en aucun cas
 *  damages, whether direct or           être tenu responsable de tout
 *  indirect, special or general,        dommage, direct ou indirect,
 *  consequential or incidental,         particulier ou général,
 *  arising from the use of the          accessoire ou fortuit, résultant
 *  software.  Neither the name          de l'utilisation du logiciel. Ni
 *  of the National Research             le nom du Conseil National de
 *  Council of Canada nor the            Recherches du Canada ni les noms
 *  names of its contributors may        de ses  participants ne peuvent
 *  be used to endorse or promote        être utilisés pour approuver ou
 *  products derived from this           promouvoir les produits dérivés
 *  software without specific prior      de ce logiciel sans autorisation
 *  written permission.                  préalable et particulière
 *                                       par écrit.
 *
 *  This file is part of the             Ce fichier fait partie du projet
 *  OpenCADC project.                    OpenCADC.
 *
 *  OpenCADC is free software:           OpenCADC est un logiciel libre ;
 *  you can redistribute it and/or       vous pouvez le redistribuer ou le
 *  modify it under the terms of         modifier suivant les termes de
 *  the GNU Affero General Public        la “GNU Affero General Public
 *  License as published by the          License” telle que publiée
 *  Free Software Foundation,            par la Free Software Foundation
 *  either version 3 of the              : soit la version 3 de cette
 *  License, or (at your option)         licence, soit (à votre gré)
 *  any later version.                   toute version ultérieure.
 *
 *  OpenCADC is distributed in the       OpenCADC est distribué
 *  hope that it will be useful,         dans l’espoir qu’il vous
 *  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
 *  without even the implied             GARANTIE : sans même la garantie
 *  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
 *  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
 *  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
 *  General Public License for           Générale Publique GNU Affero
 *  more details.                        pour plus de détails.
 *
 *  You should have received             Vous devriez avoir reçu une
 *  a copy of the GNU Affero             copie de la Licence Générale
 *  General Public License along         Publique GNU Affero avec
 *  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
 *  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
 *                                       <http://www.gnu.org/licenses/>.
 *
 *
 ************************************************************************
 */

package ca.nrc.cadc.nameresolver.integration;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.Log4jInit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * @author jburke
 */
public class NRServletTest
{
    private static final Logger log = Logger.getLogger(NRServletTest.class);

    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String TEXT_XML = "text/xml";
    private static final String TARGET = "target";
    private static final String COORDSYS = "coordsys";
    private static final String RA = "ra";
    private static final String DEC = "dec";
    private static final String TIME_MS = "time(ms)";
    private static final String ONAME = "oname";
    private static final String OTYPE = "otype";
    private static final String MTYPE = "mtype";
    private static final String ICRS = "ICRS";
    private final RegistryClient registryClient = new RegistryClient();

    public NRServletTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        Log4jInit.setLevel("ca.nrc.cadc.nameresolver", Level.INFO);
    }

    private HttpURLConnection openConnection(String query) throws Exception
    {
        URL url = registryClient.getServiceURL(URI.create("ivo://cadc.nrc.ca/resolver"), Standards.RESOLVER_10,
                                               AuthMethod.ANON);
        if (query != null)
        {
            url = new URL(url.toExternalForm() + "?" + query);
            log.debug("query url: " + url.toExternalForm());
        }
        return (HttpURLConnection) url.openConnection();
    }

    private String getContent(HttpURLConnection conn) throws Exception
    {
        byte[] buffer = new byte[4096];
        int bytesRead;
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // read until finished
        while ((bytesRead = in.read(buffer)) > 0)
        {
            out.write(buffer, 0, bytesRead);
        }
        final String content = out.toString("UTF-8");
        log.debug("content:\n" + content);
        return content;
    }

    private Map<String, String> getContentMap(HttpURLConnection conn) throws Exception
    {
        Map<String, String> map = new HashMap<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = in.readLine()) != null)
        {
            if (line.trim().isEmpty())
            {
                continue;
            }
            String[] tokens = line.split("=");
            switch (tokens.length)
            {
                case 1:
                    map.put(tokens[0], "");
                    break;
                case 2:
                    map.put(tokens[0], tokens[1]);
                    break;
                case 3:
                    map.put(tokens[0], tokens[1] + "=" + tokens[2]); // special case for Vizier
                    break;
                default:
                    throw new IllegalStateException("Unable to parse line: " + line);
            }
        }
        in.close();
        return map;
    }

    @Test
    public void testBadRequest() throws Exception
    {
        HttpURLConnection conn = openConnection("");
        conn.setRequestMethod("GET");
        assertEquals("wrong response code", 425, conn.getResponseCode());
    }

    @Test
    public void testAsciiResults() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateASCII("target=m31", target, ra, dec, null, null, null, false);
        validateASCII("target=m31&detail=min", target, ra, dec, null, null, null, false);
        validateASCII("target=m31&detail=max", target, ra, dec, oname, otype, mtype, true);
    }

    @Test
    public void testAsciiResultsAll() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateASCII("target=m31&service=all", target, ra, dec, null, null, null, false);
        validateASCII("target=m31&service=all&detail=min", target, ra, dec, null, null, null, false);
        validateASCII("target=m31&service=all&detail=max", target, ra, dec, oname, otype, mtype, true);
    }

    @Test
    public void testAsciiResultsNED() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateASCII("target=m31&service=ned", target, ra, dec, null, null, null, false);
        validateASCII("target=m31&service=ned&detail=min", target, ra, dec, null, null, null, false);
        validateASCII("target=m31&service=ned&detail=max", target, ra, dec, oname, otype, mtype, true);
    }

    @Test
    public void testAsciiResultsNEDWithRadius() throws Exception
    {
        final String target = "m31 0.5";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateASCII("target=" + NetUtil.encode(target) + "&service=ned", "m31", ra, dec, null, null, null, false);
        validateASCII("target=" + NetUtil
                .encode(target) + "&service=ned&detail=min", "m31", ra, dec, null, null, null, false);
        validateASCII("target=" + NetUtil
                .encode(target) + "&service=ned&detail=max", "m31", ra, dec, oname, otype, mtype, true);
    }

    @Test
    public void testAsciiResultsSimbad() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "M 31";
        final String otype = "G";
        final String mtype = "SA(s)b";

        validateASCII("target=m31&service=simbad", target, ra, dec, null, null, null, false);
        validateASCII("target=m31&service=simbad&detail=min", target, ra, dec, null, null, null, false);
        validateASCII("target=m31&service=simbad&detail=max", target, ra, dec, oname, otype, mtype, true);
    }

    @Test
    public void testAsciiResultsSimbadWithRadius() throws Exception
    {
        final String target = "m31, 1.0";
        final String expected = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "M 31";
        final String otype = "G";
        final String mtype = "SA(s)b";

        validateASCII("target=" + NetUtil
                .encode(target) + "&service=simbad", expected, ra, dec, null, null, null, false);
        validateASCII("target=" + NetUtil
                .encode(target) + "&service=simbad&detail=min", expected, ra, dec, null, null, null, false);
        validateASCII("target=" + NetUtil
                .encode(target) + "&service=simbad&detail=max", expected, ra, dec, oname, otype, mtype, true);
    }

    @Test
    @Ignore("Vizier is extremely unpredictable.")
    public void testAsciiResultsVizier() throws Exception
    {
        final String target = "NGC 4321";
        final double ra = 185.74d;
        final double dec = 15.83d;
        final String oname = null;
        final String otype = null;
        final String mtype = null;

        validateASCII("target=" + NetUtil.encode(target) + "&service=vizier", target, ra, dec, null, null, null, false);
        validateASCII("target=" + NetUtil
                .encode(target) + "&service=vizier&detail=min", target, ra, dec, null, null, null, false);
        validateASCII("target=" + NetUtil
                .encode(target) + "&service=vizier&detail=max", target, ra, dec, oname, otype, mtype, true);
    }

    @Test
    public void testJSONResults() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateJSON("target=m31&format=json", target, ra, dec, null, null, null,
                     false);
        validateJSON("target=m31&detail=min&format=json", target, ra, dec, null, null, null,
                     false);
        validateJSON("target=m31&detail=max&format=json", target, ra, dec, oname, otype, mtype, true);
    }

    @Test
    public void testJSONResultsAll() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateJSON("target=m31&format=json&service=all", target, ra, dec, null, null, null,
                     false);
        validateJSON("target=m31&detail=min&format=json&service=all", target, ra, dec, null, null,
                     null, false);
        validateJSON("target=m31&detail=max&format=json&service=all", target, ra, dec, oname, otype, mtype,
                     true);
    }

    @Test
    public void testJSONResultsNED() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateJSON("target=m31&format=json&service=ned", target, ra, dec, null, null, null,
                     false);
        validateJSON("target=m31&detail=min&format=json&service=ned", target, ra, dec, null, null,
                     null, false);
        validateJSON("target=m31&detail=max&format=json&service=ned", target, ra, dec, oname, otype, mtype,
                     true);
    }

    @Test
    public void testJSONResultsSimbad() throws Exception
    {
        final String target = "m31";
        final double ra = 10.684708d;
        final double dec = 41.26875d;
        final String oname = "M 31";
        final String otype = "G";
        final String mtype = "SA(s)b";

        validateJSON("target=m31&format=json&service=simbad", target, ra, dec, null, null, null, false);
        validateJSON("target=m31&format=json&service=simbad&detail=min", target, ra, dec, null, null, null, false);
        validateJSON("target=m31&format=json&service=simbad&detail=max", target, ra, dec, oname, otype, mtype, true);
    }

    @Test
    @Ignore("Vizier is extremely unpredictable.")
    public void testJSONResultsVizier() throws Exception
    {
        final String target = "NGC 4321";
        final double ra = 185.74d;
        final double dec = 15.83d;
        final String oname = null;
        final String otype = null;
        final String mtype = null;

        validateJSON("target=" + NetUtil.encode(target) + "&format=json&service=vizier", target, ra, dec, null,
                     null, null, false);
        validateJSON("target=" + NetUtil.encode(target) + "&format=json&service=vizier&detail=min", target, ra, dec,
                     null, null, null, false);
        validateJSON("target=" + NetUtil
                             .encode(target) + "&format=json&service=vizier&detail=max", target, ra, dec, oname,
                     otype, mtype, true);
    }

    @Test
    public void testXMLResults() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateXML("target=m31&format=xml", target, ra, dec, null, null, null,
                     false);
        validateXML("target=m31&detail=min&format=xml", target, ra, dec, null, null, null,
                     false);
        validateXML("target=m31&detail=max&format=xml", target, ra, dec, oname, otype, mtype, true);
    }

    @Test
    public void testXMLResultsAll() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateXML("target=m31&format=xml&service=all", target, ra, dec, null, null, null,
                     false);
        validateXML("target=m31&detail=min&format=xml&service=all", target, ra, dec, null, null,
                     null, false);
        validateXML("target=m31&detail=max&format=xml&service=all", target, ra, dec, oname, otype, mtype,
                     true);
    }

    @Test
    public void testXMLResultsNED() throws Exception
    {
        final String target = "m31";
        final double ra = 10.68479d;
        final double dec = 41.26906d;
        final String oname = "MESSIER 031";
        final String otype = "G";
        final String mtype = null;

        validateXML("target=m31&format=xml&service=ned", target, ra, dec, null, null, null,
                     false);
        validateXML("target=m31&detail=min&format=xml&service=ned", target, ra, dec, null, null,
                     null, false);
        validateXML("target=m31&detail=max&format=xml&service=ned", target, ra, dec, oname, otype, mtype,
                     true);
    }

    @Test
    public void testXMLResultsSimbad() throws Exception
    {
        final String target = "m31";
        final double ra = 10.684708d;
        final double dec = 41.26875d;
        final String oname = "M 31";
        final String otype = "G";
        final String mtype = "SA(s)b";

        validateXML("target=m31&format=xml&service=simbad", target, ra, dec, null, null, null, false);
        validateXML("target=m31&format=xml&service=simbad&detail=min", target, ra, dec, null, null, null, false);
        validateXML("target=m31&format=xml&service=simbad&detail=max", target, ra, dec, oname, otype, mtype, true);
    }

    @Test
    @Ignore("Vizier is extremely unpredictable.")
    public void testXMLResultsVizier() throws Exception
    {
        final String target = "NGC 4321";
        final double ra = 185.74d;
        final double dec = 15.83d;
        final String oname = null;
        final String otype = null;
        final String mtype = null;

        validateXML("target=" + NetUtil.encode(target) + "&format=xml&service=vizier", target, ra, dec, null,
                     null, null, false);
        validateXML("target=" + NetUtil.encode(target) + "&format=xml&service=vizier&detail=min", target, ra, dec,
                     null, null, null, false);
        validateXML("target=" + NetUtil
                             .encode(target) + "&format=xml&service=vizier&detail=max", target, ra, dec, oname,
                     otype, mtype, true);
    }

    private void validateASCII(final String query, final String target, final double ra, final double dec,
                               final String oname, final String otype, final String mtype, final boolean maxDetail)
            throws Exception
    {
        HttpURLConnection conn = openConnection(query);
        conn.setRequestMethod("GET");
        assertEquals("wrong response code", 200, conn.getResponseCode());
        assertTrue("wrong content type", conn.getHeaderField("content-type").contains(TEXT_PLAIN));

        Map<String, String> contentMap = getContentMap(conn);
        assertTrue("content should not be empty", !contentMap.isEmpty());
        assertEquals("target does not match", target, contentMap.get(TARGET));
        assertEquals("coordsys does not match", ICRS, contentMap.get(COORDSYS));
        assertEquals("ra does not match", ra, new Double(contentMap.get(RA)), 1.0);
        assertEquals("dec does not match", dec, new Double(contentMap.get(DEC)), 1.0);

        if (maxDetail)
        {
            if (oname == null)
            {
                assertEquals("oname does not match", "", contentMap.get(ONAME));
            }
            else
            {
                assertEquals("oname does not match", oname, contentMap.get(ONAME));
            }
            if (otype == null)
            {
                assertEquals("otype does not match", "", contentMap.get(OTYPE));
            }
            else
            {
                assertEquals("otype does not match", otype, contentMap.get(OTYPE));
            }
            if (mtype == null)
            {
                assertEquals("mtype does not match", "", contentMap.get(MTYPE));
            }
            else
            {
                assertEquals("mtype does not match", mtype, contentMap.get(MTYPE));
            }
        }
        else
        {
            assertNull("oname should be null", contentMap.get(ONAME));
            assertNull("otype should be null", contentMap.get(OTYPE));
            assertNull("mtype should be null", contentMap.get(MTYPE));
        }
        assertNotNull("time should not be null", contentMap.get(TIME_MS));
    }

    private JSONObject getJSONContent(final HttpURLConnection conn) throws Exception
    {
        return new JSONObject(getContent(conn));
    }

    private void validateJSON(final String query, final String target, final double ra, final double dec,
                              final String oname, final String otype, final String mtype, final boolean maxDetail)
            throws Exception
    {
        final HttpURLConnection conn = openConnection(query);
        conn.setRequestMethod("GET");
        assertEquals("wrong response code", 200, conn.getResponseCode());
        assertTrue("wrong content type >> " + conn.getURL().toExternalForm(),
                   conn.getHeaderField("content-type").contains(APPLICATION_JSON));

        final JSONObject resultsJSON = getJSONContent(conn);

        assertEquals("target does not match", NetUtil.encode(target), resultsJSON.getString("target"));
        assertEquals("coordsys does not match", ICRS, resultsJSON.getString("coordsys"));
        assertEquals("ra does not match", ra, resultsJSON.getDouble("ra"), 1.0);
        assertEquals("dec does not match", dec, resultsJSON.getDouble("dec"), 1.0);

        if (maxDetail)
        {
            if (oname == null)
            {
                assertEquals("oname does not match", "", resultsJSON.getString(ONAME));
            }
            else
            {
                assertEquals("oname does not match", oname, resultsJSON.getString(ONAME));
            }

            if (otype == null)
            {
                assertEquals("otype does not match", "", resultsJSON.getString(OTYPE));
            }
            else
            {
                assertEquals("otype does not match", otype, resultsJSON.getString(OTYPE));
            }

            if (mtype == null)
            {
                assertEquals("mtype does not match", "", resultsJSON.getString(MTYPE));
            }
            else
            {
                assertEquals("mtype does not match", mtype, resultsJSON.getString(MTYPE));
            }
        }
    }

    private void validateXML(final String query, final String target, final double ra, final double dec,
                             final String oname, final String otype, final String mtype, final boolean maxDetail)
            throws Exception
    {
        final HttpURLConnection conn = openConnection(query);
        conn.setRequestMethod("GET");
        assertEquals("wrong response code", 200, conn.getResponseCode());
        assertTrue("wrong content type >> " + conn.getURL().toExternalForm(),
                   conn.getHeaderField("content-type").contains(TEXT_XML));

        final Document document =
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(conn.getInputStream());
        final Element root = document.getDocumentElement();

        assertEquals("target does not match", NetUtil.encode(target), getText("target", root));
        assertEquals("coordsys does not match", ICRS, getText("coordsys", root));
        assertEquals("ra does not match", ra, new Double(getText("ra", root)), 1.0);
        assertEquals("dec does not match", dec, new Double(getText("dec", root)), 1.0);

        if (maxDetail)
        {
            if (oname == null)
            {
                assertEquals("oname does not match", "", getText(ONAME, root));
            }
            else
            {
                assertEquals("oname does not match", oname, getText(ONAME, root));
            }

            if (otype == null)
            {
                assertEquals("otype does not match", "", getText(OTYPE, root));
            }
            else
            {
                assertEquals("otype does not match", otype, getText(OTYPE, root));
            }

            if (mtype == null)
            {
                assertEquals("mtype does not match", "", getText(MTYPE, root));
            }
            else
            {
                assertEquals("mtype does not match", mtype, getText(MTYPE, root));
            }
        }
    }

    private String getText(final String tagName, final Element element) throws Exception
    {
        final NodeList nodeList = element.getElementsByTagName(tagName);

        return (nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : "");
    }
}
