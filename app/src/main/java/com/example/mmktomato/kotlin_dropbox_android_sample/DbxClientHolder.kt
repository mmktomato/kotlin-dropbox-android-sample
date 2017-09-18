package com.example.mmktomato.kotlin_dropbox_android_sample

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2

object DbxClientHolder {
    lateinit var client: DbxClientV2
        get
        private set

    var initialized = false
        get
        private set

    fun init(accessToken: String) {
        val config = DbxRequestConfig("kotlin-dbx-android")
        this.client = DbxClientV2(config, accessToken)
        this.initialized = true
    }
}