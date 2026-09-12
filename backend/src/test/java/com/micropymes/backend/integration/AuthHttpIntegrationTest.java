package com.micropymes.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class AuthHttpIntegrationTest
        extends AbstractPostgresIntegrationTest {

    @LocalServerPort
    private int port;

    /*
     * ============================================================
     * TEST 1
     * Registro + sesión + /me
     * ============================================================
     */

    @Test
    void registerCreatesSessionAndMeReturnsCurrentUser()
            throws Exception {

        SessionClient client =
                new SessionClient(port);

        CsrfData csrf =
                client.getCsrf();

        String email =
                "integration-"
                        + UUID.randomUUID()
                        + "@test.com";

        String body = """
                {
                  "email": "%s",
                  "password": "Password123!",
                  "firstName": "Integration",
                  "lastName": "User",
                  "organizationName": "Empresa Integration"
                }
                """.formatted(email);

        HttpResponse<String> registerResponse =
                client.post(
                        "/api/v1/auth/register",
                        body,
                        csrf
                );

        assertThat(registerResponse.statusCode())
                .isEqualTo(201);

        assertThat(registerResponse.body())
                .contains(email)
                .contains("Empresa Integration")
                .contains("OWNER");

        HttpResponse<String> meResponse =
                client.get(
                        "/api/v1/auth/me"
                );

        assertThat(meResponse.statusCode())
                .isEqualTo(200);

        assertThat(meResponse.body())
                .contains(email)
                .contains("Integration")
                .contains("Empresa Integration")
                .contains("OWNER");
    }

    /*
     * ============================================================
     * TEST 2
     * Una petición POST sin CSRF debe ser rechazada
     * ============================================================
     */

    @Test
    void postWithoutCsrfIsRejected()
            throws Exception {

        SessionClient client =
                new SessionClient(port);

        String email =
                "csrf-"
                        + UUID.randomUUID()
                        + "@test.com";

        String body = """
                {
                  "email": "%s",
                  "password": "Password123!",
                  "firstName": "Csrf",
                  "lastName": "Test",
                  "organizationName": "Empresa CSRF"
                }
                """.formatted(email);

        HttpResponse<String> response =
                client.postWithoutCsrf(
                        "/api/v1/auth/register",
                        body
                );

        assertThat(response.statusCode())
                .isEqualTo(403);
    }

    /*
     * ============================================================
     * TEST 3
     * Flujo comercial completo End-to-End
     * ============================================================
     */

    @Test
    void completeCommercialFlowWorksEndToEnd()
            throws Exception {

        SessionClient client =
                new SessionClient(port);

        /*
         * --------------------------------------------------------
         * 1. Obtener CSRF
         * --------------------------------------------------------
         */

        CsrfData csrf =
                client.getCsrf();

        String email =
                "commercial-"
                        + UUID.randomUUID()
                        + "@test.com";

        String registerBody = """
                {
                  "email": "%s",
                  "password": "Password123!",
                  "firstName": "Commercial",
                  "lastName": "User",
                  "organizationName": "Empresa E2E"
                }
                """.formatted(email);

        /*
         * --------------------------------------------------------
         * 2. Registrar usuario
         *
         * Debe crear:
         * User
         * Organization
         * OrganizationMember OWNER
         * HttpSession
         * --------------------------------------------------------
         */

        HttpResponse<String> registerResponse =
                client.post(
                        "/api/v1/auth/register",
                        registerBody,
                        csrf
                );

        assertThat(registerResponse.statusCode())
                .isEqualTo(201);

        assertThat(registerResponse.body())
                .contains(email)
                .contains("Empresa E2E")
                .contains("OWNER");

        String organizationId =
                extractJsonString(
                        registerResponse.body(),
                        "organizationId"
                );

        /*
         * Después de autenticar la sesión obtenemos
         * de nuevo un CSRF válido.
         */

        csrf = client.getCsrf();

        /*
         * --------------------------------------------------------
         * 3. Crear Customer
         * --------------------------------------------------------
         */

        String customerBody = """
                {
                  "name": "Cliente E2E",
                  "companyName": "Empresa Cliente",
                  "email": "cliente@test.com",
                  "phone": "600000000",
                  "notes": "Cliente creado por test E2E"
                }
                """;

        HttpResponse<String> customerResponse =
                client.post(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/customers",
                        customerBody,
                        csrf
                );

        assertThat(customerResponse.statusCode())
                .isEqualTo(201);

        assertThat(customerResponse.body())
                .contains("Cliente E2E");

        String customerId =
                extractJsonString(
                        customerResponse.body(),
                        "id"
                );

        /*
         * --------------------------------------------------------
         * 4. Crear Opportunity
         * --------------------------------------------------------
         */

        String opportunityBody = """
                {
                  "customerId": "%s",
                  "title": "Proyecto E2E",
                  "description": "Oportunidad del test",
                  "estimatedValue": 1500.00,
                  "currency": "EUR"
                }
                """.formatted(customerId);

        HttpResponse<String> opportunityResponse =
                client.post(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/opportunities",
                        opportunityBody,
                        csrf
                );

        assertThat(opportunityResponse.statusCode())
                .isEqualTo(201);

        assertThat(opportunityResponse.body())
                .contains("\"status\":\"NEW\"")
                .contains("Proyecto E2E");

        String opportunityId =
                extractJsonString(
                        opportunityResponse.body(),
                        "id"
                );

        /*
         * --------------------------------------------------------
         * 5. Crear Follow-up
         * --------------------------------------------------------
         */

        String followUpBody = """
                {
                  "type": "CALL",
                  "scheduledAt": "2030-01-01T10:00:00Z",
                  "notes": "Llamar al cliente"
                }
                """;

        HttpResponse<String> followUpResponse =
                client.post(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/opportunities/"
                                + opportunityId
                                + "/follow-ups",
                        followUpBody,
                        csrf
                );

        assertThat(followUpResponse.statusCode())
                .isEqualTo(201);

        assertThat(followUpResponse.body())
                .contains("\"status\":\"PENDING\"");

        String followUpId =
                extractJsonString(
                        followUpResponse.body(),
                        "id"
                );

        /*
         * --------------------------------------------------------
         * 6. Crear Quote
         * --------------------------------------------------------
         */

        String quoteBody = """
                {
                  "amount": 1500.00,
                  "currency": "EUR",
                  "expiresAt": "2030-01-10T10:00:00Z",
                  "notes": "Presupuesto E2E"
                }
                """;

        HttpResponse<String> quoteResponse =
                client.post(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/opportunities/"
                                + opportunityId
                                + "/quotes",
                        quoteBody,
                        csrf
                );

        assertThat(quoteResponse.statusCode())
                .isEqualTo(201);

        assertThat(quoteResponse.body())
                .contains("\"status\":\"DRAFT\"");

        String quoteId =
                extractJsonString(
                        quoteResponse.body(),
                        "id"
                );

        /*
         * --------------------------------------------------------
         * 7. Enviar Quote
         * DRAFT -> SENT
         * --------------------------------------------------------
         */

        HttpResponse<String> sendResponse =
                client.post(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/quotes/"
                                + quoteId
                                + "/send",
                        csrf
                );

        assertThat(sendResponse.statusCode())
                .isEqualTo(200);

        assertThat(sendResponse.body())
                .contains("\"status\":\"SENT\"");

        /*
         * --------------------------------------------------------
         * 8. Opportunity debe pasar:
         * NEW -> PROPOSAL_SENT
         * --------------------------------------------------------
         */

        HttpResponse<String> proposalOpportunity =
                client.get(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/opportunities/"
                                + opportunityId
                );

        assertThat(proposalOpportunity.statusCode())
                .isEqualTo(200);

        assertThat(proposalOpportunity.body())
                .contains(
                        "\"status\":\"PROPOSAL_SENT\""
                );

        /*
         * --------------------------------------------------------
         * 9. Aceptar Quote
         * SENT -> ACCEPTED
         * --------------------------------------------------------
         */

        HttpResponse<String> acceptResponse =
                client.post(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/quotes/"
                                + quoteId
                                + "/accept",
                        csrf
                );

        assertThat(acceptResponse.statusCode())
                .isEqualTo(200);

        assertThat(acceptResponse.body())
                .contains("\"status\":\"ACCEPTED\"");

        /*
         * --------------------------------------------------------
         * 10. Opportunity debe quedar WON
         * --------------------------------------------------------
         */

        HttpResponse<String> wonOpportunity =
                client.get(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/opportunities/"
                                + opportunityId
                );

        assertThat(wonOpportunity.statusCode())
                .isEqualTo(200);

        assertThat(wonOpportunity.body())
                .contains("\"status\":\"WON\"");

        assertThat(wonOpportunity.body())
                .contains("\"closedAt\":");

        /*
         * --------------------------------------------------------
         * 11. Follow-up pendiente debe quedar CANCELLED
         * --------------------------------------------------------
         */

        HttpResponse<String> cancelledFollowUp =
                client.get(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/follow-ups/"
                                + followUpId
                );

        assertThat(cancelledFollowUp.statusCode())
                .isEqualTo(200);

        assertThat(cancelledFollowUp.body())
                .contains("\"status\":\"CANCELLED\"");

        /*
         * --------------------------------------------------------
         * 12. Comprobar Activities
         * --------------------------------------------------------
         */

        HttpResponse<String> activities =
                client.get(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/opportunities/"
                                + opportunityId
                                + "/activities"
                );

        assertThat(activities.statusCode())
                .isEqualTo(200);

        assertThat(activities.body())
                .contains("OPPORTUNITY_CREATED")
                .contains("QUOTE_SENT")
                .contains("STATUS_CHANGE");

        /*
         * --------------------------------------------------------
         * 13. Comprobar Dashboard
         * --------------------------------------------------------
         */

        HttpResponse<String> dashboard =
                client.get(
                        "/api/v1/organizations/"
                                + organizationId
                                + "/dashboard/today"
                );

        assertThat(dashboard.statusCode())
                .isEqualTo(200);

        /*
         * La única oportunidad del test ya está WON.
         */

        assertThat(dashboard.body())
                .contains(
                        "\"openOpportunities\":0"
                );

        /*
         * El único follow-up fue cancelado automáticamente.
         */

        assertThat(dashboard.body())
                .contains(
                        "\"pendingFollowUps\":0"
                );
    }

    /*
     * ============================================================
     * CLIENTE HTTP DE TEST
     * ============================================================
     */

    private static class SessionClient {

        private final String baseUrl;

        private final HttpClient client;

        private final CookieManager cookieManager;

        SessionClient(int port) {

            this.baseUrl =
                    "http://localhost:" + port;

            this.cookieManager =
                    new CookieManager();

            cookieManager.setCookiePolicy(
                    CookiePolicy.ACCEPT_ALL
            );

            this.client =
                    HttpClient.newBuilder()
                            .cookieHandler(
                                    cookieManager
                            )
                            .build();
        }

        /*
         * --------------------------------------------------------
         * GET CSRF
         * --------------------------------------------------------
         */

        CsrfData getCsrf()
                throws Exception {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            baseUrl
                                                    + "/api/v1/auth/csrf"
                                    )
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            assertThat(response.statusCode())
                    .isEqualTo(200);

            HttpCookie csrfCookie =
                    cookieManager
                            .getCookieStore()
                            .getCookies()
                            .stream()
                            .filter(cookie ->
                                    cookie.getName()
                                            .equals(
                                                    "XSRF-TOKEN"
                                            )
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "XSRF-TOKEN cookie not found"
                                    )
                            );

            return new CsrfData(
                    "X-XSRF-TOKEN",
                    csrfCookie.getValue()
            );
        }

        /*
         * --------------------------------------------------------
         * GET
         * --------------------------------------------------------
         */

        HttpResponse<String> get(
                String path
        ) throws Exception {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            baseUrl + path
                                    )
                            )
                            .GET()
                            .build();

            return client.send(
                    request,
                    HttpResponse.BodyHandlers
                            .ofString()
            );
        }

        /*
         * --------------------------------------------------------
         * POST CON JSON + CSRF
         * --------------------------------------------------------
         */

        HttpResponse<String> post(
                String path,
                String body,
                CsrfData csrf
        ) throws Exception {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            baseUrl + path
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    csrf.headerName(),
                                    csrf.token()
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(body)
                            )
                            .build();

            return client.send(
                    request,
                    HttpResponse.BodyHandlers
                            .ofString()
            );
        }

        /*
         * --------------------------------------------------------
         * POST SIN BODY + CSRF
         *
         * Se usa para:
         * /send
         * /accept
         * /reject
         * /complete
         * etc.
         * --------------------------------------------------------
         */

        HttpResponse<String> post(
                String path,
                CsrfData csrf
        ) throws Exception {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            baseUrl + path
                                    )
                            )
                            .header(
                                    csrf.headerName(),
                                    csrf.token()
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .noBody()
                            )
                            .build();

            return client.send(
                    request,
                    HttpResponse.BodyHandlers
                            .ofString()
            );
        }

        /*
         * --------------------------------------------------------
         * POST SIN CSRF
         *
         * Se usa para comprobar que Spring Security
         * rechaza correctamente la petición.
         * --------------------------------------------------------
         */

        HttpResponse<String> postWithoutCsrf(
                String path,
                String body
        ) throws Exception {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            baseUrl + path
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(body)
                            )
                            .build();

            return client.send(
                    request,
                    HttpResponse.BodyHandlers
                            .ofString()
            );
        }
    }

    /*
     * ============================================================
     * EXTRAER UN STRING DEL JSON
     *
     * Lo usamos solamente para IDs de las respuestas.
     * NO se utiliza para CSRF.
     * ============================================================
     */

    private static String extractJsonString(
            String json,
            String field
    ) {

        Pattern pattern =
                Pattern.compile(
                        "\""
                                + Pattern.quote(field)
                                + "\"\\s*:\\s*\"([^\"]+)\""
                );

        Matcher matcher =
                pattern.matcher(json);

        if (!matcher.find()) {

            throw new IllegalStateException(
                    "Field not found in JSON: "
                            + field
                            + "\n"
                            + json
            );
        }

        return matcher.group(1);
    }

    /*
     * ============================================================
     * DATOS CSRF
     * ============================================================
     */

    private record CsrfData(
            String headerName,
            String token
    ) {
    }
}