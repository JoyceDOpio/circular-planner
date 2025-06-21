package com.example.circularplanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "days")
data class Day(
    @PrimaryKey
    val date: LocalDate,
    @ColumnInfo(name = "active_time_start")
    val activeTimeStart: Time,
    @ColumnInfo(name = "active_time_end")
    val activeTimeEnd: Time,
//    @ForeignKey()
//    val userId: UUID
)