package com.few.crm.view.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.few.crm.user.domain.User
import com.few.crm.user.repository.UserRepository
import com.few.crm.user.usecase.EnrollUserUseCase
import com.few.crm.user.usecase.dto.EnrollUserUseCaseIn
import web.view.CommonVerticalLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import org.springframework.web.client.RestTemplate

@Route("/crm/users")
class CrmUserView(
    private val userRepository: UserRepository,
    private val enrollUserUseCase: EnrollUserUseCase,
    private val objectMapper: ObjectMapper,
) : CommonVerticalLayout() {
    private val grid =
        Grid(User::class.java).apply {
            removeAllColumns()
        }

    init {
        setSizeFull()

        grid.selectionMode = Grid.SelectionMode.SINGLE
        grid.addSelectionListener { event ->
            event.allSelectedItems.firstOrNull()?.let { user ->
                openUserDetailDialog(user)
            }
        }

        val refreshButton =
            Button("Refresh").apply {
                addClickListener {
                    fetchUsers()
                    grid.setItems(userRepository.findAll())
                }
            }

        grid.setItems(userRepository.findAll())

        grid.addColumn(User::id).setHeader("ID").setSortable(true)
        grid.addColumn(User::externalId).setHeader("External ID")
        grid.addColumn(User::userAttributes).setHeader("User Attributes").setAutoWidth(true)

        contentArea.add(refreshButton)
        contentArea.add(grid)
    }

    private fun fetchUsers() {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("http://localhost:8080/api/v1/members", Map::class.java)
        val data = response.body?.get("data") as Map<String, Any>
        data["members"].let {
            it?.let {
                val users = it as List<Map<String, Any>>
                users.forEach { user ->
                    val externalId = (user["id"] as Int)
                    val attributes = mutableMapOf<String, String>()
                    attributes["email"] = user["email"] as String
                    attributes["typeCd"] = user["typeCd"] as String
                    attributes["createdAt"] = user["createdAt"] as String
                    val useCaseIn =
                        userRepository.findByExternalId(externalId.toString())?.let { exitUser ->
                            EnrollUserUseCaseIn(
                                id = exitUser.id,
                                externalId = externalId.toString(),
                                userAttributes = objectMapper.writeValueAsString(attributes),
                            )
                        } ?: run {
                            EnrollUserUseCaseIn(
                                id = null,
                                externalId = externalId.toString(),
                                userAttributes = objectMapper.writeValueAsString(attributes),
                            )
                        }
                    enrollUserUseCase.execute(useCaseIn)
                }
            }
        }
    }

    private fun openUserDetailDialog(user: User) {
        val dialog = Dialog()
        val form = FormLayout()

        val userIdField =
            TextField("ID").apply {
                value = user.id.toString()
                isReadOnly = true
            }

        val externalIdField =
            TextField("External ID").apply {
                value = user.externalId
                isReadOnly = true
            }

        val userAttributesFieldDetails = mutableListOf<TextField>()
        objectMapper.readTree(user.userAttributes).fields().forEach {
            userAttributesFieldDetails.add(
                TextField(it.key).apply {
                    value = it.value.asText()
                    isReadOnly = true
                },
            )
        }

        form.add(userIdField, externalIdField)
        userAttributesFieldDetails.forEach {
            form.add(it)
        }

        dialog.add(form)
        dialog.open()
    }
}