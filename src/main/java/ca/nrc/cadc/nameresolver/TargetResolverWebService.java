/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2022.                            (c) 2022.
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

import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.vosi.Availability;
import ca.nrc.cadc.vosi.AvailabilityPlugin;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class TargetResolverWebService implements AvailabilityPlugin {
    private static final Logger log = Logger.getLogger(TargetResolverWebService.class);
    private final Map<Service, String> testNameValues = new HashMap<>();

    public TargetResolverWebService() {
        testNameValues.put(Service.NED, "m17");
        testNameValues.put(Service.SIMBAD, "M17");
        testNameValues.put(Service.VIZIER, NetUtil.encode("HD 79158"));
    }

    /**
     * Set application name. The appName is a string unique to this
     * application.
     *
     * @param appName unique application name
     */
    @Override
    public void setAppName(String appName) {
        // Do nothing.
    }

    @Override
    public Availability getStatus() {
        boolean isAvailable = false;
        StringBuilder note = new StringBuilder();
        try {
            NRServlet serv = new NRServlet();
            serv.init(null);

            for (final Service service : Service.values()) {
                note.append("*** Service ").append(service.name());

                final Map<String, String[]> requestMap = new HashMap<>();
                requestMap.put("target", new String[]{testNameValues.get(service)});
                requestMap.put("service", new String[]{service.getCommonName()});
                requestMap.put("format", new String[]{"json"});
                TargetResolverRequest targetResolverRequest = null;
                try {
                    targetResolverRequest = new TargetResolverRequest(requestMap);
                    TargetData targetData = serv.exhaustiveLookup(targetResolverRequest);
                    if (targetData != null) {
                        note.append(" is available.");
                    } else {
                        note.append(" is not available.");
                    }
                } catch (Exception e) {
                    note.append(" is not available: ").append(e.getMessage());
                    log.error("Error checking service " + service.name(), e);
                } finally {
                    note.append(" ***   ");
                }
            }
            isAvailable = true;
        } catch (Throwable t) {
            note.append("Unrecoverable error: ").append(t);
        }

        return new Availability(isAvailable, note.toString());
    }

    /**
     * The AvailabilityServlet supports a POST with state=??? that it will pass
     * on to the WebService. This can be used to implement state-changes in the
     * service, eg disabling or enabling features.
     *
     * @param state New state to set.
     */
    @Override
    public void setState(final String state) {
        // Does nothing.
    }
    
    @Override
    public boolean heartbeat() {
        return true;
    }

}
