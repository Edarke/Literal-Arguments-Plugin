package com.github.edarke.literalcomments;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static com.github.edarke.literalcomments.Test.*;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "Guava",
        "OptionalUsedAsFieldOrParameterType", "unused", "deprecation", "ResultOfMethodCallIgnored",
        "UseCompareMethod", "CachedNumberConstructorCall"})
public class TestIgnore {

    static {
        "Hello, World!".charAt(0);
        "Test".substring(1);
        "TEST".substring(0, 3);
        ImmutableSet.of(1, 2, 3, 4, 5);
        ImmutableMap.of(0, 1, 2, 3);
        new Integer(5).equals(5);
        Math.cos(1);
        java.lang.Math.pow(2, 8);
        new Integer(10).compareTo(10);
        new HashMap<Integer, Integer>().get(5);
        new HashMap<Integer, Integer>().put(5, 2);
        String.format("%d %d", 1, 2);
        Collections.singletonList(5);
        Integer.valueOf(5);

        Optional.of(3);
        guavaAbsentOptional_shouldHaveComments(com.google.common.base.Optional.of(5));
        javaEmptyOptional_shouldHaveComments(Optional.of(5));


        stringLiteral_shouldBeIgnored("hello world");
        stringLiteral_shouldBeIgnored(/* comment */ "");
        nonnullObject_shouldBeIgnored(new Object());
        nonnullObject_shouldBeIgnored(/* o= */ null);
    }

}

