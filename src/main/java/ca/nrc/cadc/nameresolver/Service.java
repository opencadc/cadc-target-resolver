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


public enum Service
{
    // Queries the NED service at CalTech.
    NED("ned", "ned.ipac.caltech.edu", 80, "/cgi-bin/nph-objsearch?"
                                           + "extend=no&out_csys=Equatorial"
                                           + "&out_equinox=J2000.0&obj_sort=RA+or+Longitude"
                                           + "&of=xml_main&zv_breaker=30000.0"
                                           + "&list_limit=1&img_stamp=NO"
                                           + "&objname=", "HTTP/1.0\r\n\r\n", false),

    // Queries the SIMBAD service at CDS.
    SIMBAD("simbad", "simbad.u-strasbg.fr", 80, "/simbad/sim-id?"
                                                + "output.max=1&output.format=ASCII"
                                                + "&obj.coo1=on&obj.coo2=off&obj.coo3=off&obj.coo4=off"
                                                + "&frame1=ICRS&epoch1=J2000&coodisp1=d"
                                                + "&obj.pmsel=off&obj.plxsel=off&obj.rvsel=off&obj.spsel=off"
                                                + "&obj.mtsel=on&obj.sizesel=off&obj.fluxsel=off&list.idsel=off"
                                                + "&obj.bibsel=off&list.bibsel=off&obj.messel=off&obj.notesel=off"
                                                + "&Ident=", "Connection: close HTTP/1.1\r\n\r\n", false),

    // Queries the VizieR services at CADC.
    //
    // VIZIER_CADC("vizier", "vizier.hia.nrc.ca", 80, "/cgi-bin/nph-sesame/-o/V?", "HTTP/1.0\r\n\r\n", true),

    // Queries the VizieR services at CDS.
    VIZIER_CDS("vizier", "cdsweb.u-strasbg.fr", 80, "/cgi-bin/nph-sesame/-o/V?", "HTTP/1.0\r\n\r\n", true);


    // Name used when referencing it in a business like manner, or as a request parameter.
    private final String commonName;

    private final String host;
    private final int port;
    private final String parameters;

    // HTTP information when connecting.
    private final String extraInfo;

    private final boolean caseSensitiveFlag;


    Service(final String commonName, final String host, int port, final String parameters, final String extraInfo,
            final boolean caseSensitiveFlag)
    {
        this.commonName = commonName;
        this.host = host;
        this.port = port;
        this.parameters = parameters;
        this.extraInfo = extraInfo;
        this.caseSensitiveFlag = caseSensitiveFlag;
    }

    public String getCommonName()
    {
        return commonName;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getConnectString(final String target)
    {
        return "GET " + parameters + (caseSensitiveFlag ? target : target.toLowerCase()) + " " + extraInfo;
    }

    public static Service valueOfFromHost(final String host)
    {
        for (final Service service : values())
        {
            if (service.getHost().equals(host))
            {
                return service;
            }
        }

        throw new IllegalArgumentException("No such Service for " + host);
    }
}
