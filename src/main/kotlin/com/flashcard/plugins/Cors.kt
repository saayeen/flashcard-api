fun Application.configureCORS() {
    val frontendUrl = System.getenv("FRONTEND_URL") ?: "localhost:5173"
    val isLocal = frontendUrl.contains("localhost")

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        // Siempre permite local para desarrolloo
        allowHost("localhost:5173")

        //Cuando este en producción permite el dominio de Vercel
        if (!isLocal) {
            allowHost(frontendUrl, schemes = listOf("https"))
        }
    }
}