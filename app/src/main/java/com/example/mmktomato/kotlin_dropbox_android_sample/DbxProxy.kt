package com.example.mmktomato.kotlin_dropbox_android_sample

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.users.FullAccount
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

/**
 * Proxy class of DropBox API.
 *
 * @param prefs the proxy of SharedPreferences.
 * @constructor Creates proxy.
 */
internal class DbxProxy(private val prefs: SharedPrefsProxy) {
    /**
     * Holds the DropBox API client.
     */
    private object ClientHolder {
        /**
         * A raw DropBox API client.
         */
        lateinit var client: DbxClientV2

        /**
         * A boolean flag of whether client is initialized.
         */
        var initialized = false
    }

    /**
     * Returns whether this instance is initialized.
     */
    val initialized
        get() = ClientHolder.initialized

    init {
        val accessToken = this.prefs.dbxAccessToken
        if (!accessToken.isNullOrEmpty()) {
            this.initialize(accessToken)
        }
    }

    /**
     * Initializes this instance.
     *
     * @param accessToken the access token for DropBox API.
     */
    fun initialize(accessToken: String) {
        val config = DbxRequestConfig("kotlin-dbx-android")
        ClientHolder.client = DbxClientV2(config, accessToken)
        ClientHolder.initialized = true
    }

    /**
     * Returns a user account information.
     *
     * @return the instance of FollAccount class.
     */
    fun getAccountAsync(): Deferred<FullAccount> = async(CommonPool) {
        return@async ClientHolder.client.users().currentAccount
    }

    /**
     * Returns files and folder information.
     *
     * @param path the folder path in DropBox.
     * @param prevRes the previous result of this method.
     * @return the instance of ListFolderResult class.
     */
    fun listFolderAsync(path: String, prevRes: ListFolderResult?) = async(CommonPool) {
        if (prevRes == null) {
            return@async ClientHolder.client.files().listFolder(path)
        }
        else {
            return@async ClientHolder.client.files().listFolderContinue(prevRes.cursor)
        }
    }

    /**
     * Returns a temporary link of file.
     *
     * @param path the file path.
     * @return the instance of GetTemporaryLinkResult class.
     */
    fun getTemporaryLinkAsync(path: String) = async(CommonPool) {
        return@async ClientHolder.client.files().getTemporaryLink(path)
    }
}