package io.github.slash_and_rule.Utils;

import java.util.Iterator;
import java.util.function.Function;

public class QuadData<T> implements Iterable<T> {
    public T left;
    public T down;
    public T right;
    public T up;

    public QuadData(T left, T down, T right, T up) {
        this.left = left;
        this.down = down;
        this.right = right;
        this.up = up;
    }

    public QuadData() {
        this.left = null;
        this.down = null;
        this.right = null;
        this.up = null;
    }

    /**
     * Returns the element at the specified index.
     * 
     * @param index the index of the element to return (0 for left, 1 for down, 2
     *              for right, 3 for up)
     * @return the element at the specified index
     * @throws IndexOutOfBoundsException if the index is not between 0 and
     */
    public T get(int index) {
        switch (index) {
            case 0:
                return left;
            case 1:
                return down;
            case 2:
                return right;
            case 3:
                return up;
            default:
                throw new IndexOutOfBoundsException("Index must be between 0 and 3");
        }
    }

    /**
     * Sets the element at the specified index.
     *
     * @param index the index of the element to set (0 for left, 1 for down, 2 for
     *              right, 3 for up)
     * @param value the value to set at the specified index
     * @throws IndexOutOfBoundsException if the index is not between 0 and 3
     */
    public void set(int index, T value) {
        switch (index) {
            case 0:
                left = value;
                break;
            case 1:
                down = value;
                break;
            case 2:
                right = value;
                break;
            case 3:
                up = value;
                break;
            default:
                throw new IndexOutOfBoundsException("Index must be between 0 and 3");
        }
    }

    /**
     * Clears the element at the specified index by setting it to null.
     *
     * @param index the index of the element to clear (0 for left, 1 for down, 2
     *              for right, 3 for up)
     * @throws IndexOutOfBoundsException if the index is not between 0 and 3
     */
    public void clear(int index) {
        switch (index) {
            case 0:
                left = null;
                break;
            case 1:
                down = null;
                break;
            case 2:
                right = null;
                break;
            case 3:
                up = null;
                break;
            default:
                throw new IndexOutOfBoundsException("Index must be between 0 and 3");
        }
    }

    /**
     * Checks if the element at the specified index is set (not null).
     *
     * @param index the index to check (0 for left, 1 for down, 2 for right, 3 for
     *              up)
     * @return true if the element at the specified index is set, false otherwise
     * @throws IndexOutOfBoundsException if the index is not between 0 and 3
     */
    public boolean isSet(int index) {
        switch (index) {
            case 0:
                return left != null;
            case 1:
                return down != null;
            case 2:
                return right != null;
            case 3:
                return up != null;
            default:
                throw new IndexOutOfBoundsException("Index must be between 0 and 3");
        }
    }

    /**
     * Returns a shallow copy of this QuadData instance.
     *
     * @return a new QuadData instance with the same values
     */
    public QuadData<T> copy() {
        return new QuadData<>(left, down, right, up);
    }

    /**
     * Applies the given function to each element in this QuadData instance.
     *
     * @param consumer the function to apply to each element
     */
    public void map(Function<T, T> consumer) {
        left = consumer.apply(left);
        down = consumer.apply(down);
        right = consumer.apply(right);
        up = consumer.apply(up);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < 4;
            }

            @Override
            public T next() {
                switch (index++) {
                    case 0:
                        return left;
                    case 1:
                        return down;
                    case 2:
                        return right;
                    case 3:
                        return up;
                    default:
                        throw new IndexOutOfBoundsException("No more elements in QuadData");
                }
            }
        };
    }

    @Override
    public String toString() {
        return "QuadData{" +
                "left=" + left +
                ", down=" + down +
                ", right=" + right +
                ", up=" + up +
                '}';
    }

    public String toString(Function<T, String> toStringFunction) {
        return "QuadData{" +
                "left=" + ((left == null) ? "null" : toStringFunction.apply(left)) +
                ", down=" + ((down == null) ? "null" : toStringFunction.apply(down)) +
                ", right=" + ((right == null) ? "null" : toStringFunction.apply(right)) +
                ", up=" + ((up == null) ? "null" : toStringFunction.apply(up)) +
                '}';
    }
}
