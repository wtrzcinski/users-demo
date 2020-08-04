rootProject.name = "users-demo"

include(
        "user:core",
        "user:periphery:webflux",
        "user:periphery:kafka",
        "user:periphery:r2dbc",
        "user:service",
        "user:client:webflux",

        "notification:core",
        "notification:periphery:webflux",
        "notification:periphery:kafka",
        "notification:periphery:smtp",
        "notification:periphery:template",
        "notification:service"
)