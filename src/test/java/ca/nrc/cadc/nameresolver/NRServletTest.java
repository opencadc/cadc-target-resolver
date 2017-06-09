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

package ca.nrc.cadc.nameresolver;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;


public class NRServletTest
{
    @Test
    public void doGetJSON() throws Exception
    {
        final TargetData td = new TargetData("m88", "myhost.com", "ned", 88.0d, 99.0d,
                                             "myobject", "objecttype", "mtype");

        final NRServlet testSubject = new NRServlet()
        {
            /**
             * Create a socket channel with the given host and register connect interest with the selector.
             * <p>
             * Override for tests to not actually make a request out.
             *
             * @param selector the selector
             * @param host     the host name
             */
            @Override
            void createChannel(Selector selector, String host)
            {

            }

            /**
             * Create a socket channel with the given host and register connect interest with the selector.
             * <p>
             * Override for tests to not actually make a request out.
             *
             * @param selector the selector
             * @param url      the URL to use
             */
            @Override
            void createChannel(Selector selector, URL url)
            {

            }

            /**
             * Construct and start the service queries to resolve the target.
             * Returns the results from the first database that has successfully resolved the target,
             * or null if the target was not resolved.
             * <p>
             * Allow tests to override.
             *
             * @param selector the selector
             * @param target   the target name to resolve
             * @return TargetData with the target coordinates
             */
            @Override
            TargetData queryServices(Selector selector, String target)
            {
                return td;
            }
        };

        final HttpServletRequest mockRequest = createMock(HttpServletRequest.class);
        final HttpServletResponse mockResponse = createMock(HttpServletResponse.class);
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        final Map<String, String[]> paramMap = new HashMap<>();

        paramMap.put("target", new String[]{"m88"});
        paramMap.put("format", new String[]{"json"});

        expect(mockResponse.getWriter()).andReturn(printWriter).once();

        mockResponse.setContentType("application/json");
        expectLastCall().once();

        expect(mockRequest.getPathInfo()).andReturn("/test/info").once();
        expect(mockRequest.getMethod()).andReturn("GET").once();
        expect(mockRequest.getRemoteAddr()).andReturn("http://remotehost.com").once();
        expect(mockRequest.getQueryString()).andReturn("target=m88&format=json").once();
        expect(mockRequest.getParameterMap()).andReturn(paramMap).once();

        replay(mockRequest, mockResponse);

        testSubject.doGet(mockRequest, mockResponse);

        final JSONObject resultJSON = new JSONObject(writer.toString());

        final JSONObject expectedJSON = new JSONObject();
        expectedJSON.put("target", "m88");
        expectedJSON.put("service", "ned(myhost.com)");
        expectedJSON.put("coordsys", "ICRS");
        expectedJSON.put("ra", 88.0d);
        expectedJSON.put("dec", 99.0d);
        expectedJSON.put("time", td.getQueryTime());

        JSONAssert.assertEquals("Wrong JSON output.", resultJSON, expectedJSON, true);

        verify(mockRequest, mockResponse);
    }

