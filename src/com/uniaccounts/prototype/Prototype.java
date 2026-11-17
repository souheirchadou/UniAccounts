package com.uniaccounts.prototype;

/**
 * PATTERN: Prototype
 * ROLE: Prototype (interface) - an object that knows how to copy itself.
 */
public interface Prototype<T> {
    T copy();
}
