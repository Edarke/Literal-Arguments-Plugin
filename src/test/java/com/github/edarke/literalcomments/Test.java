package com.github.edarke.literalcomments;

import java.util.Optional;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "Guava",
        "OptionalUsedAsFieldOrParameterType", "unused", "ResultOfMethodCallIgnored"})
public class Test {

    static {
        nonnullObject_shouldBeIgnored(null);
        stringLiteral_shouldBeIgnored(null);


        primitives_shouldHaveComments(0, (short) 0, 0f, 0.0, 0L, false, (byte) 0, '0');

        javaEmptyOptional_shouldHaveComments(Optional.empty());
        javaEmptyOptional_shouldHaveComments(Optional.<String>empty());
        javaEmptyOptional_shouldHaveComments(java.util.Optional.<String>empty());
        javaEmptyOptional_shouldHaveComments(java.util.Optional.empty());
        javaEmptyOptional_shouldHaveComments(null);


        guavaAbsentOptional_shouldHaveComments(com.google.common.base.Optional.absent());
        guavaAbsentOptional_shouldHaveComments(com.google.common.base.Optional.<String>absent());
        guavaAbsentOptional_shouldHaveComments(null);
    }

    public static Object nonnullObject_shouldBeIgnored(Object o) {
        return o;
    }

    public static String stringLiteral_shouldBeIgnored(String arg) {
        return arg;
    }

    public static String primitives_shouldHaveComments(int integer, short shorter, float floater, double doubler, long longer, boolean bool, byte bite, char character) {
        return "" + integer + shorter + floater + doubler + longer + bool + bite + character;
    }

    public static <T> T javaEmptyOptional_shouldHaveComments(Optional<T> opt) {
        return opt.orElse(null);
    }

    public static <T> T guavaAbsentOptional_shouldHaveComments(com.google.common.base.Optional<T> opt) {
        return opt.orNull();
    }


}
