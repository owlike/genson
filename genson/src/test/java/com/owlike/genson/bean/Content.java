package com.owlike.genson.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Content {
  @JsonProperty
  String content;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((content == null) ? 0 : content.hashCode());
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
    if (!(obj instanceof Content)) {
      return false;
    }
    Content other = (Content) obj;
    if (content == null) {
      if (other.content != null) {
        return false;
      }
    } else if (!content.equals(other.content)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return content;
  }
}