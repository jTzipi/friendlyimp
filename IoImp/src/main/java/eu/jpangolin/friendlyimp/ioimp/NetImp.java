/*
 * Copyright (c) 2026. Tim Langhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.jpangolin.friendlyimp.ioimp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Net Utils.
 * <p>
 * More information's to port numbers can be found <a href="https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers">here</a>.
 *
 * @author jTzipi
 */
public final class NetImp {


    private static final Logger LOG = LoggerFactory.getLogger(NetImp.class);
    /**
     * URI of the IEEE website.
     */
    public static final URI URI_IEEE = URI.create("https://www.ieee.org");
    /**
     *
     */
    public static final int PORT_MIN = 1;
    /**
     *
     */
    public static final int PORT_FTP = 20;
    /**
     *
     */
    public static final int PORT_HTTP = 80;
    /**
     *
     */
    public static final int PORT_HTTPS = 443;
    /**
     *
     */
    public static final int PORT_MAX = 65535;
    /**
     *
     */
    public static final int MIN_TIMEOUT = 0;


    private NetImp() {
        //
    }

    /**
     * Try to open a connection to the net.
     * <p>
     * We here use the {@linkplain URLConnection} class to open a connection
     * to a host and try to read from the returning input stream.
     * </p>
     *
     * @return {@code true} if the connection can be made
     * @see #isConnectedToNet(URI)
     * @see #isConnectedToNet(Proxy)
     * @see #isConnectedToNet(URI, Proxy)
     */
    public static boolean isConnectedToNet() {

        return openCon(null, null);

    }

    /**
     * Try to open a connection to the net.
     * <p>
     * We here use the {@linkplain URLConnection} class to open a connection
     * to a host and try to read from the returning input stream.
     * </p>
     *
     * @param proxy Proxy
     * @return {@code true} if the connection can be made
     * @throws NullPointerException if {@code proxy}
     * @see #isConnectedToNet()
     * @see #isConnectedToNet(URI)
     * @see #isConnectedToNet(URI, Proxy)
     */
    public static boolean isConnectedToNet(Proxy proxy) {
        Objects.requireNonNull(proxy);

        return openCon(proxy, null);
    }

    /**
     * Try to open a connection to the net.
     * <p>
     * We here use the {@linkplain URLConnection} class to open a connection
     * to a host and try to read from the returning input stream.
     * </p>
     *
     * @param uri URI
     * @return {@code true} if the connection can be made
     * @see #isConnectedToNet()
     * @see #isConnectedToNet(Proxy)
     * @see #isConnectedToNet(URI, Proxy)
     */
    public static boolean isConnectedToNet(URI uri) {
        Objects.requireNonNull(uri);

        return openCon(null, uri);
    }

    /**
     * Try to open a connection to the net.
     * <p>
     * We here use the {@linkplain URLConnection} class to open a connection
     * to a host and try to read from the returning input stream.
     * </p>
     *
     * @return {@code true} if the connection can be made
     * @throws NullPointerException if {@code uri} | {@code proxy}
     * @see #isConnectedToNet(URI)
     * @see #isConnectedToNet(Proxy)
     * @see #isConnectedToNet()
     */
    public static boolean isConnectedToNet(URI uri, Proxy proxy) {
        Objects.requireNonNull(uri);
        Objects.requireNonNull(proxy);

        return openCon(proxy, uri);
    }

