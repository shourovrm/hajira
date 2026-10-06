package com.rms.hazira.data

import android.content.Context
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.HaziraRepository
import com.rms.hazira.domain.Payment
import com.rms.hazira.domain.Person
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomHaziraRepository(private val database: HaziraDatabase) : HaziraRepository {

    private val personDao = database.personDao()
    private val dayRecordDao = database.dayRecordDao()
    private val paymentDao = database.paymentDao()

    override fun observePeople(includeInactive: Boolean): Flow<List<Person>> {
        return personDao.observePeople(includeInactive).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observePerson(personId: Long): Flow<Person?> {
        return personDao.observePerson(personId).map { entity -> entity?.toDomain() }
    }

    override suspend fun savePerson(person: Person): Long {
        if (person.id == 0L) {
            return personDao.insert(person.toEntity())
        }
        personDao.update(person.toEntity())
        return person.id
    }

    override suspend fun deletePerson(personId: Long) {
        personDao.delete(personId)
    }

    override fun observeDayRecords(firstDate: LocalDate, lastDate: LocalDate): Flow<List<DayRecord>> {
        return dayRecordDao.observeBetween(firstDate.toEpochDay(), lastDate.toEpochDay())
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveDayRecord(record: DayRecord) {
        dayRecordDao.save(record.toEntity())
    }

    override suspend fun clearDayRecord(personId: Long, date: LocalDate) {
        dayRecordDao.clear(personId, date.toEpochDay())
    }

    override fun observePaymentsForMonth(month: YearMonth): Flow<List<Payment>> {
        return paymentDao.observeForMonth(month.toString()).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observePaymentsForPerson(personId: Long): Flow<List<Payment>> {
        return paymentDao.observeForPerson(personId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addPayment(payment: Payment): Long {
        return paymentDao.insert(payment.toEntity())
    }

    override suspend fun deletePayment(paymentId: Long) {
        paymentDao.delete(paymentId)
    }

    companion object {
        fun create(context: Context): HaziraRepository {
            return RoomHaziraRepository(HaziraDatabase.create(context))
        }
    }
}
