package com.example.mmktomato.kotlin_dropbox_android_sample

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences

/**
 * Proxy class of SharedPreferences.
 *
 * @param context the object holding SharedPreferences.
 * @constructor Creates proxy.
 */
internal class SharedPrefsProxy(private val context: ContextWrapper) {
    /**
     * The constant value of preference name.
     */
    private val PREF_NAME = "kotlin-dbx-android"

    /**
     * The constant value of preference mode.
     */
    private val PREF_MODE = Context.MODE_PRIVATE

    /**
     * The constant value of key for access token.
     */
    private val KEY_DBX_ACCESS_TOKEN = "access-token"

    /**
     * Returns instance of SharedPreferences.
     */
    private val prefs
        get() = this.context.getSharedPreferences(PREF_NAME, PREF_MODE)

    /**
     * The property of DropBox access token.
     */
    internal var dbxAccessToken
        get() = this.prefs.getString(KEY_DBX_ACCESS_TOKEN, "")
        set(value) = this.apply(this.prefs.edit().putString(KEY_DBX_ACCESS_TOKEN, value))

    /**
     * Removes DropBox access token.
     */
    internal fun removeDbxAccessToken() {
        this.apply(this.prefs.edit().remove(KEY_DBX_ACCESS_TOKEN))
    }

    /**
     * Commits the changes.
     */
    private fun apply(editor: SharedPreferences.Editor) {
        editor.apply()
    }
}