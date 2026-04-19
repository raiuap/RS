# Playwright Advanced Skills for Spring Boot

Este documento define las reglas técnicas obligatorias para la implementación de scrapers resilientes utilizando Playwright en Java 24.

## 1. Gestión de Memoria y Recursos
Playwright utiliza procesos de navegador externos que no son gestionados por el Garbage Collector de Java. El cierre explícito es **obligatorio**.

- **Regla:** Todo uso de `BrowserContext` y `Page` debe estar envuelto en bloques `try-with-resources` o `try-finally`.
- **Ejemplo:**
  ```java
  try (Playwright playwright = Playwright.create();
       Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
       BrowserContext context = browser.newContext();
       Page page = context.newPage()) {
      // Lógica de scraping
  } catch (Exception e) {
      log.error("Error durante el scraping", e);
  }
  ```

## 2. Selectores Resilientes y Esperas Dinámicas
Prohibido el uso de `Thread.sleep()` para esperar la carga de elementos en SPAs.

- **Regla:** Uso exclusivo de `page.waitForSelector(selector)` o `locator.waitFor()`.
- **Prioridad de Selectores:**
  1. Atributos semánticos (ARIA roles, placeholder, alt text).
  2. `page.getByTestId()` si el sitio lo soporta.
  3. Selectores CSS estables.
  4. **Prohibido:** XPaths absolutos dependientes de la estructura completa del DOM.

## 3. Optimización de Red y Rendimiento
Para acelerar la ejecución y reducir el consumo de ancho de banda en entornos de servidor.

- **Regla:** Configurar el `BrowserContext` para bloquear recursos innecesarios (imágenes, fuentes, medios).
- **Implementación:**
  ```java
  BrowserContext context = browser.newContext();
  context.route("**/*.{png,jpg,jpeg,svg,gif,woff,woff2,ttf,otf}", Route::abort);
  ```

## 4. Gestión de Excepciones de Red
Las SPAs pueden fallar por timeouts de red o cambios en el DOM.
- **Regla:** Configurar siempre un `setDefaultTimeout` razonable (máximo 30 segundos) para evitar procesos zombies.
