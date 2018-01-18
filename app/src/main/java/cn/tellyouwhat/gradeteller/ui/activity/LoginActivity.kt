package cn.tellyouwhat.gradeteller.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast
import cn.tellyouwhat.gradeteller.R
import cn.tellyouwhat.gradeteller.util.MD5Util
import cn.tellyouwhat.gradeteller.util.NetWorkUtils
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

/**
 * Created by harbo on 2018/1/18.
 * Email: harbourzeng@gmail.com
 */
class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginClicked(view: View) {
        if (!NetWorkUtils.isNetworkConnected(applicationContext)){
            Toast.makeText(applicationContext, "没联网唉╮(￣▽￣╮", Toast.LENGTH_LONG)
                    .show()
            return
        }
        if (inputCheck()){
            return
        }
        LoginTask().execute(mapOf(Pair("username", username.text.toString()),
                Pair("password",
                        MD5Util.getMd5StringWithSaltSimple(
                                password.text.toString(), "murp"))))
    }

    /**
     * @return true if something wrong, false otherwise
     */
    private fun inputCheck(): Boolean{
        if (username.text.length != 10){
            username.error = "学号不是10位"
            username.requestFocus()
            return true
        }
        if (password.text.isNullOrBlank()){
            password.error = "密码长度有误"
            password.requestFocus()
            return true
        }
        return false
    }

    private fun showProgressBar(showOrNot: Boolean) {
        login_button.isClickable = !showOrNot
        password.isClickable = !showOrNot
        username.isClickable = !showOrNot
        login_progress.visibility = if (showOrNot) View.VISIBLE else View.INVISIBLE
        login_bg.visibility = if (showOrNot) View.VISIBLE else View.INVISIBLE
        val alphaAnimation = AlphaAnimation(if (showOrNot) 0f else 1f, if (showOrNot) 1f else 0f)
        alphaAnimation.duration = 500
        login_bg.startAnimation(alphaAnimation)
    }

    inner class LoginTask : AsyncTask<Map<String, String>, Void, String>() {
        override fun onPreExecute() {
            showProgressBar(true)
        }

        override fun doInBackground(vararg params: Map<String, String>): String {
            val data = JSONObject()
                    .put("u", params[0]["username"])
                    .put("tec", "android:7.1.2")
                    .put("p", params[0]["password"])
                    .put("type", 110)
                    .put("ver", 214)
                    .put("uuid", "")
                    .toString()

            val httpClient = OkHttpClient()
            val request = Request.Builder()
                    .post(RequestBody.create(
                            MediaType.parse("application/json;charset=utf-8"), data))
                    .url("http://ydjw.nwu.edu.cn/university-facade/Murp/Login")
                    .header("Content-Type", "application/json;charset=utf-8")
                    .build()

            val response = httpClient.newCall(request).execute()
            return if (response.isSuccessful) {
                response.body()!!.string()
            } else {
                ""
            }
        }

        override fun onPostExecute(result: String?) {
            if (result.isNullOrBlank()) {
                showProgressBar(false)
                return
            }

            val jsonObject = JSONObject(result)
            if (jsonObject["state"] as Int == 2002) {
                password.error = "学号密码不匹配"
                password.requestFocus()
                showProgressBar(false)
                return
            }

            val data = JSONObject(jsonObject["data"].toString())
            val token = data["token"] as String

            getSharedPreferences("info", Context.MODE_PRIVATE).edit()
                    .putString("token", token)
                    .putString("username", username.text.toString())
                    .putString("password", password.text.toString())
                    .apply()

            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

    }
}