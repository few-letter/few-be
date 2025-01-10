rootProject.name = "few"

include("api")

include("domain")
include("domain:crm")
include("domain:generator")

include("library")
include("library:email")
include("library:storage")
include("library:web")
include("library:security")
include("library:event")