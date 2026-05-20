package integracao;

import br.com.adacourse.services.AuthService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@QuarkusTest
public class AuthResourceTest {

    @InjectMock
    AuthService authService;

    @Test
    public void testLoginSucesso() {
        // ARRANGE
        Map<String, String> payload = Map.of("email", "admin@ada.com", "senha", "123");
        when(authService.autenticacao("admin@ada.com", "123")).thenReturn("token_jwt_fake");

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().post("/auth/login")

                // ASSERT
                .then()
                .statusCode(200)
                .body("token", is("token_jwt_fake"));
    }

    @Test
    public void testLoginFalha() {
        // ARRANGE
        Map<String, String> payload = Map.of("email", "admin@ada.com", "senha", "senha_errada");
        doThrow(new WebApplicationException("Credenciais inválidas", Response.Status.UNAUTHORIZED))
                .when(authService).autenticacao("admin@ada.com", "senha_errada");

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().post("/auth/login")

                // ASSERT
                .then()
                .statusCode(401);
    }
}

