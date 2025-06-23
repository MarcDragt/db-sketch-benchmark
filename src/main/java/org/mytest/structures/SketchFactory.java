package org.mytest.structures;

public interface SketchFactory <T> {
    public T create(Object... args);
}
