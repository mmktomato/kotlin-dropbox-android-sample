package com.example.mmktomato.kotlin_dropbox_android_sample

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences

internal class SharedPrefsProxy(private val context: ContextWrapper) {
    private val PREF_NAME = "kotlin-dbx-android"
    private val PREF_MODE = Context.MODE_PRIVATE
    private val KEY_DBX_ACCESS_TOKEN = "access-token"

    private val prefs
        get() = this.context.getSharedPreferences(PREF_NAME, PREF_MODE)

    internal var dbxAccessToken
        get() = this.prefs.getString(KEY_DBX_ACCESS_TOKEN, "")
        set(value) = this.apply(this.prefs.edit().putString(KEY_DBX_ACCESS_TOKEN, value))

    internal fun removeDbxAccessToken() {
        this.apply(this.prefs.edit().remove(KEY_DBX_ACCESS_TOKEN))
    }

    private fun apply(editor: SharedPreferences.Editor) {
        editor.apply()
    }
}