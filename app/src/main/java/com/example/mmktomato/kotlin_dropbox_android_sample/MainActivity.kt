package com.example.mmktomato.kotlin_dropbox_android_sample

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.users.FullAccount
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI

class MainActivity : AppCompatActivity() {
    private val LOG_TAG: String = "kotlin-dbx-android"
    private val prefs = SharedPrefsProxy(this)

    private fun getAccountAsync(dbxClient: DbxClientV2): Deferred<FullAccount> = async(CommonPool) {
        return@async dbxClient.users().currentAccount
    }

    private fun listFolderAsync(dbxClient: DbxClientV2, path: String) = launch(CommonPool) {
        fun listFolder(prevRes: ListFolderResult?) = async(CommonPool) {
            if (prevRes == null) {
                return@async dbxClient.files().listFolder(path)
            }
            else {
                return@async dbxClient.files().listFolderContinue(prevRes.cursor)
            }
        }

        var res: ListFolderResult? = null
        do {
            res = listFolder(res).await()
            for (metadata in res.entries) {
                Log.i(LOG_TAG, metadata.name)
            }
        } while (res?.hasMore ?: false)
    }

    private fun getAccessToken(): String {
        var accessToken = this.prefs.dbxAccessToken
        if (accessToken.isNullOrEmpty()) {
            accessToken = Auth.getOAuth2Token()
            if (accessToken.isNullOrEmpty()) {
                accessToken = ""
            }
            else {
                this.prefs.dbxAccessToken = accessToken
            }
        }
        return accessToken
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // authenticateButton
        val authenticateButton = findViewById<Button>(R.id.authenticateButton)
        authenticateButton.setOnClickListener {
            if (!DbxClientHolder.initialized) {
                Auth.startOAuth2Authentication(this, BuildConfig.DROPBOX_APP_KEY)
            }
        }

        // listFolderButton
        val listFolderButton = findViewById<Button>(R.id.listFolderButton)
        listFolderButton.setOnClickListener {
            if (!DbxClientHolder.initialized) {
                return@setOnClickListener
            }
            listFolderAsync(DbxClientHolder.client, "")
        }

        // clearAccessTokenButton
        val clearAccessTokenButton = findViewById<Button>(R.id.clearAccessTokenButton)
        clearAccessTokenButton.setOnClickListener {
            this.prefs.removeDbxAccessToken()
        }
    }

    override fun onResume() {
        super.onResume()

        // check authentication.
        val accountTextView = findViewById<TextView>(R.id.accountTextView)
        val accessToken = getAccessToken()
        if (accessToken.isNullOrEmpty()) {
            accountTextView.text = "not authenticated."
        }
        else {
            DbxClientHolder.init(accessToken)
            launch(UI) {
                val account = getAccountAsync(DbxClientHolder.client).await()
                accountTextView.text = account.name.displayName
            }
        }
    }
}
