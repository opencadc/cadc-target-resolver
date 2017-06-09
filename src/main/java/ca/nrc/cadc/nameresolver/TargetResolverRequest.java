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

import ca.nrc.cadc.net.NetUtil;

import java.lang.annotation.Target;
import java.util.*;


class TargetResolverRequest
{
    private static final String TARGET_PARSING_ERROR = "Error parsing target from request";

    // The object name to resolve.
    final String target;
    final Collection<Service> services = new ArrayList<>();
    final Format format;
    final boolean cached;
    final Detail detail;


    /**
     * Complete constructor for testing.
     *
     * @param target            The target as entered by the client.
     * @param serviceArray      Requested services.
     * @param format            Output format.
     * @param cached            Flag to indicate cached items are preferred.
     * @param detail            Detail level of output.
     */
    TargetResolverRequest(final String target, final Service[] serviceArray, final Format format,
                          final boolean cached, final Detail detail)
    {
        this.target = target;

        if (serviceArray != null)
        {
            this.services.addAll(Arrays.asList(serviceArray));
        }

        this.format = format;
        this.cached = cached;
        this.detail = detail;
    }

    /**
     * Create a new Target Resolver request.
     *
     * @param requestMap The mapping of values from the Request.
     */
    TargetResolverRequest(final Map<String, String[]> requestMap)
    {
        final String[] targetParam = requestMap.get("target");

        if ((targetParam == null) || (targetParam.length == 0))
        {
            throw new IllegalArgumentException(TARGET_PARSING_ERROR);
        }
        else
        {
            this.target = NetUtil.decode(targetParam[0].trim());
        }

        final String[] serviceArray = requestMap.get("service");

        if ((serviceArray == null) || (serviceArray.length == 0))
        {
            this.services.addAll(Arrays.asList(Service.values()));
        }
        else
        {
            this.services.addAll(getServices(serviceArray));
        }

        final String[] formatParam = requestMap.get("format");
        this.format = getFormat(formatParam);

        final String[] cachedParam = requestMap.get("cached");
        this.cached = (cachedParam == null) || (cachedParam.length == 0)
                      || cachedParam[0].equalsIgnoreCase("yes");

        // amount of detail to return.
        final String[] detailParam = requestMap.get("detail");
        if ((detailParam == null) || (detailParam.length == 0))
        {
            this.detail = Detail.MIN;
        }
        else
        {
            this.detail = Detail.valueOf(detailParam[0].toUpperCase());
        }
    }

    String getServicesAsString()
    {
        final StringBuilder sb = new StringBuilder();

        for (final Service service : this.services)
        {
            sb.append(service.name()).append(", ");
        }

        if (sb.length() > 0)
        {
            sb.delete((sb.length() - 2), sb.length());
        }

        return sb.toString();
    }

    private Format getFormat(final String[] formatArray)
    {
        if ((formatArray == null) || (formatArray.length == 0))
        {
            return Format.ASCII;
        }
        else
        {
            return Format.valueOf(formatArray[0].toUpperCase());
        }
    }

    /**
     * Parse the HTTP service parameter.
     * Allowed values are ALL, NED, SIMBAD, VIZIER (case-insensitive).
     *
     * @param serviceArray Array of service names
     * @return String array of services to query
     */
    private Collection<Service> getServices(final String[] serviceArray)
    {
        final Set<Service> services = EnumSet.noneOf(Service.class);

        for (final String service : serviceArray)
        {
            switch (service.trim().toUpperCase())
            {
                case "ALL":
                {
                    services.addAll(Arrays.asList(Service.values()));
                    break;
                }

                case "NED":
                {
                    services.add(Service.NED);
                    break;
                }

                case "SIMBAD":
                {
                    services.add(Service.SIMBAD);
                    break;
                }

                case "VIZIER":
                {
                    services.add(Service.VIZIER_CADC);
                    services.add(Service.VIZIER_CDS);
                    break;
                }
            }
        }

        return services;
    }
}
