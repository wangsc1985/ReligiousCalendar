package com.wang17.religiouscalendar.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.model.AppInfo
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * @author coolszy
 * @date 2012-4-26
 * @blog http://blog.92coding.com
 */
class UpdateManager(private val mContext: Context) {
    /* 保存解析的XML信息 */
    var mHashMap: HashMap<String, String>? = null

    /* 下载保存路径 */
    private var mSavePath: String? = null

    /* 记录进度条数量 */
    private val progress = 0

    /* 是否取消更新 */
    private var isCancelUpdate = false

    /* 更新进度条 */
    private var mProgress: ProgressBar? = null
    private var mDownloadDialog: Dialog? = null
    private val mbUiThreadHandler: Handler
    private val cacheDir: String
    private val cacheFile: String
    private val mongoApiKey: String
    private val PROGRESS_MAX = 100

    /**
     * 检测软件更新
     */
    fun checkUpdate() {
        //
        Thread {
            try {
                if (isHaveNewVersion()) {
                    // 显示升级确认对话框
                    showConfirmDialog()
                }
            } catch (e: Exception) {
                Log.e("wangsc", e.message!!)
            }
        }.start()
    }

    private fun showWarningDialog(title: String, message: String?) {
        mbUiThreadHandler.post { AlertDialog.Builder(mContext).setTitle(title).setMessage(message).setPositiveButton("知道了", null).show() }
    }

    private fun showWarningDialog(message: String?) {
        showWarningDialog("", message)
    }

