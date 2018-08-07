// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.edarke.literalcomments;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static com.github.edarke.literalcomments.Test.*;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "Guava",
    "OptionalUsedAsFieldOrParameterType", "unused", "deprecation", "ResultOfMethodCallIgnored",
    "UseCompareMethod", "CachedNumberConstructorCall", "RedundantTypeArguments"})
public class TestIgnore {

  static {
    "Hello, World!".charAt(0);
    "Test".substring(1);
    "TEST".substring(0, 3);
    ImmutableSet.of(1, 2, 3, 4 /* e4*/, 5);
    ImmutableMap.of(0, 1, 2+1, 3);
    new Integer(5).equals(5);
    Math.cos(1);
    java.lang.Math.pow(2, 8);
    new Integer(10).compareTo(10);
    new HashMap<Integer, Integer>().get(5);
    new HashMap<Integer, Integer>().put(5, 2);
    String.format("%d %d", 1, 2);
    Collections.singletonList(5);
    Integer.valueOf(5);
    Iterables.get(ImmutableList.of(1, 2, 3), 0);

    Optional.of(3).orElse(1);
    guavaAbsentOptional_shouldHaveComments(com.google.common.base.Optional.of(5), com.google.common.base.Optional.<Integer>of(5));
    javaEmptyOptional_shouldHaveComments(Optional.of(5), Optional.<Integer>of(5));

    stringLiteral_shouldBeIgnored("hello world", "");
    stringLiteral_shouldBeIgnored(/* comment */ "", "");
    nonnullObject_shouldBeIgnored(new Object(), new Object());
    nonnullObject_shouldBeIgnored(/* o= */ null, /* five= */ 5);
    createArray(1, 2, 3, 4);
    testChar('a', 'b');
  }

  public static boolean testChar(char a, char b) {
    return a < b;
  }

  public static Object[] createArray(Object... vars) {
    return vars;
  }
}

