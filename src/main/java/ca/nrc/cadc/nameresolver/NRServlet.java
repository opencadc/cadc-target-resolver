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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ca.nrc.cadc.nameresolver.parser.SIMBADParser;
import ca.nrc.cadc.nameresolver.parser.SesameParser;
import ca.nrc.cadc.nameresolver.parser.NedParser;
import ca.nrc.cadc.log.ServletLogInfo;
import ca.nrc.cadc.log.WebServiceLogInfo;
import ca.nrc.cadc.nameresolver.exception.TargetDataParsingException;


/**
 * Servlet to resolve a target name and return the target RA & DEC values in degrees.
 * The servlet will simultaneously query several name resolvers, returning the RA & DEC
 * values from the first positive results returned.
 *
 * @author jburke
 */
public class NRServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(NRServlet.class);

    private static final int NAME_RESOLVER_HTTP_ERROR_CODE = 425;

    private static final long serialVersionUID = 200706051000L;
    private static final long TARGET_DATA_CACHE_LIFE = 604800000L;  // cache life in milliseconds (one week)
    private static final int DEFAULT_TARGET_CACHE_SIZE = 1000;
    private static final int DEFAULT_SELECTOR_TIMEOUT = 1000;
    private static final String TARGET_NOT_FOUND = "Target not found";
    private static final Pattern RADIUS_SEARCH_PATTERN = Pattern.compile("(\\w*).*,?\\s+\\d+");

    private final Map<String, TargetData> targetCache = new HashMap<>(DEFAULT_TARGET_CACHE_SIZE);
    private Charset charset;


    /**
     * @param config servlet config
     * @throws ServletException If servlet init exception
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        charset = Charset.forName("ISO-8859-1");
    }

    /**
     * Passed to doGet.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }

    /**
     * Query the services with the target name, returning the target coordinates or
     * a 500 HTTP response code if the target cannot be found.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        long start = System.currentTimeMillis();
        WebServiceLogInfo logInfo = new ServletLogInfo(request);
        logInfo.setMessage(request.getQueryString());
        LOGGER.info(logInfo.start());

        String message;
        TargetData targetData = null;
        TargetResolverRequest targetResolverRequest = null;
        TargetDataWriter targetDataWriter = null;
        try {
            targetResolverRequest = new TargetResolverRequest(request.getParameterMap());
            targetDataWriter = getWriter(targetResolverRequest.format);

            // check cache first, if not cached then query services
            String cacheKey = getCacheKey(targetResolverRequest.services, targetResolverRequest.target);

            if (targetResolverRequest.cached) {
                targetData = getCachedTargetData(cacheKey);
            }

            // Not cached?  Query the services for the target as-is...
            if (targetData == null) {
                targetData = exhaustiveLookup(targetResolverRequest);
            }

            // No results found.

            response.setContentType(targetResolverRequest.format.contentType);

            if (targetData == null) {
                message = TARGET_NOT_FOUND;
                targetData = new TargetData(targetResolverRequest.target, null,
                                            targetResolverRequest.getServicesAsString(), 0.0d, 0.0d,
                                            null, null, null, message);

                logInfo.setMessage(message);
                logInfo.setSuccess(false);
                response.setStatus(NAME_RESOLVER_HTTP_ERROR_CODE);
                targetDataWriter.write(targetData, targetResolverRequest, response.getWriter());
            } else {
                // if results not cached, add to cache
                setCachedTargetData(cacheKey, targetData);
                long time = System.currentTimeMillis() - start;
                targetData.setQueryTime(time);

                targetDataWriter.write(targetData, targetResolverRequest, response.getWriter());

                logInfo.setMessage(targetData.toString());
                logInfo.setSuccess(true);
            }
        } catch (Exception e) {
            message = e.getMessage();
            logInfo.setMessage(message);
            logInfo.setSuccess(false);
            LOGGER.error("NameResolverServlet Exception: " + e.toString());
            response.setStatus(NAME_RESOLVER_HTTP_ERROR_CODE);

            if (targetDataWriter != null) {
                targetData = new TargetData(targetResolverRequest.target, null,
                                            targetResolverRequest.getServicesAsString(), 0.0d, 0.0d,
                                            null, null, null, message);

                try {
                    targetDataWriter.write(targetData, targetResolverRequest, response.getWriter());
                } catch (Exception e2) {
                    throw new IllegalStateException(e2);
                }
            }
        } finally {
            // close remaining channels
            logInfo.setElapsedTime(System.currentTimeMillis() - start);
            LOGGER.info(logInfo.end());
        }
    }

    /**
     * Perform a query of the requested services for the requested target.
     *
     * @param targetResolverRequest The request object.
     * @return TargetData instance, or null if none found after exhaustive attempt.
     * @throws IOException If the Java NIO Channel failed.
     */
    TargetData exhaustiveLookup(final TargetResolverRequest targetResolverRequest) throws IOException {
        TargetData targetData = tryLookup(targetResolverRequest);

        if (targetData == null) {
            // If nothing found, try to strip off a radius and try again.
            final String targetValue = targetResolverRequest.target;
            final Matcher matcher = RADIUS_SEARCH_PATTERN.matcher(targetValue);

            if (matcher.find()) {
                final String targetMinusRadius = matcher.group(1);
                final TargetResolverRequest retryRequest =
                    new TargetResolverRequest(targetMinusRadius,
                                              targetResolverRequest.services.toArray(
                                                  new Service[targetResolverRequest.services.size()]),
                                              targetResolverRequest.format, targetResolverRequest.cached,
                                              targetResolverRequest.detail);

                targetData = tryLookup(retryRequest);
            }
        }

        return targetData;
    }

    /**
     * Call upon the service to query the request.
     *
     * @param targetResolverRequest The request.
     * @return TargetData instance, or null if none found.
     * @throws IOException If the Java NIO Channel failed.
     */
    TargetData tryLookup(final TargetResolverRequest targetResolverRequest) throws IOException {
        Selector selector = null;

        try {
            // open a selector
            selector = Selector.open();

            // create the channels and register connect interest with selector
            createChannels(selector, targetResolverRequest.services);

            // Query the services for the target
            return queryServices(selector, targetResolverRequest.target);
        } finally {
            closeChannels(selector);
        }
    }

    /**
     * Creates a key from the services and target.
     *
     * @param services Collection of SERVICES to query
     * @param target   the target name
     * @return String of target plus service names hash codes concatenated togeather
     */
    private String getCacheKey(Collection<Service> services, String target) {
        StringBuilder sb = new StringBuilder();
        sb.append(target.hashCode());
        for (Service service : services) {
            sb.append(service.name().hashCode());
        }
        return sb.toString();
    }

    /***
     * Get the TargetData object from the target class.
     *
     * @param cacheKey String key for target cache.
     * @return TargetData object if found in cache, null otherwise.
     */
    private TargetData getCachedTargetData(String cacheKey) {
        final TargetData targetData = targetCache.get(cacheKey);

        if (targetData == null) {
            return null;
        }

        if ((System.currentTimeMillis() - targetData.getTimestamp()) > TARGET_DATA_CACHE_LIFE) {
            targetCache.remove(cacheKey);
            return null;
        }

        return targetData;
    }

    /***
     * Add the TargetData object to the target cache.
     *
     * @param cacheKey String key for target cache.
     * @param targetData TargetData object.
     */
    private void setCachedTargetData(String cacheKey, TargetData targetData) {
        if (!targetCache.containsKey(cacheKey)) {
            targetData.setCached(true);
            targetCache.put(cacheKey, targetData);
        }
    }

    /**
     * Construct and start the service queries to resolve the target.
     * Returns the results from the first database that has successfully resolved the target,
     * or null if the target was not resolved.
     * <p>
     * Allow tests to override.
     *
     * @param selector the selector
     * @param target   the target name to resolve
     * @return TargetData with the target coordinates
     */
    TargetData queryServices(Selector selector, String target) {
        TargetData targetData = null;
        try {
            // create 'coders and buffers
            CharsetDecoder decoder = charset.newDecoder();
            CharsetEncoder encoder = charset.newEncoder();
            ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
            CharBuffer charBuffer = CharBuffer.allocate(2048);

            // process the selector keys
            boolean haveResults = false;
            String currentHost = null;
            final StringBuilder data = new StringBuilder();

            while (selector.select(DEFAULT_SELECTOR_TIMEOUT) > 0 && !haveResults) {
                final Set<SelectionKey> keys = selector.selectedKeys();
                for (final Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); ) {
                    final SelectionKey key = it.next();
                    it.remove();

                    final SocketChannel channel = (SocketChannel) key.channel();

                    // connectable channel, send the query request, then register interest in reading the channel
                    if (key.isConnectable()) {
                        connectChannel(selector, channel, key, encoder, target);
                    }

                    // readable channel, check the response code and parse the results
                    else if (key.isReadable()) {
                        // remember which channel we are processing
                        final String host = channel.socket().getInetAddress().getHostName();

                        if (currentHost == null) {
                            currentHost = host;
                        }

                        if (host.equals(currentHost)) {
                            final Service service = Service.valueOfFromHost(currentHost);
                            int bytesRead = readChannel(channel, decoder, buffer, charBuffer, data, currentHost);
                            if (bytesRead == -1) {
                                // All data read, check the http response code
                                HttpHeaderParser headerParser = new HttpHeaderParser(data.toString());
                                int responseCode = headerParser.getResponseCode();

                                LOGGER.debug(String.format("Got Response code %d from %s (%s)", responseCode,
                                                           currentHost,
                                                           service.name()));

                                // 200 returned, process the data
                                if (responseCode == HttpServletResponse.SC_OK) {
                                    Parser parser;

                                    if (service == Service.NED) {
                                        parser = new NedParser(target, currentHost, data.toString());
                                    } else if (service == Service.SIMBAD) {
                                        parser = new SIMBADParser(target, currentHost, data.toString());
                                    } else {
                                        parser = new SesameParser(target, currentHost, data.toString());
                                    }

                                    try {
                                        targetData = parser.parse();
                                    } catch (TargetDataParsingException e) {
                                        logParserError(e.getMessage());
                                    }

                                    if (targetData != null) {
                                        LOGGER.debug("  channel has results: " + currentHost);
                                        haveResults = true;
                                        closeChannel(channel, key);
                                        break;
                                    }
                                }

                                // 302 or 303, redirected
                                else if (responseCode == HttpServletResponse.SC_MOVED_TEMPORARILY
                                    || responseCode == HttpServletResponse.SC_MOVED_PERMANENTLY) {
                                    // get the redirect location
                                    String location = headerParser.getLocation();
                                    LOGGER.debug("  redirected[" + responseCode + "] to " + location);
                                    if (location == null) {
                                        // can't do anything but LOGGER the error
                                        LOGGER.error("redirected with no Location from " + currentHost);
                                    } else {
                                        // open a new channel to the redirect location
                                        createChannel(selector, new URL(location));
                                    }
                                } else {
                                    logHostError(currentHost, headerParser.getResponseCode());
                                }

                                // close channel, reset data StringBuilder and currentHost
                                closeChannel(channel, key);
                                data.setLength(0);
                                currentHost = null;
                            }
                        } else {
                            LOGGER.debug(String.format("Host does not match current host (%s != %s)", host,
                                                       currentHost));
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("IOException querying services: " + ioe.toString());
        }

        return targetData;
    }

    /**
     * For each service create a socket channel and register it with the selector.
     *
     * @param selector the selector
     * @param services List of SERVICES
     */
    private void createChannels(Selector selector, Collection<Service> services) {
        for (Service service : services) {
            createChannel(selector, service.getHost(), service.getPort());
        }
    }

    /**
     * Create a socket channel with the given host and register connect interest with the selector.
     * <p>
     * Override for tests to not actually make a request out.
     *
     * @param selector the selector
     * @param host     the host name
     * @param port     The port number.
     */
    void createChannel(Selector selector, String host, final int port) {
        try {
            InetSocketAddress address = new InetSocketAddress(host, port);
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(address);
            channel.register(selector, SelectionKey.OP_CONNECT);
            LOGGER.debug("  created channel: " + host);
        } catch (UnresolvedAddressException e) {
            logHostError(host, 404);
        } catch (IOException ioe) {
            LOGGER.error("IOException creating channel for " + host, ioe);
        }
    }

    /**
     * Create a socket channel with the given host and register connect interest with the selector.
     * <p>
     * Override for tests to not actually make a request out.
     *
     * @param selector the selector
     * @param url      the URL to use
     */
    void createChannel(Selector selector, URL url) {
        try {
            InetSocketAddress address = new InetSocketAddress(url.getHost(), url.getPort());
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(address);
            channel.register(selector, SelectionKey.OP_CONNECT);
            SelectionKey key = channel.keyFor(selector);
            key.attach(url);
            LOGGER.debug("  created redirect channel: " + url.getHost());
        } catch (UnresolvedAddressException e) {
            logHostError(url.getHost(), 404);
        } catch (IOException ioe) {
            LOGGER.error("IOException creating channel for " + url.getHost(), ioe);
        }
    }

    /**
     * Close all channels still registered with this selector.
     *
     * @param selector the selector
     */
    private void closeChannels(Selector selector) {
        if (selector != null) {
            try {
                final Set<SelectionKey> keys = selector.keys();

                for (final SelectionKey key : keys) {
                    closeChannel((SocketChannel) key.channel(), key);
                }

                selector.close();
            } catch (IOException ioe) {
                LOGGER.error("IOException closing channels: " + ioe.toString());
            }
        }
    }

    /**
     * Check that the channel is connected to the host, if not finish the connection,
     * then close this channel and cancel the key.
     *
     * @param channel the socket channel
     * @param key     the selection key
     */
    private void closeChannel(SocketChannel channel, SelectionKey key) {
        try {
            String host;
            if (channel.isConnectionPending()) {
                channel.finishConnect();
                host = "unconnected host";
            } else {
                host = channel.socket().getInetAddress().getHostName();
            }
            key.cancel();
            channel.close();
            LOGGER.debug("  closed channel: " + host);
        } catch (IOException ioe) {
            LOGGER.error("IO Exception closing channel: " + ioe.toString());
        }
    }

    /**
     * Check that the channel is connected to the host, if not finish the connection,
     * then write the request to the host and register read interest with the selector.
     *
     * @param selector the selector.
     * @param channel  the socket channel.
     * @param encoder  the char encoder.
     * @param target   the target.
     */
    private void connectChannel(Selector selector, SocketChannel channel, SelectionKey key, CharsetEncoder encoder,
                                String target) {
        try {
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }

            final String host = channel.socket().getInetAddress().getHostName();

            if (key.attachment() != null) {
                final URL url = (URL) key.attachment();
                channel.write(encoder.encode(CharBuffer.wrap("GET " + url.getPath() + "?" + url.getQuery()
                                                                 + "  HTTP/1.0\r\n\r\n")));
                LOGGER.debug("  connected redirect channel: " + host);
            } else {
                final Service service = Service.valueOfFromHost(host);
                final String connectString = service.getConnectString(target);
                channel.write(encoder.encode(CharBuffer.wrap(connectString)));
                LOGGER.debug(String.format("Connecting to '%s' with '%s'", host, connectString));
            }
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException ioe) {
            LOGGER.error("IOException connecting channel: " + ioe.toString());
        }
    }

    /**
     * If this channels host is the host we are currently processing, and we haven't reached the end of the data,
     * read the channel into the string buffer. If there is no more data, check the HTTP response header. If the
     * response code is 200, pass the data to the appropriate parser to parse out the results, then cancel the key
     * and close the channel.
     *
     * @param channel    the socket channel
     * @param decoder    the char decoder
     * @param buffer     the byte buffer
     * @param charBuffer the char buffer
     * @param data       the string builder for the data
     * @param host       the host currently being processed
     * @return true if results have been found, false otherwise
     */
    private int readChannel(SocketChannel channel, CharsetDecoder decoder, ByteBuffer buffer, CharBuffer charBuffer,
                            StringBuilder data, String host) {
        LOGGER.debug("  reading channel: " + host);
        int bytesRead = 0;
        try {
            bytesRead = channel.read(buffer);
            if (bytesRead != -1) {
                // Read the channel into a StringBuffer
                buffer.flip();
                decoder.decode(buffer, charBuffer, false);
                charBuffer.flip();
                if (charBuffer.length() > 0) {
                    data.append(charBuffer.toString());
                }
                buffer.clear();
                charBuffer.clear();
            }
        } catch (IOException ioe) {
            LOGGER.error("IOException reading channel: " + ioe.toString());
        }
        return bytesRead;
    }

    /**
     * Log an easily parsed error message that database output cannot be parsed.
     *
     * @param message the error message returned by the parser.
     */
    private static void logParserError(final String message) {
        String sb = Calendar.getInstance().getTime().toString() + " ***NameResolver*** "
            + "Error parsing resolver response: " + message;

        LOGGER.error(sb);
    }

    /**
     * Log an easily parsed error message that a database is not returning results.
     *
     * @param host       the host name.
     * @param statusCode the status code returned from the host.
     */
    private static void logHostError(final String host, final int statusCode) {
        LOGGER.error(Calendar.getInstance().getTime().toString() +
                         " *** cadc-target-resolver *** " + "Error connecting to host " + host + ", http status code "
                         + statusCode);
    }

    /**
     * Factory style to obtain a writer implementation.
     *
     * @param format The Format instance.
     * @return TargetDataWriter implementation (Default ASCII).
     */
    private TargetDataWriter getWriter(final Format format) {
        switch (format) {
            case JSON: {
                return new TargetDataJSONWriter();
            }

            case XML: {
                return new TargetDataXMLWriter();
            }

            default: {
                return new TargetDataASCIIWriter();
            }
        }
    }
}
