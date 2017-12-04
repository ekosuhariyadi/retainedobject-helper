package com.codangcoding.retainedobject

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity


class RetainedObjectStore {

    private val map = mutableMapOf<String, RetainedObject>()

    fun put(key: String, retainedObject: RetainedObject) {
        map[key]?.onCleared()
        map[key] = retainedObject
    }

    fun get(key: String): RetainedObject? = map[key]

    fun clear() {
        map.values.forEach { it.onCleared() }
        map.clear()
    }
}

object RetainedObjectStores {

    /*
    * Should be called on MainThread
    */
    @JvmStatic
    fun of(activity: FragmentActivity): RetainedObjectStore =
            RetainedFragment.Companion.retainedFragmentFor(activity).retainedObjectStore

    /*
    * Should be called on MainThread
    */
    @JvmStatic
    fun of(fragment: Fragment): RetainedObjectStore =
            RetainedFragment.Companion.retainedFragmentFor(fragment).retainedObjectStore
}

interface RetainedObjectStoreOwner {

    fun getRetainedObjectStore(): RetainedObjectStore
}