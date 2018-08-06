package com.github.edarke.literalcomments;

import com.intellij.ide.util.PropertiesComponent;

interface SettingAccessor {

  String DEFAULT_COMMENT_FORMAT = "/* %s= */";
  String COMMENT_FORMAT_KEY = "DEFAULT_COMMENT_FORMAT";

  default String getCommentFormat() {
    return PropertiesComponent.getInstance().getValue(COMMENT_FORMAT_KEY, DEFAULT_COMMENT_FORMAT);
  }

  default void setCommentFormat(String format){
    format = format.trim();
    String testFormat = String.format(format, "test_param"); // assert valid
    if (testFormat.startsWith("/*") && testFormat.endsWith("*/")){
      PropertiesComponent.getInstance().setValue(COMMENT_FORMAT_KEY, format);
    }
  }
}
