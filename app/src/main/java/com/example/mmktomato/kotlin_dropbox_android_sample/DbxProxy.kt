package com.example.mmktomato.kotlin_dropbox_android_sample

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.users.FullAccount
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

internal class DbxProxy(private val prefs: SharedPrefsProxy) {
    private object ClientHolder {
        lateinit var client: DbxClientV2
        var initialized = false
    }

    val initialized
        get() = ClientHolder.initialized

    init {
        val accessToken = this.prefs.dbxAccessToken
        if (!accessToken.isNullOrEmpty()) {
            this.initialize(accessToken)
        }
    }

    fun initialize(accessToken: String) {
        val config = DbxRequestConfig("kotlin-dbx-android")
        ClientHolder.client = DbxClientV2(config, accessToken)
        ClientHolder.initialized = true
    }

    fun getAccountAsync(): Deferred<FullAccount> = async(CommonPool) {
        return@async ClientHolder.client.users().currentAccount
    }

    fun listFolderAsync(path: String, prevRes: ListFolderResult?) = async(CommonPool) {
        if (prevRes == null) {
            return@async ClientHolder.client.files().listFolder(path)
        }
        else {
            return@async ClientHolder.client.files().listFolderContinue(prevRes.cursor)
        }
    }
}