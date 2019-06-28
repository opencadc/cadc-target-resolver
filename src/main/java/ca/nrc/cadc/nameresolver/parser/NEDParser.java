/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2019.                            (c) 2019.
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

package ca.nrc.cadc.nameresolver.parser;

import ca.nrc.cadc.nameresolver.Parser;
import ca.nrc.cadc.nameresolver.Service;
import ca.nrc.cadc.nameresolver.TargetData;
import ca.nrc.cadc.nameresolver.exception.TargetDataParsingException;
import java.util.*;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses the json results from a NED target query.
 *
 * @author jburke
 */
public class NEDParser extends DefaultParser implements Parser {
    private final static Logger log = Logger.getLogger(NEDParser.class);

    private final static double SUPPORTED_NED_VERSION = 2.0;

    private static Map<Integer, String> resultCodes =
            new TreeMap<Integer, String>();
    static {
        resultCodes.put(0, "Not an object name: the supplied string could not "
                + "be interpreted as the as a valid object name.");
        resultCodes.put(1, "Ambiguous name: the input was a valid but "
                + "unspecific name: a list of possibly intended aliases "
                + "is return.");
        resultCodes.put(2, "Valid name:  the input provided was interpreted as "
                + "a proper object name, but there is no such object known in "
                + "NED (it may be very new, or we may the NED standard form "
                + "is returned.");
        resultCodes.put(3, "Known name: the input provided was interpreted as "
                + "a proper object name that was known to NED.");
    }

    /**
     * Constructs a new NEDResolver thread initialized with the specified
     * target name, host name, and timeout.
     *
     * @param target  the target name.
     * @param host    the host name.
     * @param results the resolver results to parse.
     */
    public NEDParser(String target, String host, String results) {
        super(target, host, Service.NED.getCommonName(), results);
    }

    public TargetData parse() throws TargetDataParsingException {
        log.debug("parsing...");
        TargetData targetData = null;
        Double ra = null;
        Double dec = null;
        String oname = null;
        String otype = null;

        String[] lines = getResults().split("\n");
        for (String line : lines) {
            if (line != null && line.trim().startsWith("{")) {
                try {
                    JSONObject json = new JSONObject(line);

                    // check version of returned json
                    checkVersion(json.getDouble("Version"));

                    int resultCode = json.getInt("ResultCode");
                    if (resultCode == 3) {

                        JSONObject preferred = json.getJSONObject("Preferred");

                        // Object name
                        oname = preferred.getString("Name");

                        //Object type
                        JSONObject objectType = preferred.getJSONObject("ObjType");
                        otype = objectType.getString("Value");

                        // ra & dec
                        JSONObject position = preferred.getJSONObject("Position");
                        ra = position.getDouble("RA");
                        dec = position.getDouble("Dec");
                    } else {
                        log.debug(String.format("Result code %d: %s", resultCode,
                                resultCodes.get(resultCode)));
                    }
                    break;
                } catch (JSONException e) {
                    final String message = String.format("Unable to parse NED json\n\n'%s'\n\nbecause\n\n%s",
                            getResults(), e.getMessage());
                    throw new TargetDataParsingException(message);
                }
            }
        }
        if (ra != null && dec != null) {
            targetData = new TargetData(getTarget(), getHost(), getDatabase(),
                    ra, dec, oname, otype, null);
        }
        log.debug("returning " + targetData);
        return targetData;
    }

    private void checkVersion(final Double version) {
        if (version != SUPPORTED_NED_VERSION) {
            final String error = String.format("The current supported version "
                    + "%s does not match the returned version: %s",
                    SUPPORTED_NED_VERSION, version);
            log.error(error);
        }
    }

}
