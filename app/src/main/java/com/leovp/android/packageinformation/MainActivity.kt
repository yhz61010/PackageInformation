package com.leovp.android.packageinformation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.tabs.TabLayout
import com.leovp.android.packageinformation.ui.main.SectionsPagerAdapter
import com.leovp.android.packageinformation.utils.CustomDelegate
import com.leovp.android.packageinformation.utils.PackageInfoUtil
import com.leovp.android.packageinformation.utils.beans.PackageInfoBean
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var selectedOrderResId by CustomDelegate.preference("selectedOrderResId", R.id.rbByName)
    private var descOrder by CustomDelegate.preference("descOrder", false)

    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter

    private var userApps: MutableList<PackageInfoBean> = ArrayList()
    private var systemApps: MutableList<PackageInfoBean> = ArrayList()

    private lateinit var mTab: TabLayout
    private lateinit var viewAPager: ViewPager
    private lateinit var progress: ProgressBar
    private lateinit var toolbar: Toolbar

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setToolbarMenu()

        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        viewAPager = findViewById(R.id.view_pager)
        viewAPager.adapter = sectionsPagerAdapter
        mTab = findViewById(R.id.tabs)
        mTab.setupWithViewPager(viewAPager)

        progress = findViewById(R.id.progress)
        progress.visibility = View.VISIBLE

        val observable = Observable.create(ObservableOnSubscribe<List<PackageInfoBean>> { emitter ->
            Log.i(TAG, "subscribe()")
            lifecycleScope.launch(Dispatchers.Main) {
                emitter.onNext(initData())
            }
        })

        val consumer = Consumer<List<PackageInfoBean>> {
            Log.i(TAG, "accept()")
            toolbar.title = "${toolbar.title}(${userApps.size + systemApps.size})"
            sectionsPagerAdapter.updateTabData(userApps, systemApps)
            mTab.getTabAt(0)!!.text = "${mTab.getTabAt(0)!!.text}(${userApps.size})"
            mTab.getTabAt(1)!!.text = "${mTab.getTabAt(1)!!.text}(${systemApps.size})"
            progress.visibility = View.GONE
        }

        observable.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(consumer)

        Log.i(TAG, "onCreate - End")

//        tabs.getTabAt(TAB_USER_APP - 1)!!.text =
//            getString(TAB_TITLES[TAB_USER_APP - 1]) + sectionsPagerAdapter.fragmentList[TAB_USER_APP - 1].getUserAppsCount()
//        tabs.getTabAt(TAB_SYSTEM_APP - 1)!!.text =
//            getString(TAB_TITLES[TAB_SYSTEM_APP - 1]) + sectionsPagerAdapter.fragmentList[TAB_SYSTEM_APP - 1].getSystemAppsCount()
    }

    private fun setToolbarMenu() {
        toolbar.inflateMenu(R.menu.main_menu)
//        toolbar.overflowIcon = resources.getDrawable(R.drawable.abc_ic_menu_moreoverflow_mtrl_alpha)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_batch_copy_package_name -> {
                }
                R.id.menu_sort -> {
                    showSortDialog()
                }
                R.id.menu_restore_bak -> {
                }
                R.id.menu_about -> {
                }
            }
            false
        }
    }

    private fun showSortDialog() {
        MaterialDialog(this).show {
            icon(res = R.mipmap.ic_launcher)
            title(res = R.string.menu_sort)
            customView(viewRes = R.layout.dlg_sort, scrollable = true)
            cornerRadius(16f)

            onPreShow { dialog ->
                val customView = dialog.getCustomView()
                customView.findViewById<CheckBox>(R.id.chkDesc).isChecked = descOrder
                customView.findViewById<RadioGroup>(R.id.rgSort).check(selectedOrderResId)
            }
            onDismiss { dialog ->
                val customView = dialog.getCustomView()
                descOrder = customView.findViewById<CheckBox>(R.id.chkDesc).isChecked
                selectedOrderResId = customView.findViewById<RadioGroup>(R.id.rgSort).checkedRadioButtonId
            }

            positiveButton(android.R.string.ok) { dialog ->
                val customView = dialog.getCustomView()
                val desc = customView.findViewById<CheckBox>(R.id.chkDesc).isChecked
                doSort(customView.findViewById<RadioGroup>(R.id.rgSort).checkedRadioButtonId, desc)
                sectionsPagerAdapter.updateTabData(userApps, systemApps)
            }
            negativeButton(android.R.string.cancel) {
                dismiss()
            }
        }
    }

    private fun doSort(sortResId: Int, desc: Boolean) {
        when (sortResId) {
            R.id.rbByName -> {
                userApps.sortWith(compareBy(PackageInfoBean::appName, PackageInfoBean::appPackage))
                systemApps.sortWith(
                    compareBy(
                        PackageInfoBean::appName,
                        PackageInfoBean::appPackage
                    )
                )
            }
            R.id.rbBySize -> {
                userApps.sortWith(compareBy(PackageInfoBean::appSize, PackageInfoBean::appPackage))
                systemApps.sortWith(
                    compareBy(
                        PackageInfoBean::appSize,
                        PackageInfoBean::appPackage
                    )
                )
            }
            R.id.rbByPermission -> {
                userApps.sortWith(compareBy(PackageInfoBean::appSize, PackageInfoBean::appPackage))
                systemApps.sortWith(
                    compareBy(
                        PackageInfoBean::appSize,
                        PackageInfoBean::appPackage
                    )
                )
            }
            R.id.rbByDate -> {
                userApps.sortWith(
                    compareBy(
                        PackageInfoBean::installedDate,
                        PackageInfoBean::appPackage
                    )
                )
                systemApps.sortWith(
                    compareBy(
                        PackageInfoBean::installedDate,
                        PackageInfoBean::appPackage
                    )
                )
            }
        }

        if (desc) {
            userApps.reverse()
            systemApps.reverse()
        }
    }

    private suspend fun initData(): List<PackageInfoBean> {
        val allApps = PackageInfoUtil.getAllInstalledApp(this)
        for (app in allApps) {
            Log.d(TAG, "System App: ${app}")
//            Log.d(TAG, "Launch Activity : ${app.launchActivity}")
//            Log.d(TAG, "===============================================================")

            if (app.isSystemApp) {
                systemApps.add(app)
                systemApps.sortWith(
                    compareBy(
                        PackageInfoBean::appName,
                        PackageInfoBean::appPackage
                    )
                )
            } else {
                userApps.add(app)
                userApps.sortWith(compareBy(PackageInfoBean::appName, PackageInfoBean::appPackage))
            }
        }

        doSort(selectedOrderResId, descOrder)

        return allApps
    }

    override fun onBackPressed() {
        Log.i(TAG, "onBackPressed()")
//        super.onBackPressed()
        moveTaskToBack(true)
    }
}
