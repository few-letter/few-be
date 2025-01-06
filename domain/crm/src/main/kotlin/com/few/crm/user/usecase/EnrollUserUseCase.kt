package com.few.crm.user.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.few.crm.support.jpa.CrmTransactional
import com.few.crm.user.domain.User
import com.few.crm.user.repository.UserRepository
import com.few.crm.user.usecase.dto.EnrollUserUseCaseIn
import com.few.crm.user.usecase.dto.EnrollUserUseCaseOut
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class EnrollUserUseCase(
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
) {
    @CrmTransactional
    fun execute(useCaseIn: EnrollUserUseCaseIn): EnrollUserUseCaseOut {
        val id: Long? = useCaseIn.id
        val externalId: String = useCaseIn.externalId
        val userAttributes: String = useCaseIn.userAttributes
        run {
            try {
                val json = objectMapper.readTree(userAttributes)
                if (json["email"] == null) {
                    throw IllegalArgumentException("Email is required")
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid JSON")
            }
        }

        val updateOrSaveUser =
            if (id != null) {
                userRepository
                    .findById(id)
                    .orElseThrow {
                        throw IllegalArgumentException("User not found id: $id")
                    }.apply {
                        updateAttributes(userAttributes)
                    }
            } else {
                userRepository.save(
                    User(
                        externalId = externalId,
                        userAttributes = userAttributes,
                    ),
                )
            }

        return run {
            EnrollUserUseCaseOut(
                id = updateOrSaveUser.id!!,
                externalId = updateOrSaveUser.externalId!!,
                userAttributes = updateOrSaveUser.userAttributes,
            )
        }
    }
}