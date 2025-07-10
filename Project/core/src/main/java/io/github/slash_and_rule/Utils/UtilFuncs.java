package io.github.slash_and_rule.Utils;

public final class UtilFuncs {

    public static String[] getDirs(String prefix) {
        return new String[] {
            prefix + "Left",
            prefix + "Down",
            prefix + "Right",
            prefix + "Up"
        };
    }
}
