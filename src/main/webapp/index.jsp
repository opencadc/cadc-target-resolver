<%--
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
--%>
<%@ page language="java" contentType="text/html; ISO-8859-1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%
  final String schemeServerName = "http://" + request.getServerName();
  final int requestServerPort = request.getServerPort();
  final int port = ((request.getScheme().equals("http") && (requestServerPort == 80))
                    || (request.getScheme().equals("https") && (requestServerPort == 443)))
                   ? -1 : requestServerPort;
  final String displayLinkURL = schemeServerName + ((port > 0) ? ":" + port : "") + request.getContextPath() + "/find";
%>

<c:set var="displayLinkURL" value="<%= displayLinkURL %>" />

<!DOCTYPE html>

<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
  <meta name="description" content="Target name resolver">
  <meta name="author" content="Canadian Astronomy Data Centre">
  <meta name="keywords" content="CADC, Canadian Astronomy Data Centre">

  <title>Target Name Resolver</title>
</head>
<body>

<div class="main">

  <h1>CADC Target Name Resolver</h1>

  <p>
    The CADC Target Name Resolver is a simple web application that services HTTP GET requests to
    resolve astronomical object names to RA and DEC coordinates. Name Resolver
    concurrently queries one or more services to resolve the object name,
    returning the first positive result. The services queried are:
  </p>

  <div class="table">
    <table class="content">
      <tr>
        <th nowrap="true">Service</th>
        <th>Description</th>
      </tr>
      <tr class="odd_b">
        <td nowrap="true"><a href="http://nedwww.ipac.caltech.edu/index.html">NED</a></td>
        <td>The NASA/IPAC ExtraGalactic Database at the
          California Institute of Technology (CalTech).
        </td>
      </tr>
      <tr class="even_b">
        <td nowrap="true"><a href="http://simbad.u-strasbg.fr/simbad/">Simbad</a></td>
        <td>The SIMBAD Astronomical Database at the Centre de Donn&#233;es
          astronomiques de Strasbourg (CDS).
        </td>
      </tr>
      <tr class="odd_b">
        <td nowrap="true"><a href="http://vizier.u-strasbg.fr/viz-bin/VizieR">VizieR</a></td>
        <td>The VizieR service at CDS.</td>
      </tr>
      <tr class="even_b">
        <td nowrap="true"><a href="http://vizier.hia.nrc.ca/viz-bin/VizieR">VizieR</a></td>
        <td>The VizieR service at the Canadian Astronomy Data Centre (CADC).</td>
      </tr>
    </table>
  </div>

  <h2>HTTP GET parameters</h2>

  <div class="table">
    <table class="content">
      <tr>
        <th nowrap="true">Parameter</th>
        <th nowrap="true">Required?</th>
        <th>Value</th>
        <th>Description</th>
      </tr>
      <tr class="odd_b">
        <td>target</td>
        <td>Yes</td>
        <td>[object name]</td>
        <td>The object name to resolve.</td>
      </tr>
      <tr class="even_b">
        <td>service</td>
        <td>No</td>
        <td>
          all (default)<br/>
          ned<br/>
          simbad<br/>
          vizier
        </td>
        <td>
          Multiple query parameters accepted (e.g. service=ned&service=vizier). <br/>
          all - queries the NED, Simbad, and VizieR services concurrently.<br/>
          ned - queries the NED service at CalTech.<br/>
          simbad - queries the SIMBAD service at CDS.<br/>
          vizier - queries the VizieR services at CADC and CDS.
        </td>
      </tr>
      <tr class="odd_b">
        <td>format</td>
        <td>No</td>
        <td>
          ascii (default)<br/>
          json
        </td>
        <td>
          ascii - return results in ASCII format.<br/>
          json - return results in JSON format.
        </td>
      </tr>
      <tr class="even_b">
        <td>cached</td>
        <td>No</td>
        <td>
          yes (default)<br/>
          no
        </td>
        <td>
          yes - return previous cached object coordinates.<br/>
          no - query services for object coordinates.
        </td>
      </tr>
      <tr class="odd_b">
        <td>detail</td>
        <td>No</td>
        <td>
          min (default)<br/>
          max
        </td>
        <td>
          min - default fields:<br/>
          &nbsp;&nbsp;&nbsp;&nbsp;target: the target name to resolve.<br/>
          &nbsp;&nbsp;&nbsp;&nbsp;service: the service that resolved the object.<br/>
          &nbsp;&nbsp;&nbsp;&nbsp;coordsys: the coordinates system, always ICRS.<br/>
          &nbsp;&nbsp;&nbsp;&nbsp;ra: the right ascension of the object in degrees.<br/>
          &nbsp;&nbsp;&nbsp;&nbsp;dec: the declination of the object in degrees.<br/>
          &nbsp;&nbsp;&nbsp;&nbsp;time: time in milliseconds to resolve the object.<br/>
          max - default with following additional fields:<br/>
          &nbsp;&nbsp;&nbsp;&nbsp;oname: the service preferred object name.<br/>
          &nbsp;&nbsp;&nbsp;&nbsp;otype: the object type code.<br/>
          &nbsp;&nbsp;&nbsp;&nbsp;mtype: the object morphology type.
        </td>
      </tr>
    </table>
  </div>

  <h2>Examples</h2>
  <dl>
    <dt>Query for target m31 using default parameters in ASCII</dt>
    <dd>
      &nbsp;&nbsp;&nbsp;&nbsp;<a id="ascii_example" class="example" data-format="ascii"
                                 href="${displayLinkURL}?target=m31">${displayLinkURL}?target=m31</a>
      <br/><br/>
      &nbsp;&nbsp;Results:
      <br/>
      <pre>
        <c:import var="ascii_output" url="${displayLinkURL}?target=m31" />
        <code id="ascii_output"><c:out value="${ascii_output}" /></code>
      </pre>
    </dd>
    <dt>Query for target m31 using optional parameters: service = SIMBAD, cached = do not return cached coordinates,
      detail = maximum, results = JSON
    </dt>
    <dd>
      <br/><br/>
      &nbsp;&nbsp;&nbsp;&nbsp;<a id="json_example" class="example" data-format="json"
                                 href="${displayLinkURL}?target=m31&service=simbad&cached=no&detail=max&format=json">${displayLinkURL}?target=m31&service=simbad&cached=no&detail=max&format=json</a>
      <br/><br/>
      &nbsp;&nbsp;Results:
      <pre>
      <c:import var="json_output" url="${displayLinkURL}?target=m31&service=simbad&cached=no&detail=max&format=json" />
      <code id="json_output"><c:out value="${json_output}" /> </code>
    </pre>
    </dd>
  </dl>

  <!-- close the main div -->
</div>
</body>

</html>