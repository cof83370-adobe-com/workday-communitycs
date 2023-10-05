package com.workday.community.aem.core.utils.cache;

/**
 * Defines an interface for value callbacks.
 *
 * @param <V> The value type.
 */
public interface ValueCallback<V> {

  V getValue(String key);

}
