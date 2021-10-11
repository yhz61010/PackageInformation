package com.leovp.android.packageinformation.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.leovp.android.packageinformation.CustomApplication
import com.leovp.android.packageinformation.R
import com.leovp.android.packageinformation.utils.APKUtil
import com.leovp.android.packageinformation.utils.DeviceUtil
import com.leovp.android.packageinformation.utils.FileUtil
import com.leovp.android.packageinformation.utils.beans.PackageInfoBean
import com.leovp.android.packageinformation.utils.inflate
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Author: Michael Leo
 * Date: 19-5-20 下午2:46
 */
class AppRecyclerAdapter(var appList: MutableList<PackageInfoBean>) :
    RecyclerView.Adapter<AppRecyclerAdapter.AppHolder>() {

    companion object {
        val TAG = AppRecyclerAdapter::class.java.simpleName
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        val inflatedView = parent.inflate(R.layout.list_item_for_app, false)
        return AppHolder(inflatedView)
    }

    override fun getItemCount() = appList.size

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        val itemApp = appList[position]
        holder.bindData(itemApp)
    }

    class AppHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private lateinit var app: PackageInfoBean

        init {
            v.setOnClickListener(this)
        }

        // Item click
        override fun onClick(v: View) {
            val dialog = MaterialDialog(view.context).show {
                icon(drawable = app.appIcon)
                title(text = app.appName)
                customView(viewRes = R.layout.dlg_app_info, scrollable = true)
                cornerRadius(16f)

                positiveButton(R.string.dlg_positive_button) { dialog ->
                    Toast.makeText(CustomApplication.instance, "Do COPY", Toast.LENGTH_SHORT).show()
                }
                negativeButton(R.string.dlg_negative_button) {
                    try {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.setClassName(app.appPackage, app.launchActivity)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        CustomApplication.instance.startActivity(i)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            CustomApplication.instance,
                            "Launch failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                neutralButton(R.string.dlg_neutral_button) { dialog ->
                    //                    MaterialDialog(dialog.context).show {
//                        listItems(R.array.action_more) { dialog, index, text ->
//                            // Invoked when the user taps an item
//                        }
//                    }

//                    val popupLayout = layoutInflater.inflate(layout.component_more_action_popup, null)  //加载的布局
//                    val popupContentListView = popupLayout.findViewById<ListView>(id.listView)
                    val popupContentListView = ListView(dialog.context)
                    // Attention, the order of these two lines below can not be changed.
                    // Otherwise, the divider will not be shown.
                    popupContentListView.divider = ColorDrawable(Color.GRAY)
                    popupContentListView.dividerHeight = 1
                    popupContentListView.layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    popupContentListView.onItemClickListener =
                        AdapterView.OnItemClickListener { parent, view, position, id ->
                            //                            val text = parent.adapter.getItem(position).toString()
//                            Toast.makeText(dialog.context, text, Toast.LENGTH_SHORT).show()
                            when (position) {
                                0 -> { // Uninstall App
                                    val uri = Uri.parse("package:${app.appPackage}")
                                    val intent = Intent(Intent.ACTION_DELETE, uri)
                                    context.startActivity(intent)
                                }
                                1 -> { // App detail
                                    val intent = Intent()
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                                    intent.data = Uri.fromParts("package", app.appPackage, null)
                                    context.startActivity(intent)
                                }
                                2 -> { // Backup App
                                    backupApp(dialog)
                                }
                                3 -> { // Export App
                                    exportApp(dialog)
                                }
                                4 -> { // Export App Icon
                                    exportAppIcon(dialog)
                                }
                                5 -> { // Share
                                    val intent = Intent(Intent.ACTION_SEND)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.type = "*/*"
                                    intent.putExtra(
                                        Intent.EXTRA_STREAM,
                                        FileUtil.getUriForFile(File(app.sourceDir))
                                    )
                                    context.startActivity(intent)
                                }
                                else -> Toast.makeText(
                                    dialog.context,
                                    "Invalid operation",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    val popupContentAdapter = ArrayAdapter(
                        dialog.context,
                        android.R.layout.simple_list_item_1,
                        CustomApplication.instance.resources.getStringArray(R.array.action_more)
                    )

                    popupContentListView.adapter = popupContentAdapter

                    val popup = PopupWindow(
                        popupContentListView,
                        DeviceUtil.dip2px(150F),
                        DeviceUtil.dip2px(290F),
                        true
                    )
                    with(popup) {
                        //                        width = ViewGroup.LayoutParams.WRAP_CONTENT;
//                        height = ViewGroup.LayoutParams.WRAP_CONTENT;
//                        setBackgroundDrawable(ColorDrawable(Color.LTGRAY))
                        setBackgroundDrawable(ColorDrawable(Color.rgb(0xEE, 0xEE, 0xEE)))
                        isOutsideTouchable = true
                        isTouchable = true
                        isFocusable = true
//                    showAtLocation(getActionButton(WhichButton.NEUTRAL), Gravity.TOP, 0, 0)
                        showAsDropDown(getActionButton(WhichButton.NEUTRAL), Gravity.TOP, 0, 0)
                    }
                    dialog.noAutoDismiss()
                }
            }

            val customView = dialog.getCustomView()
            customView.findViewById<TextView>(R.id.tvPackageName).text = app.appPackage
//            customView.tvPackageName.paintFlags = customView.tvPackageName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            customView.findViewById<TextView>(R.id.tvLaunchActivity).text = app.launchActivity
            customView.findViewById<TextView>(R.id.tvPackageName).text = app.appPackage
            customView.findViewById<TextView>(R.id.tvSize).text = APKUtil.readableFileSize(app.appSize)
            customView.findViewById<TextView>(R.id.tvVersion).text = app.appVersion
            customView.findViewById<TextView>(R.id.tvVersionCode).text = app.appVersionCode.toString()
            customView.findViewById<TextView>(R.id.tvSha1).text = app.sha1
            customView.findViewById<TextView>(R.id.tvMd5).text = app.md5
            customView.findViewById<TextView>(R.id.tvInstallDate).text = SDF.format(app.installedDate)
            customView.findViewById<TextView>(R.id.tvLastUpdateDate).text = SDF.format(app.lastUpdateDate)

//            dialog.show()
        }

        @SuppressLint("CheckResult")
        private fun exportAppIcon(dialog: MaterialDialog) {
            io.reactivex.Observable.create(ObservableOnSubscribe<File> { emitter ->
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (app.appIcon is AdaptiveIconDrawable) {
                        val bitmap = Bitmap.createBitmap(
                            app.appIcon.intrinsicWidth,
                            app.appIcon.intrinsicHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        app.appIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                        app.appIcon.draw(canvas)
                        bitmap
                    } else {
                        (app.appIcon as BitmapDrawable).bitmap
                    }
                } else {
                    (app.appIcon as BitmapDrawable).bitmap
                }


                emitter.onNext(
                    FileUtil.exportAppIconToSdCard(
                        bitmap,
                        "${app.appName}-v${app.appVersion}-${app.appPackage}.png"
                    )
                )
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(dialog.context, it.absolutePath, Toast.LENGTH_SHORT).show()
                }
        }

        @SuppressLint("CheckResult")
        private fun exportApp(dialog: MaterialDialog) {
            io.reactivex.Observable.create(ObservableOnSubscribe<File> { emitter ->
                emitter.onNext(
                    FileUtil.copyFileToSdCard(
                        File(app.sourceDir),
                        FileUtil.FOLDER_TYPE.APP,
                        "${app.appName}-v${app.appVersion}-${app.appPackage}.apk"
                    )
                )
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(dialog.context, it.absolutePath, Toast.LENGTH_SHORT).show()
                }
        }

        @SuppressLint("CheckResult")
        private fun backupApp(dialog: MaterialDialog) {
            io.reactivex.Observable.create(ObservableOnSubscribe<File> { emitter ->
                emitter.onNext(
                    FileUtil.copyFileToSdCard(
                        File(app.sourceDir),
                        FileUtil.FOLDER_TYPE.BAK,
                        "${app.appName}-v${app.appVersion}-${app.appPackage}.bak"
                    )
                )
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(dialog.context, it.absolutePath, Toast.LENGTH_SHORT).show()
                }
        }

        companion object {
            val APP_KEY = "APP"
            val SDF = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        }

        fun bindData(app: PackageInfoBean) {
            this.app = app
            view.findViewById<ImageView>(R.id.ivAppIcon).setImageDrawable(app.appIcon)
            view.findViewById<TextView>(R.id.tvAppName).text = app.appName
            view.findViewById<TextView>(R.id.tvAppPackage).text = app.appPackage
            view.findViewById<TextView>(R.id.tvVersion).text = app.appVersion
            view.findViewById<TextView>(R.id.tvAppSize).text = APKUtil.readableFileSize(app.appSize)
        }
    }
}

