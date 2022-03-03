package com.example.myapplication.json

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import java.io.IOException
import java.io.InputStream

class JsonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_json)

        val jsonFileString = getJsonFromAssets(applicationContext, "readjson.json")
        Log.i("data", jsonFileString!!)

//        val gson = Gson()
//        val listUserType: Type = object : TypeToken<List<UserData?>?>() {}.type
//
//        val users: UserData = gson.fromJson(jsonFileString, listUserType)
//        for (i in users.indices) {
//            Log.i(
//                "data", """
//     > Item $i
//     ${users[i]}
//     """.trimIndent()
//            )
//        }
    }

    private fun getJsonFromAssets(context: Context, fileName: String?): String? {

        val jsonString = try {
            val iss: InputStream = context.assets.open(fileName!!)
            val size: Int = iss.available()
            val buffer = ByteArray(size)
            iss.read(buffer)
            iss.close()
            String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return jsonString
    }


}

