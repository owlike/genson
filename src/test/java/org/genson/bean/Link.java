package org.genson.bean;

import org.codehaus.jackson.annotate.JsonProperty;

public class Link {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((href == null) ? 0 : href.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Link)) {
			return false;
		}
		Link other = (Link) obj;
		if (href == null) {
			if (other.href != null) {
				return false;
			}
		} else if (!href.equals(other.href)) {
			return false;
		}
		return true;
	}

	@JsonProperty
	String href;

	@Override
	public String toString() {
		return href;
	}
}