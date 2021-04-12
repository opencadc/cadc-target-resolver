/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2021.                            (c) 2021.
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

import org.junit.Assert;
import org.junit.Test;

public class HttpHeaderParserTest {
    @Test
    public void parseApache24PlainCode() throws Exception {
        final String testSIMBADData = "HTTP/1.1 404 200\n"
                                      + "Date: Fri, 09 Apr 2021 18:34:04 GMT\n"
                                      + "Server: Apache/2.4.41 (Ubuntu)\n"
                                      + "Set-Cookie: JSESSIONID=599ADCC9AB00084D1F742CE8D6B907F2.new; Path=/simbad; HttpOnly\n"
                                      + "Vary: Accept-Encoding\n"
                                      + "Access-Control-Allow-Origin: *\n"
                                      + "Transfer-Encoding: chunked\n"
                                      + "Content-Type: text/plain;charset=UTF-8\n"
                                      + "\n"
                                      + "785\n"
                                      + "C.D.S.  -  SIMBAD4 rel 1.7  -  2021.04.09CEST20:34:04\n"
                                      + "\n"
                                      + "hd37017\n"
                                      + "-------\n"
                                      + "\n"
                                      + "Object V* V1046 Ori  ---  SB*  ---  OID=@804751   (@@16542,0)  ---  coobox=6479\n"
                                      + "\n"
                                      + "Coordinates(ICRS,ep=J2000,eq=2000): 083.8411154006778 -04.4941780197174 (Opt ) A [0.1226 0.1322 90] 2018yCat.1345....0G\n"
                                      + "hierarchy counts: #parents=0, #children=0, #siblings=0\n"
                                      + "Morphological type: ~ ~ ~\n"
                                      + "\n"
                                      + "Identifiers (45):\n"
                                      + "   TIC 427393058                   2MASS J05352186-0429390         SBC9 339                      \n"
                                      + "   BD-04 1183                      CEL 834                         CMC 132317                    \n"
                                      + "   CMC 304046                      GC 6932                         GCRV 3419                     \n"
                                      + "   GEN# +9.00010632                GSC 04774-00922                 HD 37017                      \n"
                                      + "   HGAM 434                        HIC 26233                       HIP 26233                     \n"
                                      + "   HR 1890                         JP11 5688                       JP11 5689                     \n"
                                      + "   MCW 335                         PPM 188221                      Parenago 1933                 \n"
                                      + "   ROT 873                         SAO 132317                      SBC7 239                      \n"
                                      + "   SKY# 9027                       TD1 4937                        TYC 4774-922-1                \n"
                                      + "   UBV 5508                        UBV M 51687                     V* V1046 Ori                  \n"
                                      + "   WH 285                          YZ 94 1663                      [GCS95] 239                   \n"
                                      + "   ALS 14786                       2XMM J053522.0-042938           AGASC 625745104               \n"
                                      + "   Brun 632                        Renson 9820                     WDS J05354-0430AB             \n"
                                      + "   ** HDS 745                      GBS-VLA J053521.86-042938.9     GEN# +2.19760632              \n"
                                      + "   uvby98 900010632                WEB 5132                        Gaia DR2 3209634905754969856  \n"
                                      + "================================================================================\n"
                                      + "\n"
                                      + "\n"
                                      + "0\n"
                                      + "\n";

        final HttpHeaderParser testSubject = new HttpHeaderParser(testSIMBADData);

        Assert.assertEquals("Wrong response code.", 404, testSubject.getResponseCode());
    }

    @Test
    public void parseApache24MultiCode() throws Exception {
        final String testVizieRData = "HTTP/1.1 200 OK\n"
                                      + "Date: Fri,  9 Apr 2021 18:48:25 GMT\n"
                                      + "Server: Apache/2.4.41 (Unix)\n"
                                      + "Content-Type: text/plain\n"
                                      + "Access-Control-Allow-Origin: *\n"
                                      + "Transfer-Encoding: chunked\n"
                                      + "\n"
                                      + "5f\n"
                                      + "# m51\t#Q145326\n"
                                      + "#! *** NNNothing found ***  (from cache)\n"
                                      + "\n"
                                      + "#====Done (2021-Apr-09,18:48:25z)====\n"
                                      + "\n";

        final HttpHeaderParser testSubject = new HttpHeaderParser(testVizieRData);

        Assert.assertEquals("Wrong response code.", 200, testSubject.getResponseCode());
    }
}
