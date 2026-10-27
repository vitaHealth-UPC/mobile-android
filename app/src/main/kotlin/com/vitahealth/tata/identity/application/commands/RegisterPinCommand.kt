package com.vitahealth.tata.identity.application.commands

/** Carries sensitive credentials without a generated secret-revealing toString(). */
class RegisterPinCommand(val olderAdultId: String, val pin: String)
