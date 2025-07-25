package net.ifmain.hwanultoktok.kmp.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.ifmain.hwanultoktok.kmp.database.HwanulDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            HwanulDatabase.Schema, 
            context, 
            "hwanultoktok.db",
            callback = AndroidSqliteDriver.Callback(HwanulDatabase.Schema)
        )
    }
}