/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2011.                            (c) 2011.
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
 *  $Revision: 5 $
 *
 ************************************************************************
 */

package ca.nrc.cadc.nameresolver.integration;


import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.net.HttpDownload;
import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.reg.Interface;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.StringUtil;
import ca.nrc.cadc.vosi.CapabilitiesTest;
import ca.nrc.cadc.xml.XmlUtil;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author pdowler
 */
public class VosiCapabilitiesTest extends CapabilitiesTest {
    private static final Logger log = Logger.getLogger(VosiCapabilitiesTest.class);
    private static final URI RESOURCE_ID = URI.create("ivo://opencadc.org/resolver");

    RegistryClient regClient = new RegistryClient();
    
    public VosiCapabilitiesTest() {
        super(RESOURCE_ID);
    }

    RegistryClient getRegistryClient() throws Exception {
        return regClient;
    }

    Capabilities getCapabilities() throws Exception {
        return getRegistryClient().getCapabilities(RESOURCE_ID);
    }

    @Test
    public void testValidateCapabilitiesUsingGetCapabilities() {
        try {
            // get the capabilities associated with the resourceIdentifier
            Capabilities caps = getCapabilities();
            Assert.assertNotNull(caps);

            // each web service supports capabilitites, availability and logControl
            // in addition to capabilities specific to the web service
            List<Capability> capList = caps.getCapabilities();
            Assert.assertTrue("Incorrect number of capabilities", capList.size() > 2);

            // get the capability associated with the standard ID
            Capability cap = caps.findCapability(Standards.VOSI_CAPABILITIES);
            Assert.assertNotNull(cap);

            // get the interface associated with the securityMethod
            Interface intf = cap.findInterface(Standards.getAuthMethod(Standards.SECURITY_METHOD_ANON));
            Assert.assertNotNull(intf);

            validateContent(caps);
        } catch (Exception t) {
            log.error("unexpected exception", t);
            throw new RuntimeException(t);
        }
    }

    @Test
    public void testValidateCapabilitiesUsingGetServiceURL() {
        try {
            final RegistryClient rc = getRegistryClient();

            URL serviceURL = rc.getServiceURL(RESOURCE_ID, Standards.VOSI_CAPABILITIES, AuthMethod.ANON);
            Assert.assertNotNull(serviceURL);
            log.info("serviceURL=" + serviceURL);

            validateContent(getCapabilities());
        } catch (Exception t) {
            log.error("unexpected exception", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    @Test
    public void testValidateCapabilitiesNamespaces() {
        try {
            final RegistryClient rc = getRegistryClient();
            URL serviceURL = rc.getServiceURL(RESOURCE_ID, Standards.VOSI_CAPABILITIES, AuthMethod.ANON);
            Assert.assertNotNull(serviceURL);
            log.info("serviceURL=" + serviceURL);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpDownload download = new HttpDownload(serviceURL, out);
            download.setFollowRedirects(true);
            download.run();

            if (download.getThrowable() != null) {
                Assert.fail("Unable to download capabilities XML because " + download.getThrowable().getMessage());
            }

            Document doc = XmlUtil.buildDocument(out.toString(StandardCharsets.UTF_8.toString()));
            Element capabilities = doc.getRootElement();
            List<Namespace> namespaces = capabilities.getAdditionalNamespaces();
            for (Namespace namespace : namespaces) {
                if (namespace.getURI().startsWith("http://www.ivoa.net/xml/VODataService/")) {
                    Assert.assertEquals("Expected VODataService namespace prefix vs, found " + namespace.getPrefix(),
                                        "vs", namespace.getPrefix());
                }
                if (namespace.getURI().startsWith("http://www.ivoa.net/xml/VOResource/")) {
                    Assert.assertEquals("Expected VOResource namespace prefix vr, found " + namespace.getPrefix(),
                                        "vr", namespace.getPrefix());
                }
            }
        } catch (Exception t) {
            log.error("unexpected exception", t);
            throw new RuntimeException(t);
        }
    }
}
