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

package ca.nrc.cadc.nameresolver.parser;

import ca.nrc.cadc.nameresolver.Parser;
import ca.nrc.cadc.nameresolver.TargetData;
import ca.nrc.cadc.nameresolver.exception.TargetDataParsingException;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import ca.nrc.cadc.util.CoordUtil;

/**
 * Parses the results of a Sesame target query for the RA and DEC values.
 *
 * @author jburke
 */
public class SesameParser extends DefaultParser implements Parser {
    private static final Logger log = Logger.getLogger(SesameParser.class);

    private static final String VIZIER_TARGET_NOT_FOUND = "#!VizieR:";

    /**
     * Constructs a new SesameResolver thread initialized with the specified target name,
     * host name, and timeout.
     *
     * @param target  the target name.
     * @param host    the host name.
     * @param results the resolver results to parse.
     */
    public SesameParser(String target, String host, String results) {
        super(target, host, "Sesame", results);
    }

    /**
     * Parses the resolver results for the RA and Dec values. Object name
     * and type, and morphology, are not included in the Vizier results
     * and will be null. Returns null if no results found.
     *
     * @return TargetData object if successful in parsing out the RA and Dec
     * values, or null if the resolver was unable to resolve the target.
     * @throws TargetDataParsingException if unable to parse the results
     *                                    returned from the resolver.
     */
    @Override
    public TargetData parse() throws TargetDataParsingException {
        log.debug("parsing...");
        TargetData targetData = null;
        String[] lines = getResults().split("\n");

        for (String line : lines) {
            if (line == null || line.equals("")) {
                continue;
            }

            if (line.startsWith(VIZIER_TARGET_NOT_FOUND)) {
                log.debug("Target not found in " + getHost());
                return null;
            }

            if (line.startsWith("#=")) {
                int colon = line.indexOf(':');
                if (colon > 2) {
                    setDatabase(line.substring(2, colon));
                }
            } else if (line.startsWith("%J ")) {
                StringTokenizer tokenizer = new StringTokenizer(line, " ");
                if (tokenizer.countTokens() < 3) {
                    final String message = "Unexpected Sesame data format: " + line;
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                }

                try {
                    tokenizer.nextToken();
                    final double ra = CoordUtil.raToDegrees(tokenizer.nextToken());
                    final double dec = CoordUtil.decToDegrees(tokenizer.nextToken());
                    targetData = new TargetData(getTarget(), getHost(), getDatabase(), ra, dec, null,
                                                null, null);
                    break;
                } catch (NumberFormatException nfe) {
                    final String message = "Sesame number format exception: " + nfe.getMessage();
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                } catch (IllegalArgumentException iae) {
                    final String message = "Seasme illegal argument exception: " + iae.getMessage();
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                }
            }
        }
        log.debug("returning " + targetData);
        return targetData;
    }

}
