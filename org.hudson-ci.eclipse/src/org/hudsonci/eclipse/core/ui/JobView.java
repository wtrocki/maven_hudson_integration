package org.hudsonci.eclipse.core.ui;

public class JobView {
	private final String name;
	private final String url;
	
	public JobView(String name, String url) {
		this.name = name;
		this.url = url;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUrl() {
		return url;
	}
}
