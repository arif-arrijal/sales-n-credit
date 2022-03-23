package com.rijal.salesandcredit.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rijal.salesandcredit.db.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: User): Long

    @Query("SELECT * FROM User WHERE username = :username AND password = :password AND active = 1")
    fun findByUsernameAndPassword(username: String, password: String): LiveData<User?>

    @Query("SELECT COUNT(*) FROM user")
    fun count(): LiveData<Int>

    @Query("SELECT * FROM User WHERE username = :username")
    fun findByUsername(username: String): Flow<User?>
}