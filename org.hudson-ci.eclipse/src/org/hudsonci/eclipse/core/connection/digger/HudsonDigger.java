package org.hudsonci.eclipse.core.connection.digger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * This class digs the intranet for running Hudson instances. It does so by
 * sending a broadcast datagram to port {@link HudsonDigger#HUDSON_PORT}.
 * 
 * All found Hudson instances will be cached and can be accessed by
 * {@link HudsonDigger#getServers()}.
 * 
 * Subsequent calls to {@link HudsonDigger#digForHudson()} will add any new
 * found instances to the internal cache.
 * 
 * The implementation of this class is not thread safe.
 * 
 */
public class HudsonDigger {
	private static final Logger log = Logger.getLogger(HudsonDigger.class);
	private static final int HUDSON_PORT = 33848;

	private int socketTimeout = 5000;
	private int timesRetry = 2;
	private DatagramSocket socket;

	private Set<HudsonServer> servers = new HashSet<HudsonServer>();
	private Set<DiggerListener> listeners = new HashSet<DiggerListener>();

	private boolean canceled;

	public HudsonDigger() {
		// Use defaults
	}

	public HudsonDigger(int socketTimeout, int timesRetry) {
		this.socketTimeout = socketTimeout;
		this.timesRetry = timesRetry;
	}

	/**
	 * Start digging the intranet.
	 * 
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public synchronized void digForHudson() throws IOException, ParserConfigurationException, SAXException {
		this.canceled = false;
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

		socket = new DatagramSocket();
		try {
			for (NetworkInterface netint : Collections.list(nets)) {
				if (canceled) {
					return;
				}
				brodcastOnInterface(netint);
			}
		} finally {
			socket.close();
		}
	}

	public void stopDigging() {
		this.canceled = true;
	}

	private void brodcastOnInterface(NetworkInterface netint) throws IOException, ParserConfigurationException, SAXException {
		displayInterfaceInformation(netint);

		if (netint.isLoopback() || !netint.isUp()) {
			log.debug("Ignoring " + netint);
			return;
		}

		for (InterfaceAddress interfaceAddress : netint.getInterfaceAddresses()) {
			if (canceled) {
				return;
			}
			log.debug("Broadcasting using interface " + interfaceAddress);
			broadcastOnAddress(interfaceAddress);
		}
	}

	private void broadcastOnAddress(InterfaceAddress interfaceAddress) throws IOException, ParserConfigurationException, SAXException {
		InetAddress broadCastAddress = interfaceAddress.getBroadcast();
		if (broadCastAddress == null) {
			log.debug("Skipping interface " + interfaceAddress + ", no broadcast address");
			return;
		}
		DatagramPacket sDatagram = new DatagramPacket(new byte[] {}, 0, broadCastAddress, HUDSON_PORT);
		log.debug("Sending datagram to " + broadCastAddress + " ...");
		socket.send(sDatagram);

		byte buffer[] = new byte[2048];
		DatagramPacket rDatagram = new DatagramPacket(buffer, buffer.length, broadCastAddress, HUDSON_PORT);

		socket.setSoTimeout(socketTimeout);

		int timeouts = 0;
		while (timeouts < timesRetry) {
			try {
				if (canceled) {
					return;
				}
				log.debug("Waiting for response ...");
				socket.receive(rDatagram);
				processHudsonReponse(rDatagram);
				timeouts = 0;
				rDatagram.setLength(buffer.length);
			} catch (SocketTimeoutException e) {
				log.debug("Got timout ...");
				timeouts++;
			}
		}
	}

	private void processHudsonReponse(DatagramPacket rDatagram) throws IOException, ParserConfigurationException, SAXException {
		InetAddress hAddress = rDatagram.getAddress();
		int hPort = rDatagram.getPort();
		byte[] hData = rDatagram.getData();
		int hLength = rDatagram.getLength();
		int hOffset = rDatagram.getOffset();

		ByteArrayInputStream in = new ByteArrayInputStream(hData, hOffset, hLength);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		log.debug("From address" + hAddress);
		log.debug("From port " + hPort);

		String line = null;
		String hudsonResponse = "";
		while (null != (line = reader.readLine())) {
			log.debug(line);
			hudsonResponse += line;
		}
		log.debug("Got response XML: \n" + hudsonResponse);
		servers.add(HudsonServer.fromXML(hudsonResponse));
		fireServersFound();
	}

	private void fireServersFound() {
		DiggerListener[] listeners = this.listeners.toArray(new DiggerListener[this.listeners.size()]);
		for (DiggerListener l : listeners) {
			l.serversFound(servers);
		}
	}

	private void displayInterfaceInformation(NetworkInterface netint) {
		log.debug("Interface: " + netint);
	}

	/**
	 * All servers found so far. This is a cumulative list.
	 * 
	 * @return The list of found and chached servers.
	 */
	public Set<HudsonServer> getServers() {
		return servers;
	}
	
	public void addListener(DiggerListener listener) {
		listeners.add(listener);
	}
}