    /**
     * Test the connection to the given URL with the given timeout.
     * <p>
     * Here we use the {@linkplain InetAddress} class to check the
     * {@link InetAddress#isReachable(int)} method.
     * </p>
     *
     * @param urlStr  an URL
     * @param timeout timeout [{@linkplain #MIN_TIMEOUT} .. {@linkplain Integer#MAX_VALUE}]
     * @return {@code true} if the Address given by the {@code urlStr} is reachable
     * @throws NullPointerException if {@code urlStr}
     */
    public static boolean isInetAddressReachable(final String urlStr, int timeout) {
        Objects.requireNonNull(urlStr);
        timeout = Math.max(MIN_TIMEOUT, timeout);

        // Check host
        if (!isValidURI(urlStr)) {
            LOG.warn("The URL '{}' is not valid!", urlStr);
            return false;
        }

        try {
            return InetAddress.getByName(urlStr).isReachable(timeout);
        } catch (UnknownHostException unknowHostE) {
            LOG.warn("Unknown host -> '{}'!", urlStr);
            return false;
        } catch (IOException ioE) {

            LOG.warn("Can not reach url '{}' with timeout of {}", urlStr, timeout, ioE.getCause());
            return false;
        }
    }

    /**
     * Try to connect a host.
     *
     * @param hostNameStr host name
     * @param port        port [{@link #PORT_MIN} .. {@link #PORT_MAX}]
     * @param timeout     timeout [{@link #MIN_TIMEOUT} .. {@link Integer#MAX_VALUE}]
     * @return {@code true} if the host is available
     * @throws NullPointerException if {@code hostNameStr}
     */
    public static boolean isHostAvailable(final String hostNameStr, int port, int timeout) {
        Objects.requireNonNull(hostNameStr);
        port = Math.clamp(port, PORT_MIN, PORT_MAX);
        timeout = Math.max(MIN_TIMEOUT, timeout);

        // Check host
        if (!isValidURI(hostNameStr)) {
            return false;
        }

        try (Socket socket = new Socket()) {

            InetSocketAddress socketAddress = new InetSocketAddress(hostNameStr, port);
            socket.connect(socketAddress, timeout);

            return true;
        } catch (IOException ioE) {

            LOG.warn("Can not open connection to host '{}' on port '{}' with timeout '{}'", hostNameStr, port, timeout, ioE.getCause());
            return false;
        }
    }

    /**
     * Check whether the uri is valid.
     *
     * @param uriStr URI
     * @return {@code true} if {@code uriStr} can be parsed
     * @throws NullPointerException if {@code uriStr}
     * @see URI#URI(String)
     */
    public static boolean isValidURI(String uriStr) {
        Objects.requireNonNull(uriStr);
        try {
            URI uri = new URI(uriStr);

            LOG.info("Parse URI '{}', -> {}", uriStr, uri);
            return true;
        } catch (URISyntaxException e) {

            LOG.warn("Fail to parse '{}'", uriStr);
            return false;
        }

    }

    /**
     * List all known network adapter.
     *
     * @return list of system known network adapter if there are any
     */
    public static List<NetworkInterface> getNetworkAdapter() {
        return netIF();
    }

    private static List<NetworkInterface> netIF() {

        try {
            return NetworkInterface.networkInterfaces().toList();
        } catch (SocketException sockE) {
            LOG.error("Failed to obtain NetworkIF", sockE);
            return Collections.emptyList();
        }
    }

    private static boolean openCon(Proxy proxy, URI uri) {

        if (null == uri) {
            uri = URI_IEEE;
        }


        boolean ret = false;
        LOG.info("Try to open a connection to '{}' ...", uri);

        try {

            URLConnection urlConnection;
            if (null == proxy) {
                LOG.info("Using no proxy");
                urlConnection = uri.toURL().openConnection();
            } else {
                LOG.info("Using proxy '{}'", proxy);
                urlConnection = uri.toURL().openConnection(proxy);
            }

            urlConnection.getInputStream().close();
            LOG.info("Connection ok :)");
            ret = true;
        } catch (MalformedURLException malformedURLE) {
            LOG.error("URL is malformed '{}'", uri, malformedURLE);

        } catch (SocketTimeoutException sockTOE) {
            LOG.warn("Socket Timeout!");
        } catch (IOException ioE) {

            LOG.error("Failed to open Connection to '{}'", uri, ioE);
        }
        return ret;
    }


}