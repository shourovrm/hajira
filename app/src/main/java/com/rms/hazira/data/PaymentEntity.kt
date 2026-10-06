package com.rms.hazira.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rms.hazira.domain.Payment
import java.time.LocalDate
import java.time.YearMonth

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["personId"]), Index(value = ["month"])],
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val personId: Long,
    /** ISO year-month such as "2026-09"; text that sorts in calendar order. */
    val month: String,
    val amountTaka: Int,
    val paidOnEpochDay: Long,
    val note: String,
)

fun PaymentEntity.toDomain(): Payment {
    return Payment(
        id = id,
        personId = personId,
        month = YearMonth.parse(month),
        amountTaka = amountTaka,
        paidOn = LocalDate.ofEpochDay(paidOnEpochDay),
        note = note,
    )
}

fun Payment.toEntity(): PaymentEntity {
    return PaymentEntity(
        id = id,
        personId = personId,
        month = month.toString(),
        amountTaka = amountTaka,
        paidOnEpochDay = paidOn.toEpochDay(),
        note = note,
    )
}
