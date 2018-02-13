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

import org.apache.log4j.Logger;

import ca.nrc.cadc.util.CoordUtil;

/**
 * Parses the results of a Simbad target query for the RA and DEC values.
 *
 * @author jburke
 */
public class SIMBADParser extends DefaultParser implements Parser {
    private static final Logger log = Logger.getLogger(SIMBADParser.class);

    private static final String SIMBAD_ERROR_COMMENT = "!!";
    private static final String SIMBAD_ERROR_TARGET_NOT_FOUND = "Identifier not found";
    private static final String SIMBAD_ERROR_BIBCODE = "For querying by bibcode";
    private static final String SIMBAD_ERROR_COORD_QUERY = "For querying by coordinates";
    private static final String SIMBAD_ERROR_CATALOG_NOT_FOUND = "No known catalog could be found";
    private static final String SIMBAD_ERROR_CATALOG_INCORRECT_FORMAT = "this identifier has an incorrect format for " +
        "catalog";


    private static final String SIMBAD_COORDINATES = "Coordinates";
    private static final String SIMBAD_OBJECT = "Object";
    private static final String SIMBAD_MORPHOLOGY = "Morphological type:";
    private static final String SIMBAD_MORPHOLOGY_UNKNOWN = "~";


    /**
     * Constructs a new SimbadResolver thread initialized with the specified target name,
     * host name, and timeout.
     *
     * @param target  the target name.
     * @param host    the host name.
     * @param results the resolver results to parse.
     */
    public SIMBADParser(final String target, final String host, final String results) {
        super(target, host, "Simbad", results);
    }

    /**
     * Parses the resolver results for the RA & Dec, object name and type,
     * and morphology values. Returns null if no results found.
     * <p>
     * Typical result line below:
     * <p>
     * Object M 31  ---  G  ---  OID=@1575544   (@@143594,0)  ---  coobox=11643
     * <p>
     * Coordinates(ICRS,ep=J2000,eq=2000): 010.684708 +41.268750 (IR  ) B [~ ~ ~] 2006AJ....131.1163S
     * Morphological type: SA(s)b  D 2013AJ....146...67B
     */
    @Override
    public TargetData parse()
        throws TargetDataParsingException {
        Double ra = null;
        Double dec = null;
        String name = null;
        String type = null;
        String morphology = null;

        String[] lines = getResults().split("\n");
        for (String line : lines) {
            if (line == null || line.equals("")) {
                continue;
            }

            // Try and detect some common query errors when SIMBAD thinks the
            // target is a coordinate, catalog, or bibcode.
            if (line.startsWith(SIMBAD_ERROR_COMMENT)) {
                if (line.contains(SIMBAD_ERROR_TARGET_NOT_FOUND) ||
                    line.contains(SIMBAD_ERROR_BIBCODE) ||
                    line.contains(SIMBAD_ERROR_COORD_QUERY) ||
                    line.contains(SIMBAD_ERROR_CATALOG_NOT_FOUND) ||
                    line.contains(SIMBAD_ERROR_CATALOG_INCORRECT_FORMAT)) {
                    log.debug("Simbad query error: " + line);
                    return null;
                } else {
                    final String message = "Simbad error: " + line;
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                }
            }

            // JB 2014.07.03 - added extra check for what appears to be a bug in Simbad
            // where the Coordinates line is now starts with nullCoordinates.
            if (line.startsWith(SIMBAD_COORDINATES) || line.startsWith("null" + SIMBAD_COORDINATES)) {
                String[] tokens = line.split(" ");
                if (tokens.length < 3) {
                    final String message = "Unexpected Simbad Coordinates format: " + line;
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                }

                try {
                    ra = CoordUtil.raToDegrees(tokens[1]);
                    dec = CoordUtil.decToDegrees(tokens[2]);
                } catch (NumberFormatException nfe) {
                    final String message = "Simbad number format exception: " + nfe.getMessage();
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                } catch (IllegalArgumentException iae) {
                    final String message = "Simbad illegal argument exception: " + iae.getMessage();
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                }
            } else if (line.startsWith(SIMBAD_OBJECT)) {
                int start = line.indexOf("---", 0);
                if (start == -1) {
                    final String message = "Unexpected Simbad Object format: " + line;
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                }
                name = line.substring(SIMBAD_OBJECT.length(), start).trim();

                int end = line.indexOf("---", start + 3);
                if (end == -1) {
                    final String message = "Unexpected Simbad Object format: " + line;
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                }
                type = line.substring(start + 3, end).trim();
            } else if (line.startsWith(SIMBAD_MORPHOLOGY)) {
                String[] tokens = line.split(" ");
                if (tokens.length < 3) {
                    final String message = "Unexpected Simbad Morphology format: " + line;
                    log.debug(message + "\n" + getResults());
                    throw new TargetDataParsingException(message);
                }
                morphology = tokens[2];
                if (morphology.equals(SIMBAD_MORPHOLOGY_UNKNOWN)) {
                    morphology = null;
                }
            }
        }

        if (ra == null) {
            return null;
        } else {
            return new TargetData(getTarget(), getHost(), getDatabase(), ra, dec, name, type, morphology);
        }
    }
}
