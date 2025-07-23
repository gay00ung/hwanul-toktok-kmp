package net.ifmain.hwanultoktok.kmp.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import net.ifmain.hwanultoktok.kmp.database.HwanulDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(HwanulDatabase.Schema, "hwanul.db")
    }
}