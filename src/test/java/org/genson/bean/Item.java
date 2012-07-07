package org.genson.bean;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.SerializedName;

public class Item {
	@JsonProperty
	List<String> categories;
	@JsonProperty
	String title;
	@JsonProperty
	long published;
	@JsonProperty
	long updated;
	@org.genson.annotation.JsonProperty("alternate")
	@SerializedName("alternate")
	@JsonProperty("alternate")
	List<Link> alternates;
	@JsonProperty
	Content content;
	@JsonProperty
	String author;
	@JsonProperty
	List<ReaderUser> likingUsers;

	@Override
	public String toString() {
		return title + "\nauthor: " + author + "\npublished: " + published + "\nupdated: "
				+ updated + "\n" + content + "\nliking users: " + likingUsers
				+ "\nalternates: " + alternates + "\ncategories: " + categories;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alternates == null) ? 0 : alternates.hashCode());
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((categories == null) ? 0 : categories.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((likingUsers == null) ? 0 : likingUsers.hashCode());
		result = prime * result + (int) (published ^ (published >>> 32));
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
		if (!(obj instanceof Item)) {
			return false;
		}
		Item other = (Item) obj;
		if (alternates == null) {
			if (other.alternates != null) {
				return false;
			}
		} else if (!alternates.equals(other.alternates)) {
			return false;
		}
		if (author == null) {
			if (other.author != null) {
				return false;
			}
		} else if (!author.equals(other.author)) {
			return false;
		}
		if (categories == null) {
			if (other.categories != null) {
				return false;
			}
		} else if (!categories.equals(other.categories)) {
			return false;
		}
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!content.equals(other.content)) {
			return false;
		}
		if (likingUsers == null) {
			if (other.likingUsers != null) {
				return false;
			}
		} else if (!likingUsers.equals(other.likingUsers)) {
			return false;
		}
		if (published != other.published) {
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
}