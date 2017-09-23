package com.example.mmktomato.kotlin_dropbox_android_sample

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class BrowseActivity : AppCompatActivity() {
    private lateinit var dbxProxy: DbxProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)

        this.dbxProxy = DbxProxy(SharedPrefsProxy(this))
    }

    override fun onResume() {
        super.onResume()

        // filesListView
        val filesListView = findViewById<ListView>(R.id.filesListView)
        filesListView.adapter = null

        (fun(context: Context, dbxProxy: DbxProxy) = launch(UI) {
            val res = dbxProxy.listFolderAsync("", null).await()
            filesListView.adapter = ArrayAdapter<String>(
                    context,
                    android.R.layout.simple_list_item_1,
                    res.entries.map { it.name })

            // TODO: continue (res.hasMore)
        })(this, this.dbxProxy)
    }
}
