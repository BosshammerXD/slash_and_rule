package io.github.slash_and_rule;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class MyPacker {
    public static void main(String[] args) {
        TexturePacker.process("Project/assets/entities/BasicSlimeRaw", "Project/assets/entities/BasicSlime",
                "BasicSlime");
    }
}
