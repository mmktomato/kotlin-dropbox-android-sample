package com.example.mmktomato.kotlin_dropbox_android_sample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.dropbox.core.android.Auth
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI

class MainActivity : AppCompatActivity() {
    private val prefs = SharedPrefsProxy(this)
    private lateinit var dbxProxy: DbxProxy

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

        this.dbxProxy = DbxProxy(this.prefs)

        // authenticateButton
        val authenticateButton = findViewById<Button>(R.id.authenticateButton)
        authenticateButton.setOnClickListener {
            if (!this.dbxProxy.initialized) {
                Auth.startOAuth2Authentication(this, BuildConfig.DROPBOX_APP_KEY)
            }
        }

        // listFolderButton
        val listFolderButton = findViewById<Button>(R.id.listFolderButton)
        listFolderButton.setOnClickListener {
            if (!this.dbxProxy.initialized) {
                return@setOnClickListener
            }
            val intent = Intent(this, BrowseActivity::class.java)
            this.startActivity(intent)
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
            this.dbxProxy.initialize(accessToken)
            (fun(dbxProxy: DbxProxy) = launch(UI) {
                val account = dbxProxy.getAccountAsync().await()
                accountTextView.text = account.name.displayName
            })(this.dbxProxy)
        }
    }
}
