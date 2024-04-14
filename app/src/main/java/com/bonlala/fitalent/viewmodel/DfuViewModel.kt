package com.bonlala.fitalent.viewmodel


import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blala.blalable.BleOperateManager
import com.blala.blalable.listener.OnCommBackDataListener
import com.bonlala.fitalent.emu.DeviceType
import com.bonlala.fitalent.http.RequestServer
import com.bonlala.fitalent.http.api.VersionApi
import com.bonlala.fitalent.utils.GsonUtils
import com.bonlala.fitalent.utils.LanguageUtils
import com.hjq.http.EasyConfig
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnDownloadListener
import com.hjq.http.listener.OnHttpListener
import com.hjq.http.model.BodyType
import com.hjq.http.model.HttpMethod
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.util.*

/**
 * Created by Admin
 *Date 2022/9/19
 */
open class DfuViewModel : ViewModel() {

    //后台获取的固件信息
    var getServerData = MutableLiveData<VersionApi.VersionInfo?>()

    //从手表中读取的固件信息
    var deviceDfuVersion = MutableLiveData<String>()



    //W560B从手表中获取固件版本
    fun getDeviceVersion(bleOperateManager: BleOperateManager,type : Int){
        if(type == DeviceType.DEVICE_W560B){
            bleOperateManager.getDeviceVersionData(object : OnCommBackDataListener{
                override fun onIntDataBack(value: IntArray?) {

                }

                override fun onStrDataBack(vararg value: String?) {
                    Timber.e("----固件版本="+ Arrays.toString(value))
                    deviceDfuVersion.postValue(value[1])
                }

            })
        }

        //W575读取固件信息
        if(type == DeviceType.DEVICE_W575){
            bleOperateManager.readDeviceInfoMsg(object : OnCommBackDataListener{
                override fun onIntDataBack(value: IntArray?) {


                }

                override fun onStrDataBack(vararg value: String?) {
                    deviceDfuVersion.postValue(value[0])
                }

            })
        }
    }



    //W575指定的V1.00.xx版本判断
    fun getW575SpecifiedVersion(){
        val isChinese = LanguageUtils.isChinese()
        val bean = VersionApi.VersionInfo()
        bean.fileSize = 119000
        bean.versionName = "V1.00.15"
        bean.fileUrl = "https://isportcloud.oss-cn-shenzhen.aliyuncs.com/manager/W575_V1.00.15.zip"
        bean.updateMethod = 0
        bean.remark = if(isChinese) "更新已知问题" else "fix bugs"
        getServerData.postValue(bean)
    }




    //后台获取固件版本信息
    fun getServerVersionInfo(lifecycle: LifecycleOwner,type : Int){
        val requestServer = RequestServer()
        requestServer.bodyType =BodyType.FORM
        EasyConfig.getInstance().setServer(requestServer).into()
        EasyHttp.get(lifecycle).api(VersionApi().setVersion(if(type == DeviceType.DEVICE_W560B) "W560B" else "W575",1)).request(object : OnHttpListener<String>{
            override fun onSucceed(result: String?) {
                val jsonObject = JSONObject(result)
                if(jsonObject.get("code") == "200" && jsonObject.get("success") == true){
                    val dataStr = jsonObject.getString("data")
                    val verBean = GsonUtils.getGsonObject<VersionApi.VersionInfo>(dataStr)

                    Timber.e("----固件版本="+verBean.toString())
                    getServerData.postValue(verBean)
                }
            }

            override fun onFail(e: Exception?) {
                e?.printStackTrace()
            }

        })

    }

    //下载固件信息
    fun startDownloadDfu(context: Context,lifecycle: LifecycleOwner,downUrl : String,saveFileUrl : String){
        EasyHttp.download(lifecycle).method(HttpMethod.GET).url(downUrl).file("$saveFileUrl/w560b_dfu.bin").listener(object :
            OnDownloadListener{
            override fun onStart(file: File?) {

            }

            override fun onProgress(file: File?, progress: Int) {

            }

            override fun onComplete(file: File?) {

            }

            override fun onError(file: File?, e: Exception?) {

            }

            override fun onEnd(file: File?) {

            }

        })
    }
}