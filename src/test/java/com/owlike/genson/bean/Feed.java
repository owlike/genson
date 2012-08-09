package com.owlike.genson.bean;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.SerializedName;

public class Feed {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alternates == null) ? 0 : alternates.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + (int) (updated ^ (updated >>> 32));
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
		if (!(obj instanceof Feed)) {
			return false;
		}
		Feed other = (Feed) obj;
		if (alternates == null) {
			if (other.alternates != null) {
				return false;
			}
		} else if (!alternates.equals(other.alternates)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (items == null) {
			if (other.items != null) {
				return false;
			}
		} else if (!items.equals(other.items)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (updated != other.updated) {
			return false;
		}
		return true;
	}

	@JsonProperty
	String id;
	@JsonProperty
	String title;
	@JsonProperty
	String description;
	@com.owlike.genson.annotation.JsonProperty("alternate")
	@SerializedName("alternate")
	@JsonProperty("alternate")
	List<Link> alternates;
	@JsonProperty
	long updated;
	@JsonProperty
	List<Item> items;

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder().append(id).append("\n").append(title)
				.append("\n").append(description).append("\n").append(alternates).append("\n")
				.append(updated);
		int i = 1;
		for (Item item : items) {
			result.append(i++).append(": ").append(item).append("\n\n");
		}
		return result.toString();
	}
}