package com.rms.hazira.ui.people

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.RateKind
import com.rms.hazira.domain.ServiceKind
import com.rms.hazira.domain.formatQuantity
import java.time.DayOfWeek
import java.time.LocalTime

private const val DEFAULT_UNIT_NAME = "litre"

/** The values being typed on the edit screen, and the rules for turning them into a [Person]. */
class PersonForm(private val existingPerson: Person?) {

    var name by mutableStateOf(existingPerson?.name.orEmpty())
    var role by mutableStateOf(existingPerson?.role.orEmpty())
    var kind by mutableStateOf(existingPerson?.kind ?: ServiceKind.VISIT)
    var weekdays by mutableStateOf(existingPerson?.scheduledWeekdays ?: emptySet())
    var expectedTime by mutableStateOf<LocalTime?>(existingPerson?.expectedTime)
    var rateKind by mutableStateOf(existingPerson?.rateKind ?: RateKind.MONTHLY)
    var rateText by mutableStateOf(existingPerson?.rateTaka?.toString().orEmpty())
    var unitName by mutableStateOf(existingPerson?.unitName.orEmpty().ifBlank { DEFAULT_UNIT_NAME })
    var quantityText by mutableStateOf(formatQuantity(existingPerson?.defaultQuantity ?: 1.0))
    var phone by mutableStateOf(existingPerson?.phone.orEmpty())
    var colourIndex by mutableStateOf(existingPerson?.colourIndex ?: 0)
    var isActive by mutableStateOf(existingPerson?.isActive ?: true)

    var nameError by mutableStateOf<String?>(null)
    var weekdaysError by mutableStateOf<String?>(null)
    var rateError by mutableStateOf<String?>(null)
    var unitError by mutableStateOf<String?>(null)
    var quantityError by mutableStateOf<String?>(null)

    val isDelivery: Boolean
        get() = kind == ServiceKind.DELIVERY

    /** Per unit only makes sense for a delivery, so changing the kind can change the rate kind too. */
    fun selectKind(newKind: ServiceKind) {
        kind = newKind
        if (newKind == ServiceKind.DELIVERY && rateKind == RateKind.PER_VISIT) {
            rateKind = RateKind.PER_UNIT
        }
        if (newKind != ServiceKind.DELIVERY && rateKind == RateKind.PER_UNIT) {
            rateKind = RateKind.PER_VISIT
        }
    }

    fun toggleWeekday(weekday: DayOfWeek) {
        weekdays = if (weekday in weekdays) weekdays - weekday else weekdays + weekday
    }

    fun toggleEveryDay() {
        weekdays = if (weekdays.size == 7) emptySet() else DayOfWeek.entries.toSet()
    }

    /** Shows each problem next to its field. Returns the person to save, or null when anything is wrong. */
    fun validateAndBuild(): Person? {
        nameError = if (name.isBlank()) "Enter a name" else null
        weekdaysError = if (weekdays.isEmpty()) "Pick at least one day" else null

        val rateTaka = rateText.trim().toIntOrNull()
        rateError = if (rateTaka == null || rateTaka < 0) "Enter the amount in whole taka" else null

        val quantity = quantityText.trim().toDoubleOrNull()
        unitError = null
        quantityError = null
        if (isDelivery) {
            unitError = if (unitName.isBlank()) "Enter what it is counted in, for example litre" else null
            quantityError = if (quantity == null || quantity <= 0.0) "Enter a number above zero" else null
        }

        val hasProblem = listOf(nameError, weekdaysError, rateError, unitError, quantityError).any { error -> error != null }
        if (hasProblem || rateTaka == null) {
            return null
        }
        return buildPerson(rateTaka, quantity ?: 1.0)
    }

    private fun buildPerson(rateTaka: Int, quantity: Double): Person {
        return Person(
            id = existingPerson?.id ?: 0,
            name = name.trim(),
            role = role.trim(),
            kind = kind,
            scheduledWeekdays = weekdays,
            expectedTime = expectedTime,
            rateKind = rateKind,
            rateTaka = rateTaka,
            unitName = if (isDelivery) unitName.trim() else "",
            defaultQuantity = if (isDelivery) quantity else 1.0,
            phone = phone.trim(),
            colourIndex = colourIndex,
            isActive = isActive,
        )
    }
}
