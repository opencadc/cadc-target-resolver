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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses the HTTP headers from a String.
 *
 * @author jburke
 */
class HttpHeaderParser
{
    private static final Pattern NEWLINE = Pattern.compile("\n");
    private static final Pattern HEADER_LINE = Pattern.compile("HTTP/(1.[0-9]) ([0-9]{3})(?: [0-9]{3})? ([A-Za-z ]*)");

    private String data;
    private Map<String, String> headers;
    private int responseCode;

    /**
     * Parse the HTTP status code and headers from data.
     *
     * @param data the data to parse.
     */
    HttpHeaderParser(String data)
    {
        if (data == null)
        {
            throw new NullPointerException();
        }
        //log.debug("header:\n" + data);
        this.data = data;
        this.headers = new HashMap<>();
        this.responseCode = 0;
        parse();
    }

    /**
     * @param key header name.
     * @return the header value, null if it doesn't exist.
     */
    String getHeader(String key)
    {
        if (key == null)
        {
            throw new NullPointerException();
        }
        return headers.get(key.toLowerCase());
    }

    /**
     * @return the http response code
     */
    int getResponseCode()
    {
        return responseCode;
    }

    /**
     * Parse the data, using LF to separate the lines,
     * and either CRLF or LF to denote the end of the headers.
     */
    private void parse()
    {
        int index = this.data.indexOf("\n");
        if (index > 0)
        {
            final String header = this.data.substring(0, index).trim();
            final Matcher m = HEADER_LINE.matcher(header);
            if (m.matches())
            {
                responseCode = Integer.parseInt(m.group(2));
                data = data.substring(index + 1);
            }
        }

        int endOfHeader = data.indexOf("\r\n\r\n");

        if (endOfHeader == -1)
        {
            endOfHeader = data.indexOf("\n\n");
        }

        if (endOfHeader > 0)
        {
            String header = data.substring(0, endOfHeader);
            String[] lines = NEWLINE.split(header);
            for (final String l : lines)
            {
                String line = l.trim();
                if (line.length() == 0)
                {
                    continue;
                }

                int colon = line.indexOf(':');

                if (colon == -1)
                {
                    continue;
                }

                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                headers.put(key.toLowerCase(), value.toLowerCase());
            }
        }
    }
}
