package com.rijal.salesandcredit.db.dao

import androidx.room.*
import com.rijal.salesandcredit.db.entity.Item
import com.rijal.salesandcredit.db.entity.Person
import com.rijal.salesandcredit.db.entity.User

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: Person)

    @Update
    suspend fun update(data: Person)

    @Query("select * from person order by personId desc")
    suspend fun findAll(): List<Person>

    @Query("select * from person where personId = :id")
    suspend fun findOne(id: Int): Person

    @Query("select * from person where name like '%' || :query || '%' OR phone like '%' || :query || '%' OR idCardNo like '%' || :query || '%'")
    suspend fun findByQuery(query: String): List<Person>
}