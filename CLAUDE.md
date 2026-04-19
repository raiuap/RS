# Claude Code Memory - RS Project

## Comandos Base
- **Compilar:** `./gradlew build`
- **Ejecutar App:** `./gradlew bootRun`
- **Limpiar:** `./gradlew clean`

## Directrices de Arquitectura
- **Estructura de Capas:**
  - `org.example.restlye1.controller`: Manejo de endpoints HTTP y validación básica de entrada.
  - `org.example.restlye1.service`: Orquestación de lógica de negocio y coordinación entre scrapers.
  - `org.example.restlye1.scraper`: **Aislamiento de Playwright**. Único lugar permitido para interactuar con el DOM y la API de Playwright.
- **Regla de Oro:** Nunca instancies `Playwright`, `Browser` o `Page` fuera del paquete `scraper`. Los Services deben recibir DTOs o datos limpios procesados por los scrapers.

## Workflow Progresivo (Sin Tests Iniciales)
Como el proyecto se encuentra en fase de andamiaje:
1. **Validación de Entorno:** Antes de implementar un scraper de producción, crea un endpoint temporal `GET /api/v1/scraping/test-connectivity` que simplemente navegue a `google.com` y devuelva el `<title>`.
2. **Implementación de Scraper:** Implementa la lógica siguiendo las guías en `docs/skills/playwright-skills.md`.
3. **Migración a Tests:** Una vez validada la conectividad, reemplaza los endpoints de prueba por tests de integración con JUnit 5.

## Recursos Adicionales
- [Playwright Skills](docs/skills/playwright-skills.md)
