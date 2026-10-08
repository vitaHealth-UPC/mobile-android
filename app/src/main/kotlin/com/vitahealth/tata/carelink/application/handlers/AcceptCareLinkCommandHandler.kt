package com.vitahealth.tata.carelink.application.handlers

import com.vitahealth.tata.carelink.application.CareLinkRepository
import com.vitahealth.tata.carelink.application.commands.AcceptCareLinkCommand

class AcceptCareLinkCommandHandler(
    private val repository: CareLinkRepository,
) {
    suspend operator fun invoke(command: AcceptCareLinkCommand) =
        repository.acceptLink(command.caregiverId, command.code)
}
