package com.github.edarke.literalcomments;

enum Type {
  METHOD("Magic value passed to a method without a comment."),
  CONSTRUCTOR("Magic value passed to a constructor without a comment.");

  public final String description;

  Type(String description) {
    this.description = description;
  }
}