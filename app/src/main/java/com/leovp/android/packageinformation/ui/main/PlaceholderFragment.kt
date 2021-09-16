package com.leovp.android.packageinformation.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.leovp.android.packageinformation.adapter.AppRecyclerAdapter
import com.leovp.android.packageinformation.utils.beans.PackageInfoBean


/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var adapter: AppRecyclerAdapter
    var userApps: MutableList<PackageInfoBean> = ArrayList()
    var systemApps: MutableList<PackageInfoBean> = ArrayList()

    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(
            com.leovp.android.packageinformation.R.layout.fragment_tab,
            container,
            false
        )
        initView(root, arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        return root
    }

    companion object {
        private val TAG = PlaceholderFragment::class.java.simpleName

        const val TAB_USER_APP = 1
        const val TAB_SYSTEM_APP = 2

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private fun initView(v: View, tabIndex: Int) {
        linearLayoutManager = LinearLayoutManager(activity)

        val listView =
            v.findViewById<RecyclerView>(com.leovp.android.packageinformation.R.id.listView)
        listView.layoutManager = linearLayoutManager

        listView.itemAnimator = DefaultItemAnimator()

        listView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

        adapter = AppRecyclerAdapter(if (tabIndex == TAB_USER_APP) userApps else systemApps)
        listView.adapter = adapter
    }

    fun getUserAppsCount() = userApps.size

    fun getSystemAppsCount() = systemApps.size

//    private fun receiveAppData(app: PackageInfoBean) {
//        runOnUiThread {
//            allApps.add(app)
//            adapter.notifyItemInserted(allApps.size - 1)
//        }
//    }
}