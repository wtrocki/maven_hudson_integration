package org.hudsonci.eclipse.core.connection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.jface.preference.IPreferenceStore;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.build.Build;
import org.hudsonci.eclipse.core.build.BuildHealth;
import org.hudsonci.eclipse.core.build.BuildParameter;
import org.hudsonci.eclipse.core.build.BuildStatus;
import org.hudsonci.eclipse.core.job.Job;
import org.hudsonci.eclipse.core.ui.JobView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class for accessing the Hudson server.
 * 
 */
public class HudsonOperation {
	private static final Logger log = Logger.getLogger(HudsonOperation.class);
	
	private static final Job[] EMPTY = new Job[0];

	private IPreferenceStore preferenceStore;
	
	public HudsonOperation() {
	  preferenceStore = HudsonPlugin.getDefault().getPreferenceStore();
	}
	
	protected String getBase() {
		String b = preferenceStore.getString(HudsonPlugin.PREF_BASE_URL);
		log.debug("Base url: " + b);
		if (b == null || "".equals(b.trim())) {
			return null;
		} else {
			return b;
		}
	}
	
	public Job[] getJobs() throws IOException {
		return getJobs(getBase());
	}
	
	public Job[] getJobs(String viewUrl) throws IOException {
		HttpClient client = getClient(viewUrl);
		if (client == null) return EMPTY;
		GetMethod method = new GetMethod(getRelativePath(viewUrl) + "api/xml");

		try {
			client.executeMethod(method);
			InputStream is = method.getResponseBodyAsStream();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			is.close();

			Element root = doc.getDocumentElement();
			NodeList jobNodes = root.getElementsByTagName("job");

			Job[] res = new Job[jobNodes.getLength()];
			for (int i = 0; i < res.length; i++) {
				Element jobNode = (Element) jobNodes.item(i);

				String name = getNodeValue(jobNode, "name");
				String last = getNodeValue(jobNode, "lastBuild");
				if (last == null) {
					// we're probably in a newer version of hudson, so we get the build number separately
					res[i] = getJob(name, client);
				} else {
					Build build = new Build(last, null, null, null, 0, null);
					String url = getNodeValue(jobNode, "url");

					res[i] = new Job(name, url, build, BuildStatus.getStatus(getNodeValue(jobNode, "color")), null, null);
				}
			}

			return res;
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	public JobView[] getViews() throws IOException{
		HttpClient client = getClient(getBase());
		if (client == null) return new JobView[0];

		GetMethod method = new GetMethod(getRelativePath(getBase()) + "api/xml");
		try {
			client.executeMethod(method);
			InputStream is = method.getResponseBodyAsStream();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			is.close();

			Element root = doc.getDocumentElement();
			NodeList viewNodes = root.getElementsByTagName("view");

			JobView[] res = new JobView[viewNodes.getLength()];
			for (int i = 0; i < res.length; i++) {
				Element jobNode = (Element) viewNodes.item(i);

				String name = getNodeValue(jobNode, "name");
				String url = getNodeValue(jobNode, "url").replaceAll(" ", "%20");
				res[i] = new JobView(name, url);
			}

			return res;
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	private Job getJob(String name, HttpClient client) throws IOException, SAXException, ParserConfigurationException {
		log.debug("Getting job info for " + name);
		GetMethod method = new GetMethod(getRelativePath(getBase()) + "job/" + encode(name) + "/api/xml?depth=0");
		try {
			client.executeMethod(method);
			InputStream bodyStream = method.getResponseBodyAsStream();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bodyStream);
			bodyStream.close();
			
			Element jobNode = doc.getDocumentElement();

			String jobUrl = getNodeValue(jobNode, "url");
			String jobStatus = getNodeValue(jobNode, "color");

			String lastBuildNumber = getNodeValue(getChild("lastBuild", jobNode), "number");
			String lastBuildUrl = getNodeValue(getChild("lastBuild", jobNode), "url");
			
			List<String> healthScore = getHealthScore(jobNode);
			
			BuildHealth health = BuildHealth.getLowest(healthScore);


			List<BuildParameter> defaultParameters = getDefaultParameters(name, client);

			Build lastBuild = null;
			if (lastBuildNumber != null) {
				lastBuild = getLastBuild(name, lastBuildNumber, lastBuildUrl, client);
			} else {
				log.debug("ERROR: last build not available for Job '" + name + "'");
			}
			return new Job(name, jobUrl, lastBuild, BuildStatus.getStatus(jobStatus), health, defaultParameters);
		} finally {
			method.releaseConnection();
		}
	}

	private List<BuildParameter> getDefaultParameters(String name, HttpClient client) throws IOException, SAXException, ParserConfigurationException {
		log.debug("Getting deafult parameter info for " + name);
		GetMethod method = new GetMethod(getRelativePath(getBase()) + "job/" + encode(name) + "/api/xml");
		try {
			client.executeMethod(method);
			InputStream bodyStream = method.getResponseBodyAsStream();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bodyStream);
			bodyStream.close();

			List<BuildParameter> parameters = new ArrayList<BuildParameter>();

			Element root = doc.getDocumentElement();
			NodeList actionNodes = root.getElementsByTagName("action");

			for (int i = 0; i < actionNodes.getLength(); i++) {
				Element actionNode = (Element) actionNodes.item(i);
				NodeList parameterNodes = actionNode.getElementsByTagName("parameterDefinition");
				for (int j = 0; j < parameterNodes.getLength(); j++) {
					Element parameterNode = (Element) parameterNodes.item(j);
					Element valueNode = (Element) parameterNode.getElementsByTagName("defaultParameterValue").item(0);
					parameters.add(new BuildParameter(getNodeValue(parameterNode, "name"),getNodeValue(valueNode, "value"),getNodeValue(parameterNode, "description")));
				}
			}
			return parameters;
		} finally {
			method.releaseConnection();
		}
	}
	
	private Build getLastBuild(String name, String number, String url, HttpClient client) throws IOException, SAXException, ParserConfigurationException {
		log.debug("Getting build info for Job '" + name + "' and Build '" + number + "'");
		GetMethod method = new GetMethod(getRelativePath(getBase()) + "job/" + encode(name) + "/" + number + "/api/xml");
		try {
			client.executeMethod(method);
			InputStream bodyStream = method.getResponseBodyAsStream();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bodyStream);
			bodyStream.close();

			Element root = doc.getDocumentElement();

			String status = getNodeValue(root, "status");
			String id = getNodeValue(root, "id");
			String timestamp = getNodeValue(root, "timestamp");

			List<BuildParameter> parameters = getLastBuildParameters(name, number, root, client);

			return new Build(number, url, status, id, Long.valueOf(timestamp).longValue(), parameters);
		} finally {
			method.releaseConnection();
		}
	}

	private List<BuildParameter> getLastBuildParameters(String name, String number, Element root, HttpClient client) throws IOException, SAXException, ParserConfigurationException {
		log.debug("Getting parameter info for Job '" + name + "' and Build '" + number + "'");
		List<BuildParameter> parameters = new ArrayList<BuildParameter>();

		NodeList actionNodes = root.getElementsByTagName("action");

		for (int i = 0; i < actionNodes.getLength(); i++) {
			Element actionNode = (Element) actionNodes.item(i);
			NodeList parameterNodes = actionNode.getElementsByTagName("parameter");
			for (int j = 0; j < parameterNodes.getLength(); j++) {
				Element parameterNode = (Element) parameterNodes.item(j);
				parameters.add(new BuildParameter(getNodeValue(parameterNode, "name"),getNodeValue(parameterNode, "value")));
			}
		}
		return parameters;
	}

	private List<String> getHealthScore(Element jobNode) {
		List<String> healthScore = new ArrayList<String>();
		NodeList nodes = jobNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if ("healthReport".equals(n.getNodeName())) {
				healthScore.add(getNodeValue((Element) n, "score"));
			}
		}

		return healthScore;
	}

	private Element getChild(String name, Element parent) {
		NodeList nodes = parent.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (name.equals(n.getNodeName())) {
				return (Element) n;
			}
		}
		return null;
	}
	
	private String encode(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void scheduleJob(String project, List<BuildParameter> parameters) throws IOException, ParametersRequiredException {
		HttpClient client = getClient(getBase());
		String st = preferenceStore.getString(HudsonPlugin.PREF_SECURITY_TOKEN + "_" + project);

		List<NameValuePair> query = new ArrayList<NameValuePair>();
		if (st != null && st.length() > 0) {
			query.add(new NameValuePair("token", st));
		}
		

		String path = "/build";
		if (parameters != null && parameters.size() > 0) {
			path += "WithParameters";
			
			for (BuildParameter p : parameters) {
				if (p.getName() != null && !"".equals(p.getName().trim())) {
					query.add(new NameValuePair(p.getName(), p.getValue()));
				}
			}
		}

		GetMethod method = new GetMethod(getRelativePath(getBase()) + "job/" + encode(project) + path);
		if (query.size() > 0) {
			method.setQueryString(query.toArray(new NameValuePair[query.size()]));
		}

		try {
			int res = client.executeMethod(method);
			log.debug("Build schedule result: " + res);
			if (res == HttpStatus.SC_FORBIDDEN) {
				throw new IOException("Scheduling failed, security token required");
			} else if (res == HttpStatus.SC_METHOD_NOT_ALLOWED) {
				throw new ParametersRequiredException();
			}
			method.getResponseBodyAsStream().close();
		} finally {
			method.releaseConnection();
		}
	}

	public void scheduleJob(String project) throws IOException, ParametersRequiredException {
		scheduleJob(project, null);
	}

	public void checkValidUrl(String base, boolean authEnabled, String username, String password) throws Exception {

		HttpClient client = getClient(base, authEnabled, username, password);
		GetMethod method = new GetMethod(getRelativePath(base) + "api/xml");
		try {
			int res = client.executeMethod(client.getHostConfiguration(), method);
			if (res == HttpStatus.SC_NOT_FOUND) {
				throw new IllegalArgumentException("No content found at " + method.getPath());
			} else if (res == HttpStatus.SC_UNAUTHORIZED) {
				throw new IllegalArgumentException("Basic authentication required for " + method.getPath());
			}
			InputStream bodyStream = method.getResponseBodyAsStream();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bodyStream);
			bodyStream.close();

			Element root = doc.getDocumentElement();
			if (root.getChildNodes().getLength() == 0 || !root.getNodeName().equals("hudson")) {
				throw new IllegalArgumentException("URL does not point to a valid Hudson installation. /api/xml does not return correct data.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			method.releaseConnection();
		}
	}

	private String getNodeValue(Element node, String name) {
		if (node == null) return null;
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (name.equals(n.getNodeName())) {
				return n.getTextContent().trim();
			}
		}
		return null;
	}

	protected HttpClient getClient(String base) throws IOException {
		return getClient(base, preferenceStore.getBoolean(HudsonPlugin.PREF_USE_AUTH), preferenceStore.getString(HudsonPlugin.PREF_LOGIN), preferenceStore.getString(HudsonPlugin.PREF_PASSWORD));
	}

	private HttpClient getClient(String base, boolean authEnabled, String username, String password) throws IOException {
		if (base == null) return null;
		
		try {
			HttpClient client = new HttpClient();
			String type;
			URL u = new URL(base);
			int port = u.getPort();
			if (u.getProtocol().equalsIgnoreCase("https")) {
				if (port == -1) {
					port = 443;
				}
				type = IProxyData.HTTPS_PROXY_TYPE;
				client.getHostConfiguration().setHost(u.getHost(), port, new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), 443));
			} else {
				if (port == -1) {
					port = 80;
				}
				type = IProxyData.HTTP_PROXY_TYPE;
				client.getHostConfiguration().setHost(u.getHost(), port);
			}
			IProxyData proxyData = HudsonPlugin.getDefault().getProxyService().getProxyDataForHost(u.getHost(), type);
			if (proxyData != null) {
				client.getHostConfiguration().setProxy(proxyData.getHost(), proxyData.getPort());
				if (proxyData.isRequiresAuthentication()) {
					client.getState().setProxyCredentials(new AuthScope(proxyData.getHost(), proxyData.getPort()),
							new UsernamePasswordCredentials(proxyData.getUserId(), proxyData.getPassword()));
				}
			}
			client.getParams().setConnectionManagerTimeout(1000);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(2000);
			client.getParams().setSoTimeout(3000);
			
			//submits a GET to the security servlet with user and password as parameters
			if (authEnabled) {
				log.debug("Auth is enabled, username: " + username);
				GetMethod getMethod = new GetMethod(getRelativePath(base) + "j_acegi_security_check");
				getMethod.setQueryString("j_username=" + username + "&j_password="+password);
				int res = client.executeMethod(getMethod);
				getMethod.releaseConnection();
				if (res == HttpStatus.SC_NOT_FOUND) {
					getMethod = new GetMethod(getRelativePath(base) + "j_security_check");
					getMethod.setQueryString("j_username=" + username + "&j_password="+password);
					res = client.executeMethod(getMethod);
				} else if (res == HttpStatus.SC_UNAUTHORIZED) {
					client.getParams().setAuthenticationPreemptive(true);
					client.getState().setCredentials(new AuthScope(u.getHost(), port), new UsernamePasswordCredentials(username, password));
				}
				log.debug("Login result for " + getMethod.getURI() + ": " + res);
			}

			return client;
		} catch (MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
	}

	private String getRelativePath(String url) {
		int pos = url.indexOf('/', 8);
		if (pos == -1) {
			return "/";
		} else {
			String path = url.substring(pos);
			if (!path.endsWith("/")) {
				path += "/";
			}
			return path;
		}
	}
	
	public void createJob(String name,String location) throws IOException {
		String viewUrl="http://localhost:8080/";
		HttpClient client = new HttpClient();
		URL u = new URL(viewUrl);
		int port = u.getPort();
		client.getHostConfiguration().setHost(u.getHost(), port);
		
		client.getParams().setConnectionManagerTimeout(1000);
		client.getHttpConnectionManager().getParams().setConnectionTimeout(2000);
		client.getParams().setSoTimeout(3000);
		if (client == null) return;
		String string = viewUrl + "createItem?name="+name;
		System.out.println(string);
		PostMethod method = new PostMethod(string);
		method.addRequestHeader(new Header("ContentType",
		"text/plain;charset=UTF-8"));

		int executeMethod = client.executeMethod(method);
		System.out.print(method.getPath()+
				method.getResponseBodyAsString());
		method.releaseConnection();
	}

}