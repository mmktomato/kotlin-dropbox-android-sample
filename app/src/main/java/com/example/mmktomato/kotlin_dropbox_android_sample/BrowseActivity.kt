package com.example.mmktomato.kotlin_dropbox_android_sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.dropbox.core.v2.files.ListFolderResult
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI

class BrowseActivity : AppCompatActivity() {
    private lateinit var dbxProxy: DbxProxy
    private lateinit var listViewAdapter: ArrayAdapter<String>
    private lateinit var progressBar: View
    private var lastResult: ListFolderResult? = null
    private var preventOnScroll = false
    private var isAllItemsLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)

        this.dbxProxy = DbxProxy(SharedPrefsProxy(this))

        val inflater = LayoutInflater.from(this)

        val filesListView = findViewById<ListView>(R.id.filesListView)
        this.progressBar = inflater.inflate(R.layout.listview_progressbar, filesListView, false)
        this.listViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())

        filesListView.adapter = this.listViewAdapter
        filesListView.addFooterView(this.progressBar)
        filesListView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                if (preventOnScroll || isAllItemsLoaded) {
                    return
                }
                preventOnScroll = true

                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    launch(UI) {
                        try {
                            val res = fetchItems(lastResult).await()
                            addItemsToListView(res, filesListView)
                        } finally {
                            preventOnScroll = false
                        }
                    }
                }
                else {
                    preventOnScroll = false
                }
            }

            override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    private fun fetchItems(prevRes: ListFolderResult?) = async(UI) {
        val res = dbxProxy.listFolderAsync("", prevRes).await()
        lastResult = res

        return@async res
    }

    private fun addItemsToListView(res: ListFolderResult, listView: ListView) {
        this.listViewAdapter.addAll(res.entries.map { it.name })

        if (!res.hasMore) {
            listView.removeFooterView(this.progressBar)
            isAllItemsLoaded = true
        }
    }
}
