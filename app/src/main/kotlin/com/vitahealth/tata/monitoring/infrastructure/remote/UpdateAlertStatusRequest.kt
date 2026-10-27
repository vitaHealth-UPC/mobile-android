package com.vitahealth.tata.monitoring.infrastructure.remote

/** `UpdateAlertStatusResource`: only ATTENDED and CLOSED are accepted. */
data class UpdateAlertStatusRequest(val status: String)
