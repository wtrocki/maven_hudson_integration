package org.hudsonci.eclipse.core.job;

import java.util.List;

import org.hudsonci.eclipse.core.build.Build;
import org.hudsonci.eclipse.core.build.BuildHealth;
import org.hudsonci.eclipse.core.build.BuildParameter;
import org.hudsonci.eclipse.core.build.BuildStatus;

/**
 * Representation of a Hudson job.
 * 
 */
public class Job {
	
	private String name;
	private String url;
	private Build lastBuild;
	private BuildStatus status;
	private final BuildHealth health;
	private List<BuildParameter> defaultParameters;

	public Job(String name, String url, Build lastBuild, BuildStatus status, BuildHealth health, List<BuildParameter> defaultParameters) {
		super();
		this.name = name;
		this.url = url;
		this.lastBuild = lastBuild;
		this.status = status;
		this.health = health;
		this.defaultParameters = defaultParameters;
	}
	public BuildStatus getStatus() {
		return status;
	}
	public String getName() {
		return name;
	}
	public String getUrl() {
		return url;
	}
	public Build getLastBuild() {
		return lastBuild;
	}
	public BuildHealth getHealth() {
		return health;
	}

	public List<BuildParameter> getDefaultParameters() {
		return defaultParameters;
	}
	public void setDefaultParameters(List<BuildParameter> defaultParameters) {
		this.defaultParameters = defaultParameters;
	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((status == null) ? 0 : status.hashCode());
		result = PRIME * result + ((lastBuild == null) ? 0 : lastBuild.hashCode());
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + ((url == null) ? 0 : url.hashCode());
		result = PRIME * result + ((health == null) ? 0 : health.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Job other = (Job) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (lastBuild == null) {
			if (other.lastBuild != null)
				return false;
		} else if (!lastBuild.equals(other.lastBuild))
			return false;
		if (health == null) {
			if (other.health != null)
				return false;
		} else if (!health.equals(other.health))
			return false;
		return true;
	}
}
