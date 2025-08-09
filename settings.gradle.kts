rootProject.name = "few"

include("api")

include("domain")
include("domain:generator")
include("domain:provider")

include("library")
include("library:common")
include("library:email")
include("library:storage")
include("library:web")
include("library:security")