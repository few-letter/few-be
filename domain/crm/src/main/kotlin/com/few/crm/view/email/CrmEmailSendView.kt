package com.few.crm.view.email

import com.fasterxml.jackson.databind.ObjectMapper
import com.few.crm.email.domain.EmailTemplate
import com.few.crm.email.event.send.NotificationEmailSendTimeOutEvent
import com.few.crm.email.repository.EmailTemplateRepository
import com.few.crm.email.usecase.SendNotificationEmailUseCase
import com.few.crm.email.usecase.dto.SendNotificationEmailUseCaseIn
import com.few.crm.support.schedule.TaskView
import com.few.crm.support.schedule.TimeOutEventTaskManager
import com.few.crm.user.domain.User
import com.few.crm.user.repository.UserRepository
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.timepicker.TimePicker
import com.vaadin.flow.router.Route
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Sort
import web.view.CommonVerticalLayout
import java.time.*

@Route("/crm/email/send")
class CrmEmailSendView(
    private val emailTemplateRepository: EmailTemplateRepository,
    private val userRepository: UserRepository,
    private val sendNotificationEmailUseCase: SendNotificationEmailUseCase,
    private val timeOutEventTaskManager: TimeOutEventTaskManager,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) : CommonVerticalLayout() {
    private val templateGrid =
        Grid(EmailTemplate::class.java).apply {
            removeAllColumns()
        }
    private val userGrid =
        Grid(User::class.java).apply {
            removeAllColumns()
        }

    private val selectedTemplates = mutableListOf<EmailTemplate>()
    private val selectedUsers = mutableListOf<User>()

    init {
        setSizeFull()
        templateGrid.selectionMode = Grid.SelectionMode.MULTI
        templateGrid.addSelectionListener { event ->
            selectedTemplates.clear()
            selectedTemplates.add(event.allSelectedItems.first())
            if (event.allSelectedItems.size > 1) {
                event.allSelectedItems.drop(1).forEach {
                    templateGrid.deselect(it)
                }
            }
        }
        userGrid.selectionMode = Grid.SelectionMode.MULTI
        userGrid.addSelectionListener { event ->
            selectedUsers.clear()
            selectedUsers.addAll(event.allSelectedItems)
        }
        templateGrid.height = "auto"
        userGrid.height = "auto"

        val buttonLayout =
            HorizontalLayout().apply {
                isSpacing = true
                isPadding = true
            }

        val notificationButton =
            Button("Notification").apply {
                addClickListener { sendNotificationEmail() }
            }

        val scheduledNotificationButton =
            Button("Scheduled Notification").apply {
                addClickListener {
                    openScheduledNotificationDialog()
                }
            }

        val templates = emailTemplateRepository.findAll(Sort.by(Sort.Order.desc("id")))
        templateGrid.setItems(templates)

        templateGrid.addColumn(EmailTemplate::id).setHeader("ID").setSortable(true)
        templateGrid.addColumn(EmailTemplate::templateName).setHeader("Template Name")
        templateGrid.addColumn(EmailTemplate::subject).setHeader("Subject")
        templateGrid
            .addComponentColumn { template ->
                TextArea().apply {
                    value = template.body
                    isReadOnly = true
                    width = "100%"
                    style.set("min-height", "100px") // 최소 높이 설정
                }
            }.setHeader("Body")
            .setAutoWidth(true)
        templateGrid.addColumn { it.variables.joinToString(", ") }.setHeader("Variables")
        templateGrid.addColumn(EmailTemplate::version).setHeader("Version")
        templateGrid.addColumn(EmailTemplate::createdAt).setHeader("Created At")

        val searchLayout =
            HorizontalLayout(
                TextField("Search by Template Name").apply {
                    placeholder = "Enter template name..."
                    addValueChangeListener {
                        filterGridByEmail(it.value)
                    }
                },
            )

        val users = userRepository.findAll(Sort.by(Sort.Order.desc("id")))
        userGrid.setItems(users)

        userGrid.addColumn(User::id).setHeader("ID").setSortable(true)
        userGrid.addColumn(User::externalId).setHeader("External ID")
        userGrid
            .addComponentColumn {
                TextField().apply {
                    value = objectMapper.readValue(it.userAttributes, Map::class.java)["email"] as String
                    isReadOnly = true
                }
            }.setHeader("Email")
            .setAutoWidth(true)
        userGrid.addColumn(User::userAttributes).setHeader("User Attributes")
        userGrid.addColumn(User::createdAt).setHeader("Created At")

        buttonLayout.add(notificationButton, scheduledNotificationButton)
        contentArea.add(buttonLayout)
        contentArea.add(searchLayout, templateGrid)
        contentArea.add(userGrid)
    }

    private fun openScheduledNotificationDialog() {
        val dialog =
            Dialog().apply {
                width = "80%"
                height = "60%"
            }
        val layout =
            VerticalLayout().apply {
                setSizeFull()
                style.set("overflow", "auto")
            }

        val grid =
            Grid(TaskView::class.java).apply {
                removeAllColumns()
            }
        grid.selectionMode = Grid.SelectionMode.SINGLE
        grid.addColumn(TaskView::taskName).setHeader("Task Name").setAutoWidth(true)
        grid
            .addComponentColumn {
                TextField().apply {
                    value = it.values["expiredTime"].toString()
                    isReadOnly = true
                }
            }.setHeader("Notification Time")
            .setAutoWidth(true)
            .setSortable(true)
        grid
            .addComponentColumn {
                TextField().apply {
                    value =
                        (it.values["templateId"] as Long).let {
                            emailTemplateRepository.findById(it).get().templateName
                        }
                    isReadOnly = true
                }
            }.setHeader("Template Name")
            .setAutoWidth(true)
        grid
            .addComponentColumn {
                TextField().apply {
                    value =
                        (it.values["userIds"] as List<Long>).let {
                            userRepository.findAllByIdIn(it).joinToString(", ") { user ->
                                objectMapper.readValue(user.userAttributes, Map::class.java)["email"] as String
                            }
                        }
                    isReadOnly = true
                }
            }.setHeader("User Emails")
            .setAutoWidth(true)
        grid.addColumn(TaskView::values).setHeader("Values").setAutoWidth(true)
        val tasks = timeOutEventTaskManager.scheduledTasksView()
        grid.setItems(tasks)
        val cancelTaskButton =
            Button("Cancel Task").apply {
                addClickListener {
                    val selectedTask = grid.selectedItems.firstOrNull()
                    if (selectedTask != null) {
                        timeOutEventTaskManager.cancel(selectedTask.taskName)
                        grid.setItems(timeOutEventTaskManager.scheduledTasksView())
                    }
                }
            }

        layout.add(grid)
        layout.add(cancelTaskButton)
        dialog.add(layout)
        dialog.open()
    }

    private fun filterGridByEmail(templateName: String) {
        val filteredTemplates =
            if (templateName.isBlank()) {
                emailTemplateRepository.findAll(Sort.by(Sort.Order.desc("id")))
            } else {
                emailTemplateRepository.findByTemplateNameContainingIgnoreCase(templateName)
            }
        templateGrid.setItems(filteredTemplates)
    }

    private fun sendNotificationEmail() {
        val dialog =
            Dialog().apply {
                width = "80%"
                height = "60%"
            }

        val layout =
            VerticalLayout().apply {
                setSizeFull()
                style.set("overflow", "auto")
            }

        val emailTemplate = selectedTemplates.firstOrNull()
        val templateGrid =
            Grid(EmailTemplate::class.java).apply {
            }
        templateGrid.setItems(emailTemplate)
        templateGrid.addColumn(EmailTemplate::id).setHeader("ID").setSortable(true)
        templateGrid.addColumn(EmailTemplate::templateName).setHeader("Template Name")
        templateGrid.addColumn(EmailTemplate::subject).setHeader("Subject")
        templateGrid
            .addComponentColumn { template ->
                TextArea().apply {
                    value = template.body
                    isReadOnly = true
                    width = "100%"
                    style.set("min-height", "100px") // 최소 높이 설정
                }
            }.setHeader("Body")
            .setAutoWidth(true)
        templateGrid.addColumn { it.variables.joinToString(", ") }.setHeader("Variables")
        templateGrid.addColumn(EmailTemplate::version).setHeader("Version")
        templateGrid.addColumn(EmailTemplate::createdAt).setHeader("Created At")

        val userGrid =
            Grid(User::class.java).apply {
                removeAllColumns()
            }
        userGrid.setItems(selectedUsers)
        userGrid.addColumn(User::id).setHeader("ID").setSortable(true)
        userGrid
            .addComponentColumn {
                TextField().apply {
                    value = objectMapper.readValue(it.userAttributes, Map::class.java)["email"] as String
                    isReadOnly = true
                }
            }.setHeader("Email")
            .setAutoWidth(true)

        val timeLayout =
            HorizontalLayout().apply {
                alignItems = FlexComponent.Alignment.BASELINE
                isSpacing = true
                isPadding = true
            }
        val isNowCheckbox =
            Checkbox("Is Now").apply {
                value = true
                addClickListener {
                    value != value
                    if (value) {
                        timeLayout.children
                            .filter { it is DatePicker || it is TimePicker }
                            .forEach { timeLayout.remove(it) }
                    } else {
                        val datePicker = DatePicker("Select Date")
                        datePicker.value = LocalDate.now()
                        val timePicker = TimePicker("Select Time")
                        timePicker.setStep(Duration.ofMinutes(15))
                        timePicker.value = LocalTime.now()
                        timeLayout.add(datePicker, timePicker)
                    }
                }
            }
        timeLayout.add(isNowCheckbox)

        val sendButton =
            Button("Send").apply {
                addClickListener {
                    if (isNowCheckbox.value) {
                        try {
                            sendNotificationEmailUseCase.execute(
                                SendNotificationEmailUseCaseIn(
                                    templateId = emailTemplate!!.id!!,
                                    templateVersion = null,
                                    userIds = selectedUsers.map { it.id!! },
                                ),
                            )
                        } catch (e: Exception) {
                            val alter = Notification.show(e.message, 3000, Notification.Position.MIDDLE)
                            alter.open()
                        }
                    } else {
                        val date =
                            timeLayout.children
                                .filter { it is DatePicker }
                                .map { it as DatePicker }
                                .map { it.value }
                                .findFirst()
                                .orElseThrow { IllegalArgumentException("Date is required") }
                        val time =
                            timeLayout.children
                                .filter { it is TimePicker }
                                .map { it as TimePicker }
                                .map { it.value }
                                .findFirst()
                                .orElseThrow { IllegalArgumentException("Time is required") }
                        val dateTime = LocalDateTime.of(date, time)
                        try {
                            NotificationEmailSendTimeOutEvent
                                .new(
                                    templateId = emailTemplate!!.id!!,
                                    userIds = selectedUsers.map { it.id!! },
                                    expiredTime = dateTime,
                                    eventPublisher = applicationEventPublisher,
                                ).let {
                                    timeOutEventTaskManager.newSchedule(it)
                                }
                        } catch (e: Exception) {
                            val alter = Notification.show(e.message, 3000, Notification.Position.MIDDLE)
                            alter.open()
                        }
                    }
                    dialog.close()
                }
            }

        layout.add(templateGrid, userGrid)
        layout.add(timeLayout)
        layout.add(sendButton)
        dialog.add(layout)
        dialog.open()
    }
}