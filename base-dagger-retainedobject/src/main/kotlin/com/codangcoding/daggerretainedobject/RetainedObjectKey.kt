package com.codangcoding.daggerretainedobject

import com.codangcoding.retainedobject.RetainedObject
import dagger.MapKey
import kotlin.reflect.KClass


@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class RetainedObjectKey(val value: KClass<out RetainedObject>)