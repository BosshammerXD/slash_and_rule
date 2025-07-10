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

    public static String getAtlas(String prefix, String name) {
        return prefix + "/" + name + "/" + name + ".atlas";
    }

    public static String getEnAtlas(String name) {
        return getAtlas("entities", name);
    }
}
