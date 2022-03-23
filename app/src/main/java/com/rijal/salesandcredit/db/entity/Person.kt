package com.rijal.salesandcredit.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person")
class Person(
    @PrimaryKey(autoGenerate = true) var personId: Int = 0,
    val name: String = "",
    val address: String = "",
    val idCardNo: String = "",
    val idCardImgPath: String = "",
    val phone: String = "",
    )