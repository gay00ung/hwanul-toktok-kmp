package net.ifmain.hwanultoktok.kmp.di

import net.ifmain.hwanultoktok.kmp.database.HwanulDatabase

expect class DatabaseDriverFactory {
    fun createDriver(): app.cash.sqldelight.db.SqlDriver
}

fun createDatabase(driverFactory: DatabaseDriverFactory): HwanulDatabase {
    val driver = driverFactory.createDriver()
    return HwanulDatabase(driver)
}