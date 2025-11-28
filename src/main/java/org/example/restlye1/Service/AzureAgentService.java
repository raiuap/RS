package org.example.restlye1.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class AzureAgentService {

    @org.springframework.beans.factory.annotation.Value("${azure.ai.api.key}")
    private String API_KEY;
    // Probando endpoint estándar de Azure OpenAI con el despliegue indicado
    private static final String ENDPOINT = "https://datahack4good-alberto-r-resource.services.ai.azure.com/openai/deployments/gpt-5-chat/chat/completions?api-version=2024-06-01";

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String analizarComposicion(String composicion) {
        try {
            String prompt = "Analiza la siguiente composición de prenda: " + composicion;

            String systemPrompt = """
                    Eres AnalistaModa, un experto en moda y sostenibilidad.

                    Debes responder SIEMPRE en formato JSON con la siguiente estructura EXACTA:
                    {
                      "sustainabilityVerdict": "Excelente|Buena|Regular|Mala",
                      "durabilityScore": <número del 1 al 10>,
                      "durabilityVerdict": "Alta|Media|Baja",
                      "analysisSummary": "<explicación concisa máximo 50 palabras>",
                      "materialBreakdown": [
                        {
                          "material": "<nombre del material>",
                          "percentage": <porcentaje>,
                          "impactNote": "<nota concisa máximo 10 palabras>"
                        }
                      ]
                    }

                    Asegúrate de que la suma de porcentajes sea aproximadamente 100.
                    """;

            Map<String, Object> payload = Map.of(
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", systemPrompt),
                            Map.of(
                                    "role", "user",
                                    "content", prompt)),
                    "response_format", Map.of("type", "json_object"));
            String jsonBody = MAPPER.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("api-key", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return parseResponse(response.body());
            } else {
                return "Error Azure Agent (" + response.statusCode() + "): " + response.body();
            }

        } catch (Exception e) {
            return "Error al conectar con Azure Agent: " + e.getMessage();
        }
    }

    private String parseResponse(String body) {
        try {
            Map<?, ?> responseMap = MAPPER.readValue(body, Map.class);
            List<?> choices = (List<?>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                Map<?, ?> message = (Map<?, ?>) choice.get("message");
                return (String) message.get("content");
            }
            return body;
        } catch (Exception e) {
            return body;
        }
    }
}
