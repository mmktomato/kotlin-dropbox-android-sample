package com.example.mmktomato.kotlin_dropbox_android_sample

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.Metadata
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Activity for browsing files and folders in DropBox.
 */
class BrowseActivity : AppCompatActivity() {

    /**
     * Listview scroll listener.
     *
     * @param onScrollBottom the callback function called when listview is scrolled to bottom. Returns whether there are more items.
     */
    private class OnScrollListener(private val onScrollBottom: (ctx: CoroutineContext) -> Deferred<Boolean>) : AbsListView.OnScrollListener {
        /**
         * A boolean flag of preventing OnScroll callback.
         */
        private var preventOnScroll = false

        /**
         * A boolean flag of whether all files and folders are loaded.
         */
        private var isAllItemsLoaded = false

        override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            if (preventOnScroll || isAllItemsLoaded) {
                return
            }
            preventOnScroll = true

            // Scrolling listView to bottom, fetches next.
            if (totalItemCount == firstVisibleItem + visibleItemCount) {
                val ctx = UI
                launch(ctx) {
                    try {
                        isAllItemsLoaded = !onScrollBottom(ctx).await()
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
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)

        this.dbxProxy = DbxProxy(SharedPrefsProxy(this))

        val inflater = LayoutInflater.from(this)

        val filesListView = findViewById<ListView>(R.id.filesListView)
        this.progressBar = inflater.inflate(R.layout.listview_progressbar, filesListView, false)
        this.listViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())

        val path = this.intent.getStringExtra("path")

        filesListView.adapter = this.listViewAdapter
        filesListView.addFooterView(this.progressBar)

        // on scroll
        val onScrollListener = OnScrollListener({ ctx ->
            async(ctx) {
                val res = fetchItems(path, lastResult).await()
                return@async addItemsToListView(res, filesListView)
            }
        })
        filesListView.setOnScrollListener(onScrollListener)

        // on item click
        filesListView.setOnItemClickListener { parent, view, position, id ->
            val metadata = this.lastResult?.entries?.get(position)

            if (metadata != null) {
                this.onListViewItemClick(metadata)
            }
        }
    }

    /**
     * Returns MIME type of specified filename.
     *
     * @param filename the filename to get mime type.
     * @return the mime type.
     */
    private fun getMimeType(filename: String): String {
        val i = filename.lastIndexOf(".")
        val ext = filename.substring(i + 1)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * Fetches files and folders in the DropBox.
     *
     * @param path folder path to show.
     * @param prevRes previous result of DbxProxy.listFolderAsync.
     * @return the result of DbxProxy.listFolderAsync.
     */
    private fun fetchItems(path: String, prevRes: ListFolderResult?) = async(UI) {
        val res = dbxProxy.listFolderAsync(path, prevRes).await()
        lastResult = res

        return@async res
    }

    /**
     * Adds files and folders to filesListView.
     *
     * @param res the result of DbxProxy.listFolderAsync.
     * @param listView target ListView.
     * @return whether there are more items.
     */
    private fun addItemsToListView(res: ListFolderResult, listView: ListView): Boolean {
        this.listViewAdapter.addAll(res.entries.map { it.name })

        if (!res.hasMore) {
            listView.removeFooterView(this.progressBar)
            return false
        }
        return true
    }

    /**
     * Called when an item of filesListView is tapped.
     *
     * @param metadata the tapped metadata.
     */
    private fun onListViewItemClick(metadata: Metadata) {
        when (metadata) {
            is FileMetadata -> {
                val ctx = this
                launch(UI) {
                    val res = dbxProxy.getTemporaryLinkAsync(metadata.pathLower).await()
                    val tempLink = res.link

                    //var intent = Intent(Intent.ACTION_SEND)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(tempLink), getMimeType(res.metadata.name))

                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(ctx, "No apps to open this file.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            is FolderMetadata -> {
                val intent = Intent(this, BrowseActivity::class.java)
                intent.putExtra("path", metadata.pathLower)
                this.startActivity(intent)
            }
        }
    }
}
