package com.vitahealth.tata.identity.application.commands

/** Carries sensitive credentials without a generated secret-revealing toString(). */
class SignInWithPinCommand(val olderAdultId: String, val pin: String)