    @Test
    public void doGetASCII() throws Exception
    {
        final TargetData td = new TargetData("m88", "myhost.com", "ned", 88.0d, 99.0d,
                                             "myobject", "objecttype", "mtype");

        final NRServlet testSubject = new NRServlet()
        {
            /**
             * Create a socket channel with the given host and register connect interest with the selector.
             * <p>
             * Override for tests to not actually make a request out.
             *
             * @param selector the selector
             * @param host     the host name
             */
            @Override
            void createChannel(Selector selector, String host)
            {

            }

            /**
             * Create a socket channel with the given host and register connect interest with the selector.
             * <p>
             * Override for tests to not actually make a request out.
             *
             * @param selector the selector
             * @param url      the URL to use
             */
            @Override
            void createChannel(Selector selector, URL url)
            {

            }

            /**
             * Construct and start the service queries to resolve the target.
             * Returns the results from the first database that has successfully resolved the target,
             * or null if the target was not resolved.
             * <p>
             * Allow tests to override.
             *
             * @param selector the selector
             * @param target   the target name to resolve
             * @return TargetData with the target coordinates
             */
            @Override
            TargetData queryServices(Selector selector, String target)
            {
                return td;
            }
        };

        final HttpServletRequest mockRequest = createMock(HttpServletRequest.class);
        final HttpServletResponse mockResponse = createMock(HttpServletResponse.class);
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        final Map<String, String[]> paramMap = new HashMap<>();

        paramMap.put("target", new String[]{"m88"});
        paramMap.put("format", new String[]{"ascii"});

        expect(mockResponse.getWriter()).andReturn(printWriter).once();

        mockResponse.setContentType("text/plain");
        expectLastCall().once();

        expect(mockRequest.getPathInfo()).andReturn("/test/info").once();
        expect(mockRequest.getMethod()).andReturn("GET").once();
        expect(mockRequest.getRemoteAddr()).andReturn("http://remotehost.com").once();
        expect(mockRequest.getQueryString()).andReturn("target=m88&format=ascii").once();
        expect(mockRequest.getParameterMap()).andReturn(paramMap).once();

        replay(mockRequest, mockResponse);

        testSubject.doGet(mockRequest, mockResponse);

        final String result = writer.toString();
        final String expected =
                "target=m88\r\nservice=ned(myhost.com)\r\ncoordsys=ICRS\r\nra=88.0\r\ndec=99.0\r\ntime(ms)="
                + td.getQueryTime() + "\r\n";

        Assert.assertEquals("Wrong ASCII Output", expected, result);

        verify(mockRequest, mockResponse);
    }

    @Test
    public void lookupWithCommaRadius() throws Exception
    {
        final TargetData td = new TargetData("M31", "myhost.com", "ned", 88.0d, 99.0d,
                                             "myobject", "objecttype", "mtype");
        final String target = "M31, 0.5";

        final NRServlet testSubject = new NRServlet()
        {
            /**
             * Call upon the service to query the request.
             *
             * @param targetResolverRequest The request.
             * @return TargetData instance, or null if none found.
             * @throws IOException If the Java NIO Channel failed.
             */
            @Override
            TargetData tryLookup(TargetResolverRequest targetResolverRequest) throws IOException
            {
                // Only return the target data is the one minus the radius is passed in.
                if (targetResolverRequest.target.equals(td.getTarget()))
                {
                    return td;
                }
                else
                {
                    return null;
                }
            }
        };

        final TargetResolverRequest targetResolverRequest = new TargetResolverRequest(target, null, Format.JSON, true,
                                                                                      Detail.MIN);
        final TargetData result = testSubject.exhaustiveLookup(targetResolverRequest);
        Assert.assertEquals("Wrong target.", "M31", result.getTarget());
    }

    @Test
    public void lookupWithSpaceRadius() throws Exception
    {
        final TargetData td = new TargetData("M31", "myhost.com", "ned", 88.0d, 99.0d,
                                             "myobject", "objecttype", "mtype");
        final String target = "M31 0.5";

        final NRServlet testSubject = new NRServlet()
        {
            /**
             * Call upon the service to query the request.
             *
             * @param targetResolverRequest The request.
             * @return TargetData instance, or null if none found.
             * @throws IOException If the Java NIO Channel failed.
             */
            @Override
            TargetData tryLookup(TargetResolverRequest targetResolverRequest) throws IOException
            {
                // Only return the target data is the one minus the radius is passed in.
                if (targetResolverRequest.target.equals(td.getTarget()))
                {
                    return td;
                }
                else
                {
                    return null;
                }
            }
        };

        final TargetResolverRequest targetResolverRequest = new TargetResolverRequest(target, null, Format.JSON, true,
                                                                                      Detail.MIN);
        final TargetData result = testSubject.exhaustiveLookup(targetResolverRequest);
        Assert.assertEquals("Wrong target.", "M31", result.getTarget());
    }
}
