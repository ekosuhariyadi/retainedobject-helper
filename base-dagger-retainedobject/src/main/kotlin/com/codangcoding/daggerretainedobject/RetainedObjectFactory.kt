package com.codangcoding.daggerretainedobject

import com.codangcoding.retainedobject.RetainedObject
import com.codangcoding.retainedobject.RetainedObjectProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton


@Singleton
class RetainedObjectFactory @Inject constructor(
        private val creators: MutableMap<Class<out RetainedObject>, Provider<RetainedObject>>
) : RetainedObjectProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : RetainedObject> create(clazz: Class<T>): T {
        val creator = creators.getOrElse(clazz) {
            creators.entries.firstOrNull { clazz.isAssignableFrom(it.key) }?.value
        } ?: throw IllegalArgumentException("Unknown model class $clazz")

        try {
            return creator.get() as T
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }
    }
}