package com.example.neotokopos85.utils

import android.content.Context
import android.content.SharedPreferences

object LowStockTracker {

    private const val PREF_NAME = "low_stock_pref"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Cek apakah notifikasi boleh dikirim
     */
    fun shouldNotify(context: Context, key: String): Boolean {

        val prefs = prefs(context)

        val alreadyNotified = prefs.getBoolean(key, false)

        if (alreadyNotified) {
            return false
        }

        prefs.edit()
            .putBoolean(key, true)
            .apply()

        return true
    }

    /**
     * Reset status notifikasi
     * dipanggil jika stok sudah normal lagi
     */
    fun reset(context: Context, key: String) {

        prefs(context)
            .edit()
            .remove(key)
            .apply()
    }

    /**
     * Optional: reset semua notifikasi
     * bisa dipakai saat login ulang / clear data
     */
    fun clearAll(context: Context) {

        prefs(context)
            .edit()
            .clear()
            .apply()
    }
}