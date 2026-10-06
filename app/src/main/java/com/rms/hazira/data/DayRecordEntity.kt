package com.rms.hazira.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.DayStatus
import java.time.LocalDate

/** The unique index on (personId, epochDay) is what makes saving a day replace the old record. */
@Entity(
    tableName = "day_records",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["personId", "epochDay"], unique = true)],
)
data class DayRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val personId: Long,
    val epochDay: Long,
    /** [DayStatus] name. */
    val status: String,
    val quantity: Double,
    val note: String,
)

fun DayRecordEntity.toDomain(): DayRecord {
    return DayRecord(
        personId = personId,
        date = LocalDate.ofEpochDay(epochDay),
        status = DayStatus.valueOf(status),
        quantity = quantity,
        note = note,
    )
}

fun DayRecord.toEntity(): DayRecordEntity {
    return DayRecordEntity(
        id = 0,
        personId = personId,
        epochDay = date.toEpochDay(),
        status = status.name,
        quantity = quantity,
        note = note,
    )
}
