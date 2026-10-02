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
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.HttpCookie;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

                SessionClient client = new SessionClient(port);

                CsrfData csrf = client.getCsrf();

                String email = "integration-"
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

                HttpResponse<String> registerResponse = client.post(
                                "/api/v1/auth/register",
                                body,
                                csrf);

                assertThat(registerResponse.statusCode())
                                .isEqualTo(201);

                assertThat(registerResponse.body())
                                .contains(email)
                                .contains("Empresa Integration")
                                .contains("OWNER");

                HttpResponse<String> meResponse = client.get(
                                "/api/v1/auth/me");

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

                SessionClient client = new SessionClient(port);

                String email = "csrf-"
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

                HttpResponse<String> response = client.postWithoutCsrf(
                                "/api/v1/auth/register",
                                body);

                assertThat(response.statusCode())
                                .isEqualTo(403);

                assertThat(response.body())
                                .contains("ACCESS_DENIED");
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

                SessionClient client = new SessionClient(port);

                /*
                 * --------------------------------------------------------
                 * 1. Obtener CSRF
                 * --------------------------------------------------------
                 */

                CsrfData csrf = client.getCsrf();

                String email = "commercial-"
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

                HttpResponse<String> registerResponse = client.post(
                                "/api/v1/auth/register",
                                registerBody,
                                csrf);

                assertThat(registerResponse.statusCode())
                                .isEqualTo(201);

                assertThat(registerResponse.body())
                                .contains(email)
                                .contains("Empresa E2E")
                                .contains("OWNER");

                String organizationId = extractJsonString(
                                registerResponse.body(),
                                "organizationId");

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

                HttpResponse<String> customerResponse = client.post(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/customers",
                                customerBody,
                                csrf);

                assertThat(customerResponse.statusCode())
                                .isEqualTo(201);

                assertThat(customerResponse.body())
                                .contains("Cliente E2E");

                String customerId = extractJsonString(
                                customerResponse.body(),
                                "id");

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

                HttpResponse<String> opportunityResponse = client.post(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/opportunities",
                                opportunityBody,
                                csrf);

                assertThat(opportunityResponse.statusCode())
                                .isEqualTo(201);

                assertThat(opportunityResponse.body())
                                .contains("\"status\":\"NEW\"")
                                .contains("Proyecto E2E");

                String opportunityId = extractJsonString(
                                opportunityResponse.body(),
                                "id");

                // --------------------------------------------------------
                // 4.1 Comprobar que la oportunidad tiene el customer correcto
                // --------------------------------------------------------
                Instant followUpDate = Instant.now()
                                .plusSeconds(7 * 24 * 60 * 60);

                Instant quoteExpirationDate = Instant.now()
                                .plusSeconds(14 * 24 * 60 * 60);
                /*
                 * --------------------------------------------------------
                 * 5. Crear Follow-up
                 * --------------------------------------------------------
                 */

                String followUpBody = """
                                {
                                  "type": "CALL",
                                  "scheduledAt": "%s",
                                  "notes": "Llamar al cliente"
                                }
                                """.formatted(
                                followUpDate);

                HttpResponse<String> followUpResponse = client.post(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/opportunities/"
                                                + opportunityId
                                                + "/follow-ups",
                                followUpBody,
                                csrf);

                assertThat(followUpResponse.statusCode())
                                .isEqualTo(201);

                assertThat(followUpResponse.body())
                                .contains("\"status\":\"PENDING\"");

                String followUpId = extractJsonString(
                                followUpResponse.body(),
                                "id");

                /*
                 * --------------------------------------------------------
                 * 6. Crear Quote
                 * --------------------------------------------------------
                 */

                String quoteBody = """
                                {
                                  "amount": 1500.00,
                                  "currency": "EUR",
                                  "expiresAt": "%s",
                                  "notes": "Presupuesto E2E"
                                }
                                """.formatted(
                                quoteExpirationDate);

                HttpResponse<String> quoteResponse = client.post(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/opportunities/"
                                                + opportunityId
                                                + "/quotes",
                                quoteBody,
                                csrf);

                assertThat(quoteResponse.statusCode())
                                .isEqualTo(201);

                assertThat(quoteResponse.body())
                                .contains("\"status\":\"DRAFT\"");

                String quoteId = extractJsonString(
                                quoteResponse.body(),
                                "id");

                /*
                 * --------------------------------------------------------
                 * 7. Enviar Quote
                 * DRAFT -> SENT
                 * --------------------------------------------------------
                 */

                HttpResponse<String> sendResponse = client.post(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/quotes/"
                                                + quoteId
                                                + "/send",
                                csrf);

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

                HttpResponse<String> proposalOpportunity = client.get(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/opportunities/"
                                                + opportunityId);

                assertThat(proposalOpportunity.statusCode())
                                .isEqualTo(200);

                assertThat(proposalOpportunity.body())
                                .contains(
                                                "\"status\":\"PROPOSAL_SENT\"");

                /*
                 * --------------------------------------------------------
                 * 9. Aceptar Quote
                 * SENT -> ACCEPTED
                 * --------------------------------------------------------
                 */

                HttpResponse<String> acceptResponse = client.post(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/quotes/"
                                                + quoteId
                                                + "/accept",
                                csrf);

                assertThat(acceptResponse.statusCode())
                                .isEqualTo(200);

                assertThat(acceptResponse.body())
                                .contains("\"status\":\"ACCEPTED\"");

                /*
                 * --------------------------------------------------------
                 * 10. Opportunity debe quedar WON
                 * --------------------------------------------------------
                 */

                HttpResponse<String> wonOpportunity = client.get(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/opportunities/"
                                                + opportunityId);

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

                HttpResponse<String> cancelledFollowUp = client.get(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/follow-ups/"
                                                + followUpId);

                assertThat(cancelledFollowUp.statusCode())
                                .isEqualTo(200);

                assertThat(cancelledFollowUp.body())
                                .contains("\"status\":\"CANCELLED\"");

                /*
                 * --------------------------------------------------------
                 * 12. Comprobar Activities
                 * --------------------------------------------------------
                 */

                HttpResponse<String> activities = client.get(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/opportunities/"
                                                + opportunityId
                                                + "/activities");

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

                HttpResponse<String> dashboard = client.get(
                                "/api/v1/organizations/"
                                                + organizationId
                                                + "/dashboard/today");

                assertThat(dashboard.statusCode())
                                .isEqualTo(200);

                /*
                 * La única oportunidad del test ya está WON.
                 */

                assertThat(dashboard.body())
                                .contains(
                                                "\"openOpportunities\":0");

                /*
                 * El único follow-up fue cancelado automáticamente.
                 */

                assertThat(dashboard.body())
                                .contains(
                                                "\"pendingFollowUps\":0");
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

                        this.baseUrl = "http://localhost:" + port;

                        this.cookieManager = new CookieManager();

                        cookieManager.setCookiePolicy(
                                        CookiePolicy.ACCEPT_ALL);

                        this.client = HttpClient.newBuilder()
                                        .cookieHandler(
                                                        cookieManager)
                                        .build();
                }

                /*
                 * --------------------------------------------------------
                 * GET CSRF
                 * --------------------------------------------------------
                 */

                CsrfData getCsrf()
                                throws Exception {

                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(
                                                        URI.create(
                                                                        baseUrl
                                                                                        + "/api/v1/auth/csrf"))
                                        .GET()
                                        .build();

                        HttpResponse<String> response = client.send(
                                        request,
                                        HttpResponse.BodyHandlers
                                                        .ofString());

                        assertThat(response.statusCode())
                                        .isEqualTo(200);

                        HttpCookie csrfCookie = cookieManager
                                        .getCookieStore()
                                        .getCookies()
                                        .stream()
                                        .filter(cookie -> cookie.getName()
                                                        .equals(
                                                                        "XSRF-TOKEN"))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalStateException(
                                                        "XSRF-TOKEN cookie not found"));

                        return new CsrfData(
                                        "X-XSRF-TOKEN",
                                        csrfCookie.getValue());
                }

                /*
                 * --------------------------------------------------------
                 * GET
                 * --------------------------------------------------------
                 */

                HttpResponse<String> get(
                                String path) throws Exception {

                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(
                                                        URI.create(
                                                                        baseUrl + path))
                                        .GET()
                                        .build();

                        return client.send(
                                        request,
                                        HttpResponse.BodyHandlers
                                                        .ofString());
                }

                /*
                 * --------------------------------------------------------
                 * POST CON JSON + CSRF
                 * --------------------------------------------------------
                 */

                HttpResponse<String> post(
                                String path,
                                String body,
                                CsrfData csrf) throws Exception {

                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(
                                                        URI.create(
                                                                        baseUrl + path))
                                        .header(
                                                        "Content-Type",
                                                        "application/json")
                                        .header(
                                                        csrf.headerName(),
                                                        csrf.token())
                                        .POST(
                                                        HttpRequest.BodyPublishers
                                                                        .ofString(body))
                                        .build();

                        return client.send(
                                        request,
                                        HttpResponse.BodyHandlers
                                                        .ofString());
                }

                // --------------------------------------------------------
                // Extraer el valor de una cookie por nombre
                // --------------------------------------------------------
                String getCookieValue(
                                String cookieName) {

                        return cookieManager
                                        .getCookieStore()
                                        .getCookies()
                                        .stream()
                                        .filter(cookie -> cookie.getName()
                                                        .equals(cookieName))
                                        .map(HttpCookie::getValue)
                                        .findFirst()
                                        .orElse(null);
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
                                CsrfData csrf) throws Exception {

                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(
                                                        URI.create(
                                                                        baseUrl + path))
                                        .header(
                                                        csrf.headerName(),
                                                        csrf.token())
                                        .POST(
                                                        HttpRequest.BodyPublishers
                                                                        .noBody())
                                        .build();

                        return client.send(
                                        request,
                                        HttpResponse.BodyHandlers
                                                        .ofString());
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
                                String body) throws Exception {

                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(
                                                        URI.create(
                                                                        baseUrl + path))
                                        .header(
                                                        "Content-Type",
                                                        "application/json")
                                        .POST(
                                                        HttpRequest.BodyPublishers
                                                                        .ofString(body))
                                        .build();

                        return client.send(
                                        request,
                                        HttpResponse.BodyHandlers
                                                        .ofString());
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
                        String field) {

                Pattern pattern = Pattern.compile(
                                "\""
                                                + Pattern.quote(field)
                                                + "\"\\s*:\\s*\"([^\"]+)\"");

                Matcher matcher = pattern.matcher(json);

                if (!matcher.find()) {

                        throw new IllegalStateException(
                                        "Field not found in JSON: "
                                                        + field
                                                        + "\n"
                                                        + json);
                }

                return matcher.group(1);
        }

        @Test
        void invalidUuidReturnsBadRequest()
                        throws Exception {

                SessionClient client = new SessionClient(port);

                CsrfData csrf = client.getCsrf();

                String email = "invalid-uuid-"
                                + UUID.randomUUID()
                                + "@test.com";

                String registerBody = """
                                {
                                  "email": "%s",
                                  "password": "Password123!",
                                  "firstName": "Invalid",
                                  "lastName": "Uuid",
                                  "organizationName": "Empresa UUID Test"
                                }
                                """.formatted(email);

                HttpResponse<String> registerResponse = client.post(
                                "/api/v1/auth/register",
                                registerBody,
                                csrf);

                assertThat(registerResponse.statusCode())
                                .isEqualTo(201);

                /*
                 * organizationId debería ser UUID,
                 * pero enviamos deliberadamente texto.
                 */
                HttpResponse<String> response = client.get(
                                "/api/v1/organizations/"
                                                + "esto-no-es-un-uuid"
                                                + "/customers");

                assertThat(response.statusCode())
                                .isEqualTo(400);

                assertThat(response.body())
                                .contains("VALIDATION_FAILED");
        }

        @Test
        void malformedJsonReturnsBadRequest()
                        throws Exception {

                SessionClient client = new SessionClient(port);

                CsrfData csrf = client.getCsrf();

                /*
                 * Este JSON es inválido porque tiene
                 * una coma antes de cerrar la llave.
                 */
                String malformedBody = """
                                {
                                  "email": "broken@test.com",
                                  "password": "Password123!",
                                }
                                """;

                HttpResponse<String> response = client.post(
                                "/api/v1/auth/register",
                                malformedBody,
                                csrf);

                assertThat(response.statusCode())
                                .isEqualTo(400);

                assertThat(response.body())
                                .contains("VALIDATION_FAILED");
        }

        @Test
        void successfulLoginChangesExistingSessionId()
                        throws Exception {

                SessionClient client = new SessionClient(port);

                CsrfData csrf = client.getCsrf();

                String email = "session-fixation-"
                                + UUID.randomUUID()
                                + "@test.com";

                String password = "Password123!";

                String registerBody = """
                                {
                                  "email": "%s",
                                  "password": "%s",
                                  "firstName": "Session",
                                  "lastName": "Test",
                                  "organizationName": "Empresa Session Test"
                                }
                                """.formatted(
                                email,
                                password);

                /*
                 * Registramos al usuario.
                 *
                 * El registro también autentica al usuario
                 * y crea una HttpSession.
                 */
                HttpResponse<String> registerResponse = client.post(
                                "/api/v1/auth/register",
                                registerBody,
                                csrf);

                assertThat(registerResponse.statusCode())
                                .isEqualTo(201);

                String sessionIdBeforeLogin = client.getCookieValue(
                                "JSESSIONID");

                assertThat(sessionIdBeforeLogin)
                                .isNotNull()
                                .isNotBlank();

                /*
                 * Obtenemos un CSRF válido antes del nuevo login.
                 */
                csrf = client.getCsrf();

                String loginBody = """
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(
                                email,
                                password);

                /*
                 * Hacemos login utilizando una sesión
                 * que ya existe.
                 */
                HttpResponse<String> loginResponse = client.post(
                                "/api/v1/auth/login",
                                loginBody,
                                csrf);

                assertThat(loginResponse.statusCode())
                                .isEqualTo(200);

                String sessionIdAfterLogin = client.getCookieValue(
                                "JSESSIONID");

                assertThat(sessionIdAfterLogin)
                                .isNotNull()
                                .isNotBlank();

                /*
                 * La protección frente a session fixation
                 * debe haber regenerado el identificador.
                 */
                assertThat(sessionIdAfterLogin)
                                .isNotEqualTo(
                                                sessionIdBeforeLogin);
        }

        @Test
        void unauthenticatedUserReceives401ApiError()
                        throws Exception {

                SessionClient client = new SessionClient(port);

                HttpResponse<String> response = client.get(
                                "/api/v1/organizations/"
                                                + UUID.randomUUID()
                                                + "/customers");

                assertThat(response.statusCode())
                                .isEqualTo(401);

                assertThat(response.body())
                                .contains("UNAUTHENTICATED")
                                .contains("Authentication is required");
        }

        @Test
        void organizationCannotAccessCustomerFromAnotherOrganization()
                        throws Exception {

                /*
                 * ============================================================
                 * ORGANIZACIÓN A
                 * ============================================================
                 */

                SessionClient clientA = new SessionClient(port);

                CsrfData csrfA = clientA.getCsrf();

                String emailA = "tenant-a-"
                                + UUID.randomUUID()
                                + "@test.com";

                String registerBodyA = """
                                {
                                  "email": "%s",
                                  "password": "Password123!",
                                  "firstName": "Tenant",
                                  "lastName": "A",
                                  "organizationName": "Empresa A"
                                }
                                """.formatted(emailA);

                HttpResponse<String> registerResponseA = clientA.post(
                                "/api/v1/auth/register",
                                registerBodyA,
                                csrfA);

                assertThat(registerResponseA.statusCode())
                                .isEqualTo(201);

                String organizationAId = extractJsonString(
                                registerResponseA.body(),
                                "organizationId");

                csrfA = clientA.getCsrf();

                /*
                 * Creamos un cliente que pertenece
                 * exclusivamente a la organización A.
                 */
                String customerBody = """
                                {
                                  "name": "Cliente privado A",
                                  "companyName": "Empresa privada",
                                  "email": "privado-a@test.com",
                                  "phone": "600000001",
                                  "notes": "Solo pertenece a A"
                                }
                                """;

                HttpResponse<String> customerResponse = clientA.post(
                                "/api/v1/organizations/"
                                                + organizationAId
                                                + "/customers",
                                customerBody,
                                csrfA);

                assertThat(customerResponse.statusCode())
                                .isEqualTo(201);

                String customerAId = extractJsonString(
                                customerResponse.body(),
                                "id");

                /*
                 * ============================================================
                 * ORGANIZACIÓN B
                 * ============================================================
                 */

                SessionClient clientB = new SessionClient(port);

                CsrfData csrfB = clientB.getCsrf();

                String emailB = "tenant-b-"
                                + UUID.randomUUID()
                                + "@test.com";

                String registerBodyB = """
                                {
                                  "email": "%s",
                                  "password": "Password123!",
                                  "firstName": "Tenant",
                                  "lastName": "B",
                                  "organizationName": "Empresa B"
                                }
                                """.formatted(emailB);

                HttpResponse<String> registerResponseB = clientB.post(
                                "/api/v1/auth/register",
                                registerBodyB,
                                csrfB);

                assertThat(registerResponseB.statusCode())
                                .isEqualTo(201);

                String organizationBId = extractJsonString(
                                registerResponseB.body(),
                                "organizationId");

                /*
                 * ============================================================
                 * ATAQUE / ACCESO CRUZADO
                 * ============================================================
                 *
                 * El usuario B conoce el UUID del cliente A,
                 * pero intenta consultarlo dentro de su propia organización.
                 */

                HttpResponse<String> crossTenantResponse = clientB.get(
                                "/api/v1/organizations/"
                                                + organizationBId
                                                + "/customers/"
                                                + customerAId);

                /*
                 * Nunca debe recibir los datos del cliente A.
                 */
                assertThat(crossTenantResponse.statusCode())
                                .isEqualTo(404);

                assertThat(crossTenantResponse.body())
                                .contains("CUSTOMER_NOT_FOUND");

                assertThat(crossTenantResponse.body())
                                .doesNotContain("Cliente privado A")
                                .doesNotContain("privado-a@test.com");
        }

        @Test
        void userCannotAccessAnotherOrganizationByOrganizationId()
                        throws Exception {

                /*
                 * ============================================================
                 * ORGANIZACIÓN A
                 * ============================================================
                 */

                SessionClient clientA = new SessionClient(port);

                CsrfData csrfA = clientA.getCsrf();

                String emailA = "tenant-owner-a-"
                                + UUID.randomUUID()
                                + "@test.com";

                String registerBodyA = """
                                {
                                  "email": "%s",
                                  "password": "Password123!",
                                  "firstName": "Tenant",
                                  "lastName": "OwnerA",
                                  "organizationName": "Empresa A"
                                }
                                """.formatted(emailA);

                HttpResponse<String> registerResponseA = clientA.post(
                                "/api/v1/auth/register",
                                registerBodyA,
                                csrfA);

                assertThat(registerResponseA.statusCode())
                                .isEqualTo(201);

                String organizationAId = extractJsonString(
                                registerResponseA.body(),
                                "organizationId");

                /*
                 * ============================================================
                 * ORGANIZACIÓN B
                 * ============================================================
                 */

                SessionClient clientB = new SessionClient(port);

                CsrfData csrfB = clientB.getCsrf();

                String emailB = "tenant-owner-b-"
                                + UUID.randomUUID()
                                + "@test.com";

                String registerBodyB = """
                                {
                                  "email": "%s",
                                  "password": "Password123!",
                                  "firstName": "Tenant",
                                  "lastName": "OwnerB",
                                  "organizationName": "Empresa B"
                                }
                                """.formatted(emailB);

                HttpResponse<String> registerResponseB = clientB.post(
                                "/api/v1/auth/register",
                                registerBodyB,
                                csrfB);

                assertThat(registerResponseB.statusCode())
                                .isEqualTo(201);

                /*
                 * ============================================================
                 * ACCESO CRUZADO
                 * ============================================================
                 *
                 * clientB está autenticado como usuario de B,
                 * pero intenta acceder directamente a la
                 * organización A.
                 */

                HttpResponse<String> response = clientB.get(
                                "/api/v1/organizations/"
                                                + organizationAId);

                /*
                 * El backend no debe revelar que la organización
                 * existe para este usuario.
                 */
                assertThat(response.statusCode())
                                .isEqualTo(404);

                assertThat(response.body())
                                .contains("ORGANIZATION_NOT_FOUND");

                /*
                 * Tampoco debemos filtrar información
                 * de la organización A.
                 */
                assertThat(response.body())
                                .doesNotContain("Empresa A");
        }

        /*
         * ============================================================
         * DATOS CSRF
         * ============================================================
         */

        private record CsrfData(
                        String headerName,
                        String token) {
        }
}