package com.codangcoding.retainedobject

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.util.Log


class RetainedFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "RetainedObjectStores"
        private val retainedFragmentManager = RetainedFragmentManager()

        @JvmStatic
        val RETAINED_TAG = "com.codangcoding.retainobject.RetainedFragment"

        @JvmStatic
        internal fun retainedFragmentFor(activity: FragmentActivity): RetainedFragment =
                retainedFragmentManager.retainedFragmentFor(activity)

        @JvmStatic
        internal fun retainedFragmentFor(fragment: Fragment): RetainedFragment =
                retainedFragmentManager.retainedFragmentFor(fragment)
    }


    val retainedObjectStore = RetainedObjectStore()

    init {
        retainInstance = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainedFragmentManager.retainedFragmentCreated(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        retainedObjectStore.clear()
    }

    class RetainedFragmentManager {

        companion object {
            @JvmStatic
            fun findRetainedFragment(manager: FragmentManager): RetainedFragment? {
                if (manager.isDestroyed) throw IllegalStateException("Can't access RetainedObject from onDestroy")

                val fragment = manager.findFragmentByTag(RETAINED_TAG)
                if (fragment != null && fragment !is RetainedFragment)
                    throw IllegalStateException("Unexpected fragment instance was returned by RETAIN_TAG")

                return fragment as? RetainedFragment
            }

            @JvmStatic
            fun createRetainedFragment(manager: FragmentManager): RetainedFragment {
                val retainedFragment = RetainedFragment()
                manager.beginTransaction().add(retainedFragment, RETAINED_TAG).commitAllowingStateLoss()

                return retainedFragment
            }
        }

        private val notCommittedActivityMap = mutableMapOf<Activity, RetainedFragment>()
        private val notCommittedFragmentMap = mutableMapOf<Fragment, RetainedFragment>()

        private val activityCallbacks = object : Application.ActivityLifecycleCallbacks {

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                notCommittedActivityMap.remove(activity)?.let {
                    Log.e(LOG_TAG, "Failed to save a RetainedObject for $activity")
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivityCreated(activity: Activity, p1: Bundle?) {
            }
        }

        private var activityCallbackIsAdded = false

        private val parentDestroyedCallback = object : FragmentManager.FragmentLifecycleCallbacks() {

            override fun onFragmentDestroyed(fragmentMgr: FragmentManager, fragment: Fragment) {
                super.onFragmentDestroyed(fragmentMgr, fragment)
                notCommittedFragmentMap.remove(fragment)?.let {
                    Log.e(LOG_TAG, "Failed to save a RetainedObject for $fragment")
                }
            }
        }

        fun retainedFragmentCreated(fragment: Fragment) {
            val parent = fragment.parentFragment
            if (parent != null) {
                notCommittedFragmentMap.remove(parent)
                parent.fragmentManager?.unregisterFragmentLifecycleCallbacks(parentDestroyedCallback)
            } else {
                notCommittedActivityMap.remove(fragment.activity!!)
            }
        }

        fun retainedFragmentFor(activity: FragmentActivity): RetainedFragment {
            val manager = activity.supportFragmentManager
            var retainedFragment = findRetainedFragment(manager)
            if (retainedFragment != null)
                return retainedFragment

            retainedFragment = notCommittedActivityMap[activity]
            if (retainedFragment != null)
                return retainedFragment

            if (!activityCallbackIsAdded) {
                activityCallbackIsAdded = true
                activity.application.registerActivityLifecycleCallbacks(activityCallbacks)
            }
            retainedFragment = createRetainedFragment(manager)
            notCommittedActivityMap.put(activity, retainedFragment)

            return retainedFragment
        }

        fun retainedFragmentFor(fragment: Fragment): RetainedFragment {
            val manager = fragment.childFragmentManager
            var retainedFragment = findRetainedFragment(manager)
            if (retainedFragment != null)
                return retainedFragment

            retainedFragment = notCommittedFragmentMap[fragment]
            if (retainedFragment != null)
                return retainedFragment

            fragment.fragmentManager?.registerFragmentLifecycleCallbacks(parentDestroyedCallback, false)
            retainedFragment = createRetainedFragment(manager)
            notCommittedFragmentMap.put(fragment, retainedFragment)

            return retainedFragment
        }
    }
}