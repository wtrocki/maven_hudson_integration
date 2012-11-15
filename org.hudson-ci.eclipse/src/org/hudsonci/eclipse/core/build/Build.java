package org.hudsonci.eclipse.core.build;

import java.sql.Timestamp;
import java.util.List;

/**
 * Representation of a Hudson build.
 *
 */
public class Build {

	private String number;
	private String url;
	private String status;
	private String id;
	private long timestamp;
	private List<BuildParameter> parameters;

	public Build(String number, String url, String status, String id, long timestamp, List<BuildParameter> parameters) {
		super();
		this.number = number;
		this.url = url;
		this.status = status;
		this.id = id;
		this.timestamp = timestamp;
		this.parameters = parameters;
	}

	public String getNumber() {
		return number;
	}

	public String getUrl() {
		return url;
	}

	public String getId() {
		return id;
	}

	public long getTimestampLong() {
		return timestamp;
	}

	public Timestamp getTimestamp() {
		return new Timestamp(timestamp);
	}

	public String getStatus() {
		return status;
	}

	public List<BuildParameter> getParameters() {
		return parameters;
	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((number == null) ? 0 : number.hashCode());
		result = PRIME * result + ((url == null) ? 0 : url.hashCode());
		result = PRIME * result + ((status == null) ? 0 : status.hashCode());
		result = PRIME * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Build other = (Build)obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (timestamp == 0) {
			if (other.timestamp != 0)
				return false;
		} else if (timestamp == other.timestamp)
			return false;
		return true;
	}
}
