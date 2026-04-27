rootProject.name = "backend-platform"

include(
    "services:api-gateway",
    "services:auth-service",
    "services:ai-proxy-service",
    "services:chat-service",
    "services:prompt-service"
)
