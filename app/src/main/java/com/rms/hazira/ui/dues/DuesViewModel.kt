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
import com.rms.hazira.domain.firstDateNeededFor
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * @property accounts Everyone with a charge or a payment in [month]; unpaid first.
 * @property stillToPayTaka Sum of the unpaid amounts. An overpaid person does not reduce it.
 * @property paidTaka Everything paid towards [month].
 * @property monthTotalTaka Sum of the month's charges.
 */
data class DuesUiState(
    val month: YearMonth = YearMonth.now(),
    val accounts: List<MonthAccount> = emptyList(),
    val stillToPayTaka: Int = 0,
    val paidTaka: Int = 0,
    val monthTotalTaka: Int = 0,
    val isLoaded: Boolean = false,
) {
    val canShowNextMonth: Boolean
        get() = month.isBefore(YearMonth.now())
}

class DuesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<HaziraApp>().repository

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DuesUiState> = selectedMonth
        .flatMapLatest { month -> observeMonth(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DuesUiState())

    fun showPreviousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun showNextMonth() {
        val nextMonth = selectedMonth.value.plusMonths(1)
        if (!nextMonth.isAfter(YearMonth.now())) {
            selectedMonth.value = nextMonth
        }
    }

    private fun observeMonth(month: YearMonth) = combine(
        repository.observePeople(includeInactive = true),
        repository.observeDayRecords(firstDateNeededFor(month), month.atEndOfMonth()),
        repository.observePaymentsForMonth(month),
    ) { people, records, payments ->
        buildUiState(month, people, records, payments)
    }

    private fun buildUiState(
        month: YearMonth,
        people: List<Person>,
        records: List<DayRecord>,
        payments: List<Payment>,
    ): DuesUiState {
        val accounts = people
            .map { person -> buildMonthAccount(person, month, records, payments) }
            .filter { account -> account.chargeTaka != 0 || account.paidTaka != 0 }
            .sortedWith(compareBy<MonthAccount> { account -> account.dueTaka <= 0 }.thenBy { account -> account.person.name })

        return DuesUiState(
            month = month,
            accounts = accounts,
            stillToPayTaka = accounts.sumOf { account -> maxOf(account.dueTaka, 0) },
            paidTaka = accounts.sumOf { account -> account.paidTaka },
            monthTotalTaka = accounts.sumOf { account -> account.chargeTaka },
            isLoaded = true,
        )
    }
}
