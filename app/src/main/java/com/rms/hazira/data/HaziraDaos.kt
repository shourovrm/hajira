package com.rms.hazira.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM people WHERE isActive = 1 OR :includeInactive = 1 ORDER BY name COLLATE NOCASE")
    fun observePeople(includeInactive: Boolean): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :personId")
    fun observePerson(personId: Long): Flow<PersonEntity?>

    @Insert
    suspend fun insert(person: PersonEntity): Long

    @Update
    suspend fun update(person: PersonEntity)

    @Query("DELETE FROM people WHERE id = :personId")
    suspend fun delete(personId: Long)
}

@Dao
interface DayRecordDao {

    @Query("SELECT * FROM day_records WHERE epochDay BETWEEN :firstEpochDay AND :lastEpochDay")
    fun observeBetween(firstEpochDay: Long, lastEpochDay: Long): Flow<List<DayRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(record: DayRecordEntity)

    @Query("DELETE FROM day_records WHERE personId = :personId AND epochDay = :epochDay")
    suspend fun clear(personId: Long, epochDay: Long)
}

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE month = :month")
    fun observeForMonth(month: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE personId = :personId ORDER BY paidOnEpochDay DESC, id DESC")
    fun observeForPerson(personId: Long): Flow<List<PaymentEntity>>

    @Insert
    suspend fun insert(payment: PaymentEntity): Long

    @Query("DELETE FROM payments WHERE id = :paymentId")
    suspend fun delete(paymentId: Long)
}
