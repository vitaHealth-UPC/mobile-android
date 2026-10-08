package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.treatment.application.MyMedicationsRepository

class GetMyMedicationsQueryHandler(private val repository: MyMedicationsRepository) {
    suspend operator fun invoke() = repository.list()
}
