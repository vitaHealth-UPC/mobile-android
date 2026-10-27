package com.vitahealth.tata.carelink.application.commands

/** Requests a link using the temporary code supplied by an adult profile. */
data class AcceptCareLinkCommand(val caregiverId: String, val code: String)
