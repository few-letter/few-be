package com.few.api.domain.member.usecase

import com.few.api.config.jooq.ApiTransactional
import com.few.api.domain.member.repo.MemberDao
import com.few.api.domain.member.repo.command.DeleteMemberCommand
import com.few.api.domain.member.service.MemberSubscriptionService
import com.few.api.domain.member.service.dto.DeleteSubscriptionDto
import com.few.api.domain.member.usecase.dto.DeleteMemberUseCaseIn
import com.few.api.domain.member.usecase.dto.DeleteMemberUseCaseOut
import org.springframework.stereotype.Component

@Component
class DeleteMemberUseCase(
    private val memberDao: MemberDao,
    private val memberSubscriptionService: MemberSubscriptionService,
) {
    @ApiTransactional
    fun execute(useCaseIn: DeleteMemberUseCaseIn): DeleteMemberUseCaseOut {
        memberDao.deleteMember(
            DeleteMemberCommand(
                memberId = useCaseIn.memberId,
            ),
        )

        memberSubscriptionService.deleteSubscription(
            DeleteSubscriptionDto(
                memberId = useCaseIn.memberId,
            ),
        )
        return DeleteMemberUseCaseOut(true)
    }
}