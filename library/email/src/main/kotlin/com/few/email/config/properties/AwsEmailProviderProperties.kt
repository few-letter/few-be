package com.few.email.config.properties

data class AwsEmailProviderProperties(
    var accessKey: String = "",
    var secretKey: String = "",
    var region: String = "",
    var configurationSetName: String = "",
)