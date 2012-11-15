package org.hudsonci.eclipse.core.operation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.hudsonci.eclipse.core.connection.HudsonOperation;

public class CreateJobOperation extends HudsonOperation {

	public void createNewJob(String projectName) throws Exception {
			createNewHudsonProject(getBase(), projectName);
	}

	public boolean createNewHudsonProject(String baseUrl, String projectName)
			throws URISyntaxException, IOException {
		String configurationFile = "config.xml";
		final InputStream resource = HudsonOperation.class.getClassLoader().getResourceAsStream(
				configurationFile);
		if (resource == null)
			return false;

		HttpClient client = getClient(baseUrl);

		URI path = new URI(baseUrl + "/createItem?name=" + projectName);
		PostMethod post = new PostMethod(path.toString());

		RequestEntity entity = new InputStreamRequestEntity(resource,
				"text/xml; charset=UTF-8");
		post.setRequestEntity(entity);

		int result = client.executeMethod(post);
		post.releaseConnection();
		if (result > 300)
			return false;
		return true;
	}

}
