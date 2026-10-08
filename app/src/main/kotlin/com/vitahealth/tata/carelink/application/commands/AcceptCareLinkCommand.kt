package com.vitahealth.tata.carelink.application.commands

data class AcceptCareLinkCommand(
    val caregiverId: String,
    val code: String,
)
