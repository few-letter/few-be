package com.few.provider.controller.response

class EnrollSubscriptionResponse(
    val existingCategories: List<CodeValueResponse>,
    val newCategories: List<CodeValueResponse>,
)