package com.codangcoding.daggerretainedobject

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector


interface Injectable

fun Application.registerDagger2InjectionCallback() {

    fun handleActivity(activity: Activity) {
        if (activity is Injectable || activity is HasSupportFragmentInjector) {
            AndroidInjection.inject(activity)
        }

        if (activity is FragmentActivity) {
            activity.supportFragmentManager
                    .registerFragmentLifecycleCallbacks(
                            object : FragmentManager.FragmentLifecycleCallbacks() {

                                override fun onFragmentCreated(fm: FragmentManager?, fragment: Fragment, savedInstanceState: Bundle?) {
                                    if (fragment is Injectable) {
                                        AndroidSupportInjection.inject(fragment)
                                    }
                                }
                            },
                            true
                    )
        }
    }

    this.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle?) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivityCreated(activity: Activity, p1: Bundle?) {
            handleActivity(activity)
        }
    })
}
