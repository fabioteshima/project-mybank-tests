package br.com.adacourse.resources;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.services.ClienteService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ClienteResourceTest {

    @InjectMock
    ClienteService clienteService;

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN", "GERENTE"})
    public void testListarClientesEndpoint() {
        Cliente c1 = new Cliente();
        c1.setId(1L);
        c1.setNome("Ada Lovelace");

        when(clienteService.listarClientes()).thenReturn(List.of(c1));

        given()
                .when().get("/clientes")
                .then()
                .statusCode(200)
                .body("[0].nome", is("Ada Lovelace"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN", "GERENTE"})
    public void testCadastrarClienteComPayloadInvalido() {
        // Enviando JSON vazio para disparar as validações do @Valid / @NotBlank
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/clientes")
                .then()
                .statusCode(400); // Bad Request pelas anotações de validação do DTO
    }
}