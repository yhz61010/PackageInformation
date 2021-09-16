package com.leovp.android.packageinformation.ui.main

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.leovp.android.packageinformation.R
import com.leovp.android.packageinformation.utils.beans.PackageInfoBean

public val TAB_TITLES = arrayOf(
    R.string.tab_name_1,
    R.string.tab_name_2
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragmentList: MutableList<PlaceholderFragment> = ArrayList()

    companion object {
        private val TAG = SectionsPagerAdapter::class.java.simpleName
    }

    override fun getItem(position: Int): Fragment {
        Log.i(TAG, "getItem $position")
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        val fragment = PlaceholderFragment.newInstance(position + 1)
        fragmentList.add(fragment)
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        Log.i(TAG, "getPageTitle $position")
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        Log.i(TAG, "getCount ${TAB_TITLES.size}")
        return TAB_TITLES.size
    }

    fun updateTabData(userApps: MutableList<PackageInfoBean>, systemApps: MutableList<PackageInfoBean>) {
        fragmentList[0].adapter.appList = userApps
        fragmentList[0].adapter.notifyDataSetChanged()


        fragmentList[1].adapter.appList = systemApps
        fragmentList[1].adapter.notifyDataSetChanged()
    }
}