package com.rijal.salesandcredit.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rijal.salesandcredit.db.dao.*
import com.rijal.salesandcredit.db.entity.*
import com.rijal.salesandcredit.helpers.Constants
import com.rijal.salesandcredit.helpers.DataTypeConverters
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        User::class,
        Transaction::class,
        Invoice::class,
        Item::class,
        ItemTransaction::class,
        Person::class,
        Cost::class,
        Settlement::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(DataTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao
    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): TransactionDao
    abstract fun settlementDao(): SettlementDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun costDao(): CostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

//        private val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE `transaction` ADD COLUMN totalKeuntungan REAL NOT NULL DEFAULT 0.0")
//            }
//        }
        fun getDatabase(context: Context, scope: CoroutineScope?): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    Constants.DB_NAME)
//                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}