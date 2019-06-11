package com.github.naixx.reader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

fun log(m: Any?) {
    Log.d("log", m?.toString())
}

data class Span(val a: Int, val b: Int, val c: Int, val s: String? = null)
data class Chapter(val text: String, val spans: List<Span>)

data class Book(
    val chapters: List<Chapter>,
    val indexes: List<Map<String, IntArray>>
)

class MainActivity : AppCompatActivity() {

    var book: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.text).setOnClickListener {


            val texts = assets.list("Text").filter { it.endsWith(".html") }
                .map { assets.open("Text/$it").readBytes().toString(Charset.defaultCharset()) }
            val spans = assets.list("Text").filter { it.endsWith(".html.spans") }
                .map { assets.open("Text/$it").readBytes().toString(Charset.defaultCharset()) }.map {
                    val base = JSONArray(it)
                    List(base.length()) {
                        val element = base.getJSONArray(it)
                        Span(element.getInt(0), element.getInt(1), element.getInt(2), element.optString(3))
                    }
                }
            log(spans)

            val indexes = assets.list("Index").filter { it.contains("index") }
                .map { assets.open("Index/$it").readBytes().toString(Charset.defaultCharset()) }.map {
                    val index = mutableMapOf<String, IntArray>()
                    val base = JSONObject(it)
                    base.keys().forEach { key ->
                        val values = base.getJSONArray(key)
                        index[key] = IntArray(values.length()) { values.getInt(it) }
                    }
                    index
                }
            log(indexes)

            book = Book(texts.zip(spans) { t, s -> Chapter(t, s) }, indexes)
        }
    }
}
