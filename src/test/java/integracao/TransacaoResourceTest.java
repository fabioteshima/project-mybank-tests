package integracao;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Conta;
import br.com.adacourse.models.Transacao;
import br.com.adacourse.services.TransacaoService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TransacaoResourceTest {

    @InjectMock
    TransacaoService transacaoService;

    private Transacao transacaoMock;
    private Conta contaOrigem;

    @BeforeEach
    public void setup() {
        Cliente titular = new Cliente();
        titular.setEmail("cliente@teste.com");
        titular.setNome("Fabio");

        contaOrigem = new Conta();
        contaOrigem.setId(1L);
        contaOrigem.setNumero("12345-6");
        contaOrigem.setTitular(titular);

        transacaoMock = new Transacao();
        transacaoMock.setId(100L);
        transacaoMock.setValor(BigDecimal.valueOf(150.0));
        transacaoMock.setTipo(br.com.adacourse.enums.TipoTransacao.SAQUE);
        transacaoMock.setDataHora(LocalDateTime.now());
        transacaoMock.setContaOrigem(contaOrigem);
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testBuscarTransacoesPorContaComoGerente() {
        // ARRANGE
        Mockito.when(transacaoService.buscarTransacoesPorConta(1L))
                .thenReturn(List.of(transacaoMock));

        // ACT
        given()
                .queryParam("contaId", 1L)
                .when()
                .get("/transacoes")

                // ASSERT
                .then()
                .statusCode(200)
                .body("[0].id", Matchers.equalTo(100));
    }

    @Test
    @TestSecurity(user = "cliente@teste.com", roles = {"CLIENTE"})
    public void testBuscarTransacoesPorContaComoClienteAutorizado() {
        // ARRANGE
        Mockito.when(transacaoService.buscarTransacoesPorConta(1L))
                .thenReturn(List.of(transacaoMock));

        // ACT
        given()
                .queryParam("contaId", 1L)
                .when()
                .get("/transacoes")

                // ASSERT
                .then()
                .statusCode(200)
                .body("[0].id", Matchers.equalTo(100));
    }

    @Test
    @TestSecurity(user = "invasor@teste.com", roles = {"CLIENTE"})
    public void testBuscarTransacoesPorContaComoClienteNaoAutorizado() {
        // ARRANGE
        Mockito.when(transacaoService.buscarTransacoesPorConta(1L))
                .thenReturn(List.of(transacaoMock));

        // ACT
        given()
                .queryParam("contaId", 1L)
                .when()
                .get("/transacoes")

                // ASSERT
                .then()
                .statusCode(403)
                .body("erro", Matchers.equalTo("Acesso não autorizado"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testListarTodasAsTransacoesSemContaIdComoGerente() {
        // ARRANGE
        Mockito.when(transacaoService.listarTransacoes())
                .thenReturn(List.of(transacaoMock));

        // ACT
        given()
                .when()
                .get("/transacoes")

                // ASSERT
                .then()
                .statusCode(200)
                .body("[0].id", Matchers.equalTo(100));
    }

    @Test
    @TestSecurity(user = "cliente@teste.com", roles = {"CLIENTE"})
    public void testListarTodasAsTransacoesSemContaIdComoClienteLançaForbidden() {
        // ACT
        given()
                .when()
                .get("/transacoes")

                // ASSERT
                .then()
                .statusCode(403)
                .body("erro", Matchers.equalTo("Apenas gerente pode listar todas as transações"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testBuscarTransacaoPorIdInexistenteRetornaNotFound() {
        // ARRANGE
        Mockito.when(transacaoService.buscarTransacaoPorId(999L))
                .thenReturn(null);

        // ACT
        given()
                .pathParam("id", 999L)
                .when()
                .get("/transacoes/{id}")

                // ASSERT
                .then()
                .statusCode(404)
                .body("erro", Matchers.equalTo("Transação não encontrada"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testBuscarTransacaoPorIdComoGerente() {
        // ARRANGE
        Mockito.when(transacaoService.buscarTransacaoPorId(100L))
                .thenReturn(transacaoMock);

        // ACT
        given()
                .pathParam("id", 100L)
                .when()
                .get("/transacoes/{id}")

                // ASSERT
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(100));
    }

    @Test
    @TestSecurity(user = "cliente@teste.com", roles = {"CLIENTE"})
    public void testBuscarTransacaoPorIdComoClienteAutorizado() {
        // ARRANGE
        Mockito.when(transacaoService.buscarTransacaoPorId(100L))
                .thenReturn(transacaoMock);

        // ACT
        given()
                .pathParam("id", 100L)
                .when()
                .get("/transacoes/{id}")

                // ASSERT
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(100));
    }

    @Test
    @TestSecurity(user = "invasor@teste.com", roles = {"CLIENTE"})
    public void testBuscarTransacaoPorIdComoClienteNaoAutorizadoRetornaForbidden() {
        // ARRANGE
        Mockito.when(transacaoService.buscarTransacaoPorId(100L))
                .thenReturn(transacaoMock);

        // ACT
        given()
                .pathParam("id", 100L)
                .when()
                .get("/transacoes/{id}")

                // ASSERT
                .then()
                .statusCode(403)
                .body("erro", Matchers.equalTo("Acesso não autorizado"));
    }

    @Test
    @TestSecurity(user = "beneficiario@teste.com", roles = {"CLIENTE"})
    public void testBuscarTransacoesPorContaComoClienteSendoContaDestino() {
        // ARRANGE
        Cliente titularDestino = new Cliente();
        titularDestino.setEmail("beneficiario@teste.com");

        Conta contaDestino = new Conta();
        contaDestino.setId(1L);
        contaDestino.setTitular(titularDestino);

        Transacao depositoMock = new Transacao();
        depositoMock.setId(200L);
        depositoMock.setValor(BigDecimal.valueOf(300.0));
        depositoMock.setTipo(br.com.adacourse.enums.TipoTransacao.DEPOSITO);
        depositoMock.setDataHora(LocalDateTime.now());
        depositoMock.setContaOrigem(null);
        depositoMock.setContaDestino(contaDestino);

        Mockito.when(transacaoService.buscarTransacoesPorConta(1L))
                .thenReturn(List.of(depositoMock));

        // ACT
        given()
                .queryParam("contaId", 1L)
                .when()
                .get("/transacoes")

                // ASSERT
                .then()
                .statusCode(200)
                .body("[0].id", Matchers.equalTo(200));
    }

    @Test
    @TestSecurity(user = "beneficiario@teste.com", roles = {"CLIENTE"})
    public void testBuscarTransacaoPorIdComoClienteSendoContaDestino() {
        // ARRANGE
        Cliente titularDestino = new Cliente();
        titularDestino.setEmail("beneficiario@teste.com");

        Conta contaDestino = new Conta();
        contaDestino.setId(1L);
        contaDestino.setTitular(titularDestino);

        Transacao depositoMock = new Transacao();
        depositoMock.setId(200L);
        depositoMock.setValor(BigDecimal.valueOf(300.0));
        depositoMock.setTipo(br.com.adacourse.enums.TipoTransacao.DEPOSITO);
        depositoMock.setDataHora(LocalDateTime.now());
        depositoMock.setContaOrigem(null);
        depositoMock.setContaDestino(contaDestino);

        Mockito.when(transacaoService.buscarTransacaoPorId(200L)).thenReturn(depositoMock);

        // ACT
        given()
                .pathParam("id", 200L)
                .when()
                .get("/transacoes/{id}")

                // ASSERT
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(200));
    }

    @Test
    @TestSecurity(user = "usuario_aleatorio@teste.com", roles = {"CLIENTE"})
    public void testBuscarTransacaoComContasNulasRetornaForbidden() {
        // ARRANGE
        Transacao transacaoSemContas = new Transacao();
        transacaoSemContas.setId(500L);
        transacaoSemContas.setValor(BigDecimal.valueOf(5.0));
        transacaoSemContas.setDataHora(LocalDateTime.now());
        transacaoSemContas.setContaOrigem(null);
        transacaoSemContas.setContaDestino(null);

        Mockito.when(transacaoService.buscarTransacaoPorId(500L)).thenReturn(transacaoSemContas);

        // ACT
        given()
                .pathParam("id", 500L)
                .when()
                .get("/transacoes/{id}")

                // ASSERT
                .then()
                .statusCode(403)
                .body("erro", Matchers.equalTo("Acesso não autorizado"));
    }
}