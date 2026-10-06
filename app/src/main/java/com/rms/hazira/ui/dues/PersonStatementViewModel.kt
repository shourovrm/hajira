package com.rms.hazira.ui.dues

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rms.hazira.HaziraApp
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.MonthAccount
import com.rms.hazira.domain.Payment
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.buildMonthAccount
import com.rms.hazira.domain.buildStatementText
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * One person's statement for [month]. [person] and [account] are null until the first read
 * finishes, or when the person no longer exists.
 *
 * @property records The person's records in [month].
 * @property payments The person's payments counted towards [month], oldest first.
 * @property earlierMonths Months before [month] that have a charge or a payment, newest first.
 */
data class PersonStatementUiState(
    val month: YearMonth = YearMonth.now(),
    val person: Person? = null,
    val account: MonthAccount? = null,
    val records: List<DayRecord> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val statementText: String = "",
    val earlierMonths: List<EarlierMonth> = emptyList(),
)

/** An earlier month's account and the day its last payment was made, if any. */
data class EarlierMonth(
    val account: MonthAccount,
    val lastPaidOn: LocalDate?,
)

private data class StatementSelection(val personId: Long, val month: YearMonth)

class PersonStatementViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<HaziraApp>().repository

    private val selection = MutableStateFlow<StatementSelection?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PersonStatementUiState> = selection
        .flatMapLatest { chosen -> if (chosen == null) flowOf(PersonStatementUiState()) else observeStatement(chosen) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PersonStatementUiState())

    fun show(personId: Long, month: YearMonth) {
        selection.value = StatementSelection(personId, month)
    }

    fun addPayment(amountTaka: Int, paidOn: LocalDate, note: String) {
        val chosen = selection.value ?: return
        viewModelScope.launch {
            repository.addPayment(
                Payment(personId = chosen.personId, month = chosen.month, amountTaka = amountTaka, paidOn = paidOn, note = note),
            )
        }
    }

    fun deletePayment(paymentId: Long) {
        viewModelScope.launch {
            repository.deletePayment(paymentId)
        }
    }

    private fun observeStatement(chosen: StatementSelection) = combine(
        repository.observePerson(chosen.personId),
        repository.observeDayRecords(LocalDate.MIN, LocalDate.MAX),
        repository.observePaymentsForPerson(chosen.personId),
    ) { person, allRecords, allPayments ->
        if (person == null) {
            PersonStatementUiState(month = chosen.month)
        } else {
            buildUiState(person, chosen.month, allRecords.filter { record -> record.personId == person.id }, allPayments)
        }
    }

    private fun buildUiState(
        person: Person,
        month: YearMonth,
        personRecords: List<DayRecord>,
        personPayments: List<Payment>,
    ): PersonStatementUiState {
        val account = buildMonthAccount(person, month, personRecords, personPayments)
        val recordsInMonth = personRecords.filter { record -> YearMonth.from(record.date) == month }
        val paymentsInMonth = personPayments.filter { payment -> payment.month == month }.sortedBy { payment -> payment.paidOn }
        return PersonStatementUiState(
            month = month,
            person = person,
            account = account,
            records = recordsInMonth,
            payments = paymentsInMonth,
            statementText = buildStatementText(account, recordsInMonth, paymentsInMonth),
            earlierMonths = buildEarlierMonths(person, month, personRecords, personPayments),
        )
    }

    private fun buildEarlierMonths(
        person: Person,
        month: YearMonth,
        personRecords: List<DayRecord>,
        personPayments: List<Payment>,
    ): List<EarlierMonth> {
        val monthsWithData = personRecords.map { record -> YearMonth.from(record.date) } +
            personPayments.map { payment -> payment.month }
        return monthsWithData
            .distinct()
            .filter { candidate -> candidate.isBefore(month) }
            .sortedDescending()
            .map { candidate -> buildMonthAccount(person, candidate, personRecords, personPayments) }
            .filter { account -> account.chargeTaka != 0 || account.paidTaka != 0 }
            .map { account ->
                val lastPaidOn = personPayments
                    .filter { payment -> payment.month == account.month }
                    .maxOfOrNull { payment -> payment.paidOn }
                EarlierMonth(account = account, lastPaidOn = lastPaidOn)
            }
    }
}
