package cn.tellyouwhat.gradeteller.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import cn.tellyouwhat.gradeteller.R
import cn.tellyouwhat.gradeteller.util.NetWorkUtils
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import android.annotation.SuppressLint
import org.json.JSONArray


/**
 * Created by harbo on 2018/1/18.
 * Email: harbourzeng@gmail.com
 */
class MainActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!NetWorkUtils.isNetworkConnected(applicationContext)) {
            Toast.makeText(applicationContext, "没联网唉╮(￣▽￣╮", Toast.LENGTH_LONG)
                    .show()
            return
        }

        grades_tv.text = "loading"

        val preferences = getSharedPreferences("info", Context.MODE_PRIVATE)
        val token = preferences.getString("token", "")
        if (token.isNullOrBlank()) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
            return
        }

        GetAllGradesTask().execute(token)

    }

    inner class GetAllGradesTask : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            loading_grades_pb.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String {
            val httpClient = OkHttpClient()
            val request = Request.Builder()
                    .url("http://ydjw.nwu.edu.cn/university-facade/MyUniversity/MyGrades?" +
                            "token=" + params[0])
                    .addHeader("Content-Type", "application/json")
                    .build()
            val response = httpClient.newCall(request).execute()
            return response.body()!!.string()
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {

            val jsonObject = JSONObject(result)
            if (jsonObject["state"] as Int != 200) {
                loading_grades_pb.visibility = View.INVISIBLE
                Toast.makeText(applicationContext, "身份失效", Toast.LENGTH_LONG)
                        .show()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                return
            }
            val data = jsonObject.getJSONArray("data")
            grades_tv.text = ""
            for (i in 0 until data.length()) {
                val aSemester = data.getJSONObject(i)
                val schoolYear = aSemester["xn"] as String
                val semesterNumber = aSemester["xq"] as String
                val grades = aSemester["items"] as JSONArray
                grades_tv.append("-----------\n学年：$schoolYear，学期：$semesterNumber\n")
                for (g in 0 until grades.length()) {
                    grades_tv.append(grades.getJSONObject(g)["kcmc"] as CharSequence)
                    grades_tv.append(": ")
                    grades_tv.append(grades.getJSONObject(g)["cj"] as CharSequence)
                    grades_tv.append("\n")
                }
            }
            loading_grades_pb.visibility = View.INVISIBLE
        }

    }
}