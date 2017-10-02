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

/**
 * Activity for browsing files and folders in DropBox.
 */
class BrowseActivity : AppCompatActivity() {
    /**
     * A proxy object of DropBox API.
     */
    private lateinit var dbxProxy: DbxProxy

    /**
     * An adapter of fileListView.
     */
    private lateinit var listViewAdapter: ArrayAdapter<String>

    /**
     * A progress bar of filesListView.
     */
    private lateinit var progressBar: View

    /**
     * Previous result of calling DbxProxy.listFolderAsync.
     */
    private var lastResult: ListFolderResult? = null

    /**
     * A boolean flag of preventing OnScroll callback.
     */
    private var preventOnScroll = false

    /**
     * A boolean flag of whether all files and folders are loaded.
     */
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

                // Scrolling listView to bottom, fetches next.
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

    /**
     * Fetches files and folders in the DropBox.
     *
     * @param prevRes previous result of DbxProxy.listFolderAsync.
     * @return the result of DbxProxy.listFolderAsync.
     */
    private fun fetchItems(prevRes: ListFolderResult?) = async(UI) {
        val res = dbxProxy.listFolderAsync("", prevRes).await()
        lastResult = res

        return@async res
    }

    /**
     * Adds files and folders to filesListView.
     *
     * @param res the result of DbxProxy.listFolderAsync.
     * @param listView target ListView.
     */
    private fun addItemsToListView(res: ListFolderResult, listView: ListView) {
        this.listViewAdapter.addAll(res.entries.map { it.name })

        if (!res.hasMore) {
            listView.removeFooterView(this.progressBar)
            isAllItemsLoaded = true
        }
    }
}
