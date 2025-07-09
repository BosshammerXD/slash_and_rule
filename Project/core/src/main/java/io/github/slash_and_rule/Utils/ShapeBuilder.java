package io.github.slash_and_rule.Utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

public class ShapeBuilder {
    public static Shape circ(float radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be greater than 0.");
        }
        return new CircleShape() {
            {
                setRadius(radius);
            }
        };
    }

    public static Shape circ(float x, float y, float radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be greater than 0.");
        }
        return new CircleShape() {
            {
                setPosition(new Vector2(x, y));
                setRadius(radius);
            }
        };
    }

    public static Shape rect(float width, float height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be greater than 0.");
        }
        return new PolygonShape() {
            {
                setAsBox(width / 2, height / 2);
            }
        };
    }

    public static Shape rect(float x, float y, float width, float height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be greater than 0.");
        }
        return new PolygonShape() {
            {
                setAsBox(width / 2, height / 2, new Vector2(x, y), 0);
            }
        };
    }

    public static Shape poly(float[] vertices) {
        if (vertices == null || vertices.length < 6 || vertices.length % 2 != 0) {
            throw new IllegalArgumentException("Vertices must contain at least three points (6 values).");
        }
        return new PolygonShape() {
            {
                set(vertices);
            }
        };
    }
}