    //            String mongoUrl = "mongodb://wangsc:351489@ds053126.mlab.com:53126/app-manager";
//            String collctionName = "app-info";
//            //
//            String whereKey = "PackageName";
//            Object whereValue = "com.wang17.religiouscalendar";
//            String orderKey = "decade";
//            Object orderValue = 1;
//            //
//            MongoClientURI uri = new MongoClientURI(mongoUrl);
//            MongoClient client = new MongoClient(uri);
//            DB db = client.getDB(uri.getDatabase());
//            DBCollection songs = db.getCollection(collctionName);
//            //
//            BasicDBObject findQuery = new BasicDBObject(whereKey, new BasicDBObject("$gte", whereValue));
//            BasicDBObject orderBy = new BasicDBObject(orderKey, orderValue);
//
//            DBCursor docs = songs.find(findQuery).sort(orderBy);
//
//            while (docs.hasNext()) {
//                DBObject doc = docs.next();
//                return new AppInfo((String)doc.get("PackageName"), (int)doc.get("VersionCode"), (String)doc.get("VersionName"), (String)doc.get("LoadUrl"), (String)doc.get("AccessToken"));
//            }
    val appInfoFromMongoDB: AppInfo?
        get() = try {

//            String mongoUrl = "mongodb://wangsc:351489@ds053126.mlab.com:53126/app-manager";
//            String collctionName = "app-info";
//            //
//            String whereKey = "PackageName";
//            Object whereValue = "com.wang17.religiouscalendar";
//            String orderKey = "decade";
//            Object orderValue = 1;
//            //
//            MongoClientURI uri = new MongoClientURI(mongoUrl);
//            MongoClient client = new MongoClient(uri);
//            DB db = client.getDB(uri.getDatabase());
//            DBCollection songs = db.getCollection(collctionName);
//            //
//            BasicDBObject findQuery = new BasicDBObject(whereKey, new BasicDBObject("$gte", whereValue));
//            BasicDBObject orderBy = new BasicDBObject(orderKey, orderValue);
//
//            DBCursor docs = songs.find(findQuery).sort(orderBy);
//
//            while (docs.hasNext()) {
//                DBObject doc = docs.next();
//                return new AppInfo((String)doc.get("PackageName"), (int)doc.get("VersionCode"), (String)doc.get("VersionName"), (String)doc.get("LoadUrl"), (String)doc.get("AccessToken"));
//            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    /**
     * 检查软件是否有更新版本
     *
     * @return
     */
    private fun isHaveNewVersion(): Boolean {
        return try {
            serverAppInfo = appInfoFromMongoDB
            if (serverAppInfo != null && serverAppInfo!!.getVersionCode() > mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionCode) {
                true
            } else {
                serverAppInfo = null
                false
            }
        } catch (e: Exception) {
            Log.e("wangsc", e.message!!)
            false
        }
    }

    /**
     * 显示确认软件更新对话框
     */
    private fun showConfirmDialog() {
        mbUiThreadHandler.post {
            // 构造对话框
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle("软件更新")
            builder.setMessage("检测到新版本，立即更新吗？")
            // 更新
            builder.setPositiveButton("更新") { dialog, which ->
                dialog.dismiss()
                startDownload()
            }
            // 稍后更新
            builder.setNegativeButton("稍后更新") { dialog, which -> dialog.dismiss() }
            builder.show()
            //                Dialog noticeDialog = builder.create();
//                noticeDialog.show();
        }
    }

    fun startDownload() {
        var permission = ""
        var isLegal = true
        if (!_Utils.havePermission(mContext, "android.permission.ACCESS_NETWORK_STATE")) {
            permission += "网络状态权限\n"
            isLegal = false
        }
        if (!_Utils.havePermission(mContext, "android.permission.INTERNET")) {
            permission += "访问网络权限\n"
            isLegal = false
        }
        if (!_Utils.havePermission(mContext, "android.permission.WRITE_EXTERNAL_STORAGE")) {
            permission += "向SD卡写入数据权限\n"
            isLegal = false
        }
        //        if (!_Utils.havePermission(context, "android.permission.MOUNT_UNMOUNT_FILESYSTEMS")) {
//            permission += "在SD卡中创建与删除文件权限\n";
//            isLegal = false;
//        }
        if (!isLegal) {
            showWarningDialog(permission)
            return
        }
        if (!_Utils.isNetworkAvailable(mContext)) {
            //
            showWarningDialog("请先打开网络，然后再更新。")
            return
        }
        if (!_Utils.isWiFiActive(mContext)) {
            //
            mbUiThreadHandler.post {
                AlertDialog.Builder(mContext).setMessage("确认要使用移动网络下载软件吗？")
                        .setPositiveButton("确定") { dialog, which ->
                            dialog.dismiss()
                            showDownloadDialog()
                        }
                        .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
            }
            return
        }
        showDownloadDialog()
    }

    /**
     * 显示软件下载对话框
     */
    private fun showDownloadDialog() {
        mbUiThreadHandler.post {
            // 构造软件下载对话框
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle("正在升级")
            // 给下载对话框增加进度条
            val inflater = LayoutInflater.from(mContext)
            val v = inflater.inflate(R.layout.softupdate_progress, null)
            mProgress = v.findViewById<View>(R.id.update_progress) as ProgressBar
            builder.setView(v)
            builder.setOnCancelListener {
                Log.i("wangsc", "Download Dialog is Cancel...")
                isCancelUpdate = true
            }
            // 取消更新
            builder.setNegativeButton("取消升级") { dialog, which ->
                dialog.dismiss()
                isCancelUpdate = true
            }
            mDownloadDialog = builder.create()
            mDownloadDialog!!.show()
            // 下载文件
            Thread {
                try {
                    loadFromBaiduPCS()
                } catch (e: Exception) {
                    showWarningDialog(e.message)
                }
                // 取消下载对话框显示
                mDownloadDialog!!.dismiss()
            }.start()
        }
    }

    @Throws(Exception::class)
    private fun loadFromBaiduPCS() {
        mProgress!!.max = PROGRESS_MAX
        val file = File(cacheDir)
        if (!file.exists()) file.mkdir()

//        BaiduPCSClient api = new BaiduPCSClient();
//        api.setAccessToken(serverAppInfo.getAccessToken()); //mbOauth为使用Oauth得到的access_token
//        api.downloadFileFromStream(serverAppInfo.getLoadUrl(), cacheFile, new BaiduPCSStatusListener() {
//            @Override
//            public void onProgress(long l, long l1) {
//                final int value = (int) (l * PROGRESS_MAX / l1);
//
//                if (isCancelUpdate) return;
//
//                if (l >= l1) {
//                    installApk();
//                }
//                mbUiThreadHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (value <= PROGRESS_MAX)
//                            mProgress.setProgress(value);
//                    }
//                });
//            }
//        });
        mProgress!!.progress = 0
    }

    @Throws(Exception::class)
    private fun loadFromNet() {
        // 判断SD卡是否存在，并且是否具有读写权限
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            // 获得存储卡的路径
            val sdpath = Environment.getExternalStorageDirectory().toString() + "/"
            mSavePath = sdpath + "download"
            val url = URL(mHashMap!!["url"])
            // 创建连接
            val conn = url.openConnection() as HttpURLConnection
            conn.connect()
            // 获取文件大小
            val length = conn.contentLength
            // 创建输入流
            val `is` = conn.inputStream
            val file = File(mSavePath)
            // 判断文件目录是否存在
            if (!file.exists()) {
                file.mkdir()
            }
            val apkFile = File(mSavePath, mHashMap!!["name"])
            val fos = FileOutputStream(apkFile)
            var count = 0
            // 缓存
            val buf = ByteArray(1024)
            // 写入到文件中
            do {
                val numread = `is`.read(buf)
                count += numread
                val value = (count.toFloat() / length * PROGRESS_MAX).toInt()
                // 计算进度条位置
                mbUiThreadHandler.post { mProgress!!.progress = value }

                // 更新进度
//                    mHandler.sendEmptyMessage(DOWNLOAD);
                if (numread <= 0) {
                    // 下载完成
                    mbUiThreadHandler.post { installApk() }
                    break
                }
                // 写入文件
                fos.write(buf, 0, numread)
            } while (!isCancelUpdate) // 点击取消就停止下载.
            fos.close()
            `is`.close()
        }
        // 取消下载对话框显示
        mDownloadDialog!!.dismiss()
    }

    /**
     * 安装APK文件
     */
    private fun installApk() {
        val apkfile = File(cacheFile)
        if (!apkfile.exists()) {
            return
        }
        // 通过Intent安装APK文件
        val i = Intent(Intent.ACTION_VIEW)
        i.setDataAndType(Uri.parse("file://$apkfile"), "application/vnd.android.package-archive")
        mContext.startActivity(i)
    }

    companion object {
        private var serverAppInfo: AppInfo? = null
        val isUpdate: Boolean
            get() = serverAppInfo != null
    }

    //    private String baiduAccessToken;
    init {
        mbUiThreadHandler = Handler()
        isCancelUpdate = false
        cacheDir = Environment.getExternalStorageDirectory().toString() + "/download"
        cacheFile = "$cacheDir/寿康宝鉴日历.apk"
        mongoApiKey = "7s7lwu2FGxvf7ezVUWpjuR4xMGYqSok3"

//        baiduAccessToken = "23.49b0c9b25b4a6431ce800c7cb3839a27.2592000.1478355666.1649802760-1641135";
    }
}