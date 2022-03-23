package com.rijal.salesandcredit.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String = "",
    val password: String = "",
    val active: Boolean = true,
)