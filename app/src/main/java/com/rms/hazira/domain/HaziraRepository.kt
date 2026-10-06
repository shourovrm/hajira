package com.rms.hazira.domain

import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

/** The only way screens read or change stored data. */
interface HaziraRepository {

    /** People ordered by name. Stopped services are left out unless [includeInactive] is true. */
    fun observePeople(includeInactive: Boolean = false): Flow<List<Person>>

    fun observePerson(personId: Long): Flow<Person?>

    /** Inserts when `person.id` is 0, otherwise updates. Returns the person's id. */
    suspend fun savePerson(person: Person): Long

    /** Deletes the person together with their day records and payments. */
    suspend fun deletePerson(personId: Long)

    /** Every person's records with a date from [firstDate] to [lastDate], both included. */
    fun observeDayRecords(firstDate: LocalDate, lastDate: LocalDate): Flow<List<DayRecord>>

    /** Stores the record, replacing any existing one for the same person and date. */
    suspend fun saveDayRecord(record: DayRecord)

    /** Returns the day to "not marked". */
    suspend fun clearDayRecord(personId: Long, date: LocalDate)

    /** Every person's payments that count towards [month]. */
    fun observePaymentsForMonth(month: YearMonth): Flow<List<Payment>>

    /** One person's payments across all months, newest first. */
    fun observePaymentsForPerson(personId: Long): Flow<List<Payment>>

    suspend fun addPayment(payment: Payment): Long

    suspend fun deletePayment(paymentId: Long)
}
