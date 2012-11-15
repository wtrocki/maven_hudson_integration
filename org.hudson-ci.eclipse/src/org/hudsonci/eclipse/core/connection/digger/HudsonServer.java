package org.hudsonci.eclipse.core.connection.digger;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Just a simple bean to store the information from the XML returned by Hudson.
 * 
 * 
 */
public class HudsonServer {
	private String version;
	private String url;
	private String slavePort;

	private HudsonServer() {

	}

	public static HudsonServer fromXML(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		HudsonServer hudsonServer = new HudsonServer();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder
				.parse(new InputSource(new StringReader(xml)));
		Element hudsonEl = document.getDocumentElement();
		NodeList versionEl = hudsonEl.getElementsByTagName("version");
		NodeList urlEl = hudsonEl.getElementsByTagName("url");
		NodeList slavePortEl = hudsonEl.getElementsByTagName("slave-port");

		hudsonServer.version = versionEl.item(0).getTextContent();
		hudsonServer.url = urlEl.item(0).getTextContent();
		hudsonServer.slavePort = slavePortEl.item(0).getTextContent();

		return hudsonServer;
	}

	public String getSlavePort() {
		return slavePort;
	}

	public String getUrl() {
		return url;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return "Url: \t\t" + url + "\nVersion: \t" + version
				+ "\nSlavePort: \t" + slavePort;
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = true;
		if (obj instanceof HudsonServer) {
			HudsonServer hudsonServer = (HudsonServer) obj;
			if (null != url) {
				result = result && url.equals(hudsonServer.url);
			} else {
				result = result && (null == hudsonServer.url);
			}
		} else {
			result = false;
		}
		return result;
	}
}
