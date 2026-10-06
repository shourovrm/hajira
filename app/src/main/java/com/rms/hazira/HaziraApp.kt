package com.rms.hazira

import android.app.Application
import com.rms.hazira.data.RoomHaziraRepository
import com.rms.hazira.domain.HaziraRepository

class HaziraApp : Application() {

    /** One repository for the whole process. Created on first use so app start stays cheap. */
    val repository: HaziraRepository by lazy { RoomHaziraRepository.create(this) }
}
