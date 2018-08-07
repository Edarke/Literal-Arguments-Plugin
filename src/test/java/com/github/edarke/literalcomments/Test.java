package com.github.edarke.literalcomments;

import java.util.Optional;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "Guava",
    "OptionalUsedAsFieldOrParameterType", "unused", "ResultOfMethodCallIgnored",
    "OptionalAssignedToNull", "ConstantConditions"})
public class Test {

    static {
        nonnullObject_shouldBeIgnored(null, null);
        stringLiteral_shouldBeIgnored(null, null);


        primitives_shouldHaveComments(0, (short) 0   /*shorter*/, 0f, 0.0, 0L, false, (byte) 0);

        javaEmptyOptional_shouldHaveComments(Optional.empty(), Optional.<String>empty());
        javaEmptyOptional_shouldHaveComments(java.util.Optional.<String>empty(), java.util.Optional.empty());
        javaEmptyOptional_shouldHaveComments(null, null);


        guavaAbsentOptional_shouldHaveComments(com.google.common.base.Optional.absent(), com.google.common.base.Optional.<String>absent());
        guavaAbsentOptional_shouldHaveComments(null, null);
    }

    public static Object nonnullObject_shouldBeIgnored(Object o1, Object o2) {
        return o1;
    }

    public static String stringLiteral_shouldBeIgnored(String a, String arg) {
        return arg;
    }

    public static String primitives_shouldHaveComments(int integer, short shorter, float floater, double doubler, long longer, boolean bool, byte bite) {
        return "" + integer + shorter + floater + doubler + longer + bool + bite;
    }

    public static <T> T javaEmptyOptional_shouldHaveComments(Optional<T> opt, Optional<T> opt2) {
        return opt.orElse(null);
    }

    public static <T> T guavaAbsentOptional_shouldHaveComments(com.google.common.base.Optional<T> opt, com.google.common.base.Optional<T> opt2) {
        return opt.orNull();
    }


}
