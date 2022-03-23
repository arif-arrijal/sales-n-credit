package com.rijal.salesandcredit.db.dao

import androidx.room.*
import com.rijal.salesandcredit.db.entity.Item

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: Item)

    @Update
    suspend fun update(data: Item)

    @Query("select * from item order by itemId desc")
    suspend fun findAll(): List<Item>

    @Query("select * from item where itemId = :id")
    suspend fun findOne(id: Int): Item

    @Query("select * from item where name like '%' || :query || '%' OR series like '%' || :query || '%'")
    suspend fun findByQuery(query: String): List<Item>
}