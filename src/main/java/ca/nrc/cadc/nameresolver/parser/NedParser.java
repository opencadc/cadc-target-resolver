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

import ca.nrc.cadc.dali.tables.TableData;
import ca.nrc.cadc.dali.tables.votable.VOTableDocument;
import ca.nrc.cadc.dali.tables.votable.VOTableField;
import ca.nrc.cadc.dali.tables.votable.VOTableParam;
import ca.nrc.cadc.dali.tables.votable.VOTableReader;
import ca.nrc.cadc.dali.tables.votable.VOTableResource;
import ca.nrc.cadc.dali.tables.votable.VOTableTable;
import ca.nrc.cadc.nameresolver.Parser;
import ca.nrc.cadc.nameresolver.TargetData;
import ca.nrc.cadc.nameresolver.exception.TargetDataParsingException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Parses the VOTable results from a NED target query. The results are parsed using the VOTable pull parser from CDS.
 *
 * @author jburke
 */
public class NedParser extends DefaultParser implements Parser
{
    private final static Logger log = Logger.getLogger(NedParser.class);

    private static final String RA_FIELD_NAME = "RA(deg)";
    private static final String DEC_FIELD_NAME = "DEC(deg)";
    private static final String NAME_FIELD_NAME = "Object Name";
    private static final String TYPE_FIELD_NAME = "Type";

    /**
     * Constructs a new NEDResolver thread initialized with the specified
     * target name, host name, and timeout.
     *
     * @param target  the target name.
     * @param host    the host name.
     * @param results the resolver results to parse.
     */
    public NedParser(String target, String host, String results)
    {
        super(target, host, "NED", results);
    }

    public TargetData parse() throws TargetDataParsingException
    {
        // Find the start of the XML data
        String results = getResults();
        int index = results.indexOf("<?xml");
        if (index == -1)
        {
            throw new TargetDataParsingException("VOTABLE not found in " + results);
        }

        VOTableReader reader = new VOTableReader(false);
        VOTableDocument votable;
        try
        {
            votable = reader.read(results.substring(index));
        }
        catch (IOException e)
        {
            final String message = "error reading NED VOTABLE because " + e.getMessage();
            throw new TargetDataParsingException(message);
        }

        List<VOTableResource> resources = votable.getResources();
        if (resources.isEmpty())
        {
            final String message = "NED VOTable does not contain a resource element";
            log.debug(message + "\n" + getResults());
            throw new TargetDataParsingException(message);
        }

        VOTableResource resource = resources.get(0);

        for (VOTableParam param : resource.getParams())
        {
            if (param.getName().equalsIgnoreCase("error"))
            {
                log.debug("Target not found in " + getDatabase());
                return null;
            }
        }

        VOTableTable table = resource.getTable();
        if (table == null)
        {
            final String message = "NED VOTable resource table not found";
            log.debug(message + "\n" + getResults());
            throw new TargetDataParsingException(message);
        }

        TableData tableData = table.getTableData();
        if (tableData == null)
        {
            final String message = "Parsing error, NED VOTable table data not found";
            log.debug(message + "\n" + getResults());
            throw new TargetDataParsingException(message);
        }

        List<VOTableField> fields = table.getFields();
        if (fields.isEmpty())
        {
            final String message = "Parsing error, NED VOTable data fields not found";
            log.debug(message + "\n" + getResults());
            throw new TargetDataParsingException(message);
        }

        Iterator<List<Object>> iterator = tableData.iterator();
        if (!iterator.hasNext())
        {
            final String message = "Parsing error, NED VOTable data is empty";
            log.debug(message + "\n" + getResults());
            throw new TargetDataParsingException(message);
        }

        TargetData targetData = null;
        Double ra = null;
        Double dec = null;
        String oname = null;
        String otype = null;
        List<Object> data = iterator.next();
        for (int i = 0; i < fields.size(); i++)
        {
            VOTableField field = fields.get(i);
            try
            {
                Object value = data.get(i);
                if (RA_FIELD_NAME.equals(field.getName()))
                {
                    ra = (Double) value;
                }
                if (DEC_FIELD_NAME.equals(field.getName()))
                {
                    dec = (Double) value;
                }
                if (NAME_FIELD_NAME.equals(field.getName()))
                {
                    oname = (String) value;
                }
                if (TYPE_FIELD_NAME.equals(field.getName()))
                {
                    otype = (String) value;
                }
            }
            catch (NumberFormatException nfe)
            {
                final String message = "NED number format exception: " + nfe.getMessage();
                log.debug(message + "\n" + getResults());
                throw new TargetDataParsingException(message);
            }
        }
        if (ra != null && dec != null)
        {
            targetData = new TargetData(getTarget(), getHost(), getDatabase(), ra, dec, oname, otype, null);
        }
        return targetData;
    }
}
