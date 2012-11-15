package org.hudsonci.eclipse.core.build;

import org.eclipse.swt.graphics.Image;
import org.hudsonci.eclipse.core.HudsonPlugin;


public class BuildStatus {
	
	private final boolean building;
	private final Status status;
	
	private BuildStatus(Status status, boolean building) {
		this.status = status;
		this.building = building;
	}

	public static BuildStatus getStatus(String code) {
		Status status = Status.getStatus(code);
		if (status == null) {
			throw new IllegalArgumentException("No status constant for value " + code);
		}
		boolean building = code.endsWith("_anime");
		
		return new BuildStatus(status, building);
	}
	
	public Image getImage() {
		String name = status.color;
		if (building) {
			name += "_anime";
		}
		name += ".png";
		return HudsonPlugin.getImage("icons/" + name);
	}
	
	public Status getStatus() {
		return status;
	}
	
	public enum Status {
		SUCCESS("blue"),
		FAIL("red"),
		TEST_FAIL("yellow"),
		NO_BUILD("grey"),
		DISABLED("disabled"),
		UNKNOWN("unknown");
		
		private final String color;

		private Status(String code) {
			color = code;
		}

		public static Status getStatus(String code) {
			if (code == null) {
				return null;
			}
			code = code.toLowerCase();
			if (code.endsWith("_anime")) {
				code = code.substring(0, code.indexOf("_anime"));
			}
			for (Status b : values()) {
				if (b.color.equals(code)) {
					return b;
				}
			}
			return UNKNOWN;
		}
}
}
