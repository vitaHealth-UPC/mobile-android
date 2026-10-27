package com.vitahealth.tata.identity.infrastructure.remote

/** Older-adult identity and PIN used for setup or authentication. */
data class PinAccessRequest(val olderAdultId: String, val pin: String)
