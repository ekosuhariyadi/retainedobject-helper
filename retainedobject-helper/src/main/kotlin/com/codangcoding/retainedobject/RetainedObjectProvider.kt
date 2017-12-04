package com.codangcoding.retainedobject

import android.app.Activity
import android.app.Application
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity


class RetainedObjectProvider(
        private val retainedObjectStore: RetainedObjectStore,
        private val factory: Factory
) {

    companion object {
        private const val DEFAULT_KEY = "com.codangcoding.retainedobject.Provider.DefaultKey"
    }

    interface Factory {

        fun <T : RetainedObject> create(clazz: Class<T>): T
    }

    constructor(owner: RetainedObjectStoreOwner, factory: Factory) : this(owner.getRetainedObjectStore(), factory)

    fun <T : RetainedObject> get(clazz: Class<T>): T {
        val canonicalName = clazz.canonicalName ?:
                throw IllegalArgumentException("Local and anonymous classes can't be RetainedObjects")

        return get("$DEFAULT_KEY:$canonicalName", clazz)
    }

    /*
    * Should be called on MainThread
    */
    @Suppress("UNCHECKED_CAST")
    fun <T : RetainedObject> get(key: String, clazz: Class<T>): T {
        var retainedObject = retainedObjectStore.get(key)

        if (clazz.isInstance(retainedObject))
            return retainedObject as T

        retainedObject = factory.create(clazz)
        retainedObjectStore.put(key, retainedObject)

        return retainedObject as T
    }

    open class NewInstanceFactory : Factory {

        override fun <T : RetainedObject> create(clazz: Class<T>): T {
            try {

                return clazz.newInstance()
            } catch (ex: InstantiationException) {
                throw RuntimeException("Can't create an instance of $clazz")
            } catch (ex: IllegalAccessException) {
                throw RuntimeException("Can't create an instance of $clazz")
            }
        }
    }
}

object RetainedObjectProviders {

    @JvmStatic
    private val defaultFactory by lazy { RetainedObjectProvider.NewInstanceFactory() }

    @JvmStatic
    private fun checkApplication(activity: Activity): Application =
            activity.application ?: throw IllegalStateException("Your activity/fragment is not yet attached to " +
                    "Application. You can't request RetainedObject before onCreate call.")

    private fun checkActivity(fragment: Fragment): Activity =
            fragment.activity ?: throw IllegalStateException("Can't create RetainedObjectProvider for detached fragment")

    /*
    * Should be called on MainThread
    */
    @JvmStatic
    @JvmOverloads
    fun of(fragment: Fragment,
           factory: RetainedObjectProvider.Factory = defaultFactory): RetainedObjectProvider {
        checkApplication(checkActivity(fragment))
        return RetainedObjectProvider(RetainedObjectStores.of(fragment), factory)
    }

    /*
    * Should be called on MainThread
    */
    @JvmStatic
    @JvmOverloads
    fun of(activity: FragmentActivity,
           factory: RetainedObjectProvider.Factory = defaultFactory): RetainedObjectProvider {
        checkApplication(activity)
        return RetainedObjectProvider(RetainedObjectStores.of(activity), factory)
    }
}