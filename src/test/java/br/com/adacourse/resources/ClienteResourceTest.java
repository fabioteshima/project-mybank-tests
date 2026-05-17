package br.com.adacourse.resources;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.enums.TipoCliente;
import br.com.adacourse.services.ClienteService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class ClienteResourceTest {

    @InjectMock
    ClienteService clienteService;

    @Test
    @TestSecurity(user = "gerenteTest", roles = {"GERENTE"})
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
    @TestSecurity(user = "gerenteTest", roles = {"GERENTE"})
    public void testCadastrarClienteComSucesso() {
        String payloadValido = """
            {
                "nome": "Fabio",
                "cpf": "12345678901",
                "email": "fabio@email.com",
                "senha": "senhaMestra123"
            }
        """;

        Cliente clienteSalvo = new Cliente();
        clienteSalvo.setId(1L);
        clienteSalvo.setNome("Fabio");
        clienteSalvo.setCpf("12345678901");
        clienteSalvo.setEmail("fabio@email.com");
        clienteSalvo.setRole(TipoCliente.CLIENTE);

        when(clienteService.cadastrarCliente(any(Cliente.class))).thenReturn(clienteSalvo);

        given()
                .contentType(ContentType.JSON)
                .body(payloadValido)
                .when().post("/clientes")
                .then()
                .statusCode(201) // Seu resource usa Response.created() que devolve 201
                .header("Location", endsWith("/clientes/1"))
                .body("id", notNullValue())
                .body("nome", is("Fabio"));
    }

    @Test
    @TestSecurity(user = "gerenteTest", roles = {"GERENTE"})
    public void testCadastrarClienteComPayloadInvalido() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/clientes")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "gerenteTest", roles = {"GERENTE"})
    public void testBuscarClientePorIdComSucesso() {
        Long idExistente = 1L;

        Cliente clienteMock = new Cliente();
        clienteMock.setId(idExistente);
        clienteMock.setNome("Fabio");
        clienteMock.setEmail("fabio@email.com");

        when(clienteService.buscarClientePorId(idExistente)).thenReturn(clienteMock);

        given()
                .pathParam("id", idExistente)
                .when().get("/clientes/{id}")
                .then()
                .statusCode(200)
                .body("id", is(idExistente.intValue()))
                .body("nome", is("Fabio"));
    }

    @Test
    @TestSecurity(user = "gerenteTest", roles = {"GERENTE"})
    public void testBuscarClientePorIdInexistente() {
        Long idInexistente = 999L;

        // O controller espera receber null do service para retornar 404
        when(clienteService.buscarClientePorId(idInexistente)).thenReturn(null);

        given()
                .pathParam("id", idInexistente)
                .when().get("/clientes/{id}")
                .then()
                .statusCode(404)
                .body("erro", is("Cliente não encontrado"));
    }

    // --- NOVO TESTE: Atualização (PUT) com sucesso ---
    @Test
    @TestSecurity(user = "gerenteTest", roles = {"GERENTE"})
    public void testAtualizarClienteComSucesso() {
        Long idExistente = 1L;
        String payloadUpdate = """
            {
                "nome": "Fabio Alterado",
                "email": "fabio.novo@email.com",
                "senha": "novaSenha123"
            }
        """;

        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setId(idExistente);
        clienteAtualizado.setNome("Fabio Alterado");
        clienteAtualizado.setEmail("fabio.novo@email.com");

        when(clienteService.atualizarCliente(eq(idExistente), any(Cliente.class)))
                .thenReturn(clienteAtualizado);

        given()
                .contentType(ContentType.JSON)
                .body(payloadUpdate)
                .pathParam("id", idExistente)
                .when().put("/clientes/{id}")
                .then()
                .statusCode(200)
                .body("nome", is("Fabio Alterado"))
                .body("email", is("fabio.novo@email.com"));
    }

    // --- NOVO TESTE: Atualização (PUT) ID inexistente ---
    @Test
    @TestSecurity(user = "gerenteTest", roles = {"GERENTE"})
    public void testAtualizarClienteIdInexistente() {
        Long idInexistente = 999L;
        String payloadUpdate = """
            {
                "nome": "Fabio",
                "email": "fabio@email.com",
                "senha": "senhaMestra123"
            }
        """;

        when(clienteService.atualizarCliente(eq(idInexistente), any(Cliente.class)))
                .thenReturn(null);

        given()
                .contentType(ContentType.JSON)
                .body(payloadUpdate)
                .pathParam("id", idInexistente)
                .when().put("/clientes/{id}")
                .then()
                .statusCode(404)
                .body("erro", is("Cliente Id não encontrado"));
    }

    @Test
    @TestSecurity(user = "gerenteTest", roles = {"GERENTE"})
    public void testAtualizarClienteComDadosInvalidosLancaException() {
        Long idExistente = 1L;
        String payloadUpdate = """
            {
                "nome": "Fabio",
                "email": "email.duplicado@email.com",
                "senha": "senhaMestra123"
            }
        """;

        // Força o service a lançar a exceção que o seu catch está esperando capturar
        when(clienteService.atualizarCliente(eq(idExistente), any(Cliente.class)))
                .thenThrow(new IllegalArgumentException("E-mail já cadastrado em outra conta"));

        given()
                .contentType(ContentType.JSON)
                .body(payloadUpdate)
                .pathParam("id", idExistente)
                .when().put("/clientes/{id}")
                .then()
                .statusCode(400) // Bad Request conforme configurado no seu catch
                .body("erro", is("E-mail já cadastrado em outra conta"));
    }
}