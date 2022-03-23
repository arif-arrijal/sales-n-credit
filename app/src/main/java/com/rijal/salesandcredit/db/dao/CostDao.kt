package com.rijal.salesandcredit.db.dao

import androidx.room.*
import com.rijal.salesandcredit.db.entity.Cost
import java.util.*

@Dao
interface CostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: Cost)

    @Update
    suspend fun update(data: Cost)

    @Query("select * from cost order by createdAt desc")
    suspend fun findAll(): List<Cost>

    @Query("select * from cost where createdAt between :start and :end order by createdAt desc")
    suspend fun findBetween(start: Date, end: Date): List<Cost>

    @Query("select * from cost where id = :id")
    suspend fun findOne(id: Int): Cost

    @Query("select * from cost where title like '%' || :query || '%' OR description like '%' || :query || '%'")
    suspend fun findByQuery(query: String): List<Cost>
}