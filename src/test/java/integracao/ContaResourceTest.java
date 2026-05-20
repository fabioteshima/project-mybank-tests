package integracao;

import br.com.adacourse.enums.TipoTransacao;
import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Conta;
import br.com.adacourse.models.Transacao;
import br.com.adacourse.repositories.ContaRepository;
import br.com.adacourse.services.ContaService;
import br.com.adacourse.services.TransacaoService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ContaResourceTest {

    @InjectMock
    TransacaoService transacaoService;

    @InjectMock
    ContaRepository contaRepository;

    @InjectMock
    ContaService contaService;

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testSacarEndpointComSucesso() {
        // ARRANGE
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 50.0);

        Cliente clienteMock = new Cliente();
        clienteMock.setId(1L);
        clienteMock.setNome("Cliente Teste");

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setSaldo(BigDecimal.valueOf(500));
        contaMock.setTitular(clienteMock);

        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(100L);
        transacaoMock.setValor(BigDecimal.valueOf(50.0));
        transacaoMock.setTipo(TipoTransacao.SAQUE);
        transacaoMock.setContaOrigem(contaMock);

        when(contaRepository.findById(contaId)).thenReturn(contaMock);
        when(transacaoService.sacar(anyLong(), any(BigDecimal.class))).thenReturn(transacaoMock);

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/saque")

                // ASSERT
                .then()
                .statusCode(200);
    }

    @Test
    public void testSacarSemAutenticacaoDeveDar401() {
        // ARRANGE
        Map<String, Object> payload = Map.of("valor", 50.0);

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", 1L)
                .when()
                .post("/contas/{id}/saque")

                // ASSERT
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testDepositarEndpointComSucesso() {
        // ARRANGE
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 100.0);

        Cliente clienteMock = new Cliente();
        clienteMock.setId(1L);
        clienteMock.setNome("Cliente Teste");

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setSaldo(BigDecimal.valueOf(600));
        contaMock.setTitular(clienteMock);

        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(101L);
        transacaoMock.setValor(BigDecimal.valueOf(100.0));
        transacaoMock.setTipo(TipoTransacao.DEPOSITO);
        transacaoMock.setContaDestino(contaMock);

        when(contaRepository.findById(contaId)).thenReturn(contaMock);
        when(transacaoService.depositar(anyLong(), any(BigDecimal.class))).thenReturn(transacaoMock);

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/deposito")

                // ASSERT
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirEndpointComSucesso() {
        // ARRANGE
        Long contaOrigemId = 1L;
        Long contaDestinoId = 2L;

        Map<String, Object> contaDestinoPayload = Map.of("id", contaDestinoId);
        Map<String, Object> payload = Map.of(
                "contaDestino", contaDestinoPayload,
                "valor", 150.0
        );

        Cliente titularMock = new Cliente();
        titularMock.setNome("Fulano");
        titularMock.setCpf("123.456.789-00");
        titularMock.setEmail("user@teste.com");

        Conta origenMock = new Conta();
        origenMock.setId(contaOrigemId);
        origenMock.setNumero("12345-6");
        origenMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        origenMock.setSaldo(BigDecimal.valueOf(500.0));
        origenMock.setTitular(titularMock);

        Conta destinoMock = new Conta();
        destinoMock.setId(contaDestinoId);
        destinoMock.setNumero("78910-1");
        destinoMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        destinoMock.setSaldo(BigDecimal.valueOf(150.0));
        destinoMock.setTitular(titularMock);

        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(102L);
        transacaoMock.setValor(BigDecimal.valueOf(150.0));
        transacaoMock.setTipo(TipoTransacao.TRANSFERENCIA);
        transacaoMock.setDataHora(LocalDateTime.now());
        transacaoMock.setContaOrigem(origenMock);
        transacaoMock.setContaDestino(destinoMock);

        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn(transacaoMock);
        Mockito.when(contaRepository.findById(contaOrigemId)).thenReturn(origenMock);
        Mockito.when(contaRepository.findById(contaDestinoId)).thenReturn(destinoMock);

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")

                // ASSERT
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testBuscarSaldoComSucesso() {
        // ARRANGE
        Long contaId = 1L;

        Cliente titularMock = new Cliente();
        titularMock.setNome("Fulano");
        titularMock.setCpf("123.456.789-00");
        titularMock.setEmail("user@teste.com"); // Deve coincidir com o user do @TestSecurity

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setNumero("12345-6");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(1000.0));
        contaMock.setTitular(titularMock);

        Mockito.when(contaService.buscarContaPorId(Mockito.anyLong())).thenReturn(contaMock);

        // ACT
        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")

                // ASSERT
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testCriarContaComSucesso() {
        // ARRANGE
        Map<String, Object> clientePayload = Map.of("id", 1L);
        Map<String, Object> payload = Map.of(
                "tipo", "CORRENTE",
                "cliente", clientePayload
        );

        Cliente titularMock = new Cliente();
        titularMock.setId(1L);
        titularMock.setNome("Cliente Teste");

        Conta contaCriadaMock = new Conta();
        contaCriadaMock.setId(50L);
        contaCriadaMock.setNumero("99999-9");
        contaCriadaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaCriadaMock.setTitular(titularMock);

        Mockito.when(contaService.criarConta(Mockito.any(Conta.class)))
                .thenReturn(contaCriadaMock);

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/contas")

                // ASSERT
                .then()
                .statusCode(201) // Created
                .header("Location", org.hamcrest.Matchers.containsString("/contas/50"))
                .body("id", org.hamcrest.Matchers.equalTo(50))
                .body("numero", org.hamcrest.Matchers.equalTo("99999-9"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testListarContasComSucesso() {
        // ARRANGE
        Cliente titularMock = new Cliente();
        titularMock.setNome("Fulano");
        titularMock.setCpf("123.456.789-00");

        Conta contaMock = new Conta();
        contaMock.setId(1L);
        contaMock.setNumero("12345-6");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(1000.0));
        contaMock.setTitular(titularMock);

        Mockito.when(contaService.listarContas()).thenReturn(List.of(contaMock));

        // ACT
        given()
                .when()
                .get("/contas")

                // ASSERT
                .then()
                .statusCode(200)
                .body("$.size()", org.hamcrest.Matchers.equalTo(1))
                .body("[0].numero", org.hamcrest.Matchers.equalTo("12345-6"));
    }

    @Test
    public void testDepositarComSucesso() {
        // ARRANGE
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 200.0);

        Cliente titularMock = new Cliente();
        titularMock.setId(1L);
        titularMock.setNome("Fulano");

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setNumero("12345-6");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(1200.0));
        contaMock.setTitular(titularMock);

        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(201L);
        transacaoMock.setValor(BigDecimal.valueOf(200.0));
        transacaoMock.setTipo(br.com.adacourse.enums.TipoTransacao.DEPOSITO);
        transacaoMock.setDataHora(java.time.LocalDateTime.now());
        transacaoMock.setContaDestino(contaMock);

        Mockito.when(transacaoService.depositar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn(transacaoMock);
        Mockito.when(contaRepository.findById(contaId))
                .thenReturn(contaMock);

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/deposito")

                // ASSERT
                .then()
                .statusCode(200)
                .body("saldoAtual", org.hamcrest.Matchers.equalTo(1200.0f));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testSacarComSucesso() {
        // ARRANGE
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 50.0);

        Cliente titularMock = new Cliente();
        titularMock.setId(1L);
        titularMock.setNome("Fulano");

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setNumero("12345-6");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(450.0));
        contaMock.setTitular(titularMock);

        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(202L);
        transacaoMock.setValor(BigDecimal.valueOf(50.0));
        transacaoMock.setTipo(br.com.adacourse.enums.TipoTransacao.SAQUE);
        transacaoMock.setDataHora(java.time.LocalDateTime.now());
        transacaoMock.setContaOrigem(contaMock);

        Mockito.when(transacaoService.sacar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn(transacaoMock);
        Mockito.when(contaRepository.findById(contaId))
                .thenReturn(contaMock);

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/saque")

                // ASSERT
                .then()
                .statusCode(200)
                .body("saldoAtual", org.hamcrest.Matchers.equalTo(450.0f));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testCriarContaLançaIllegalArgumentException() {
        // ARRANGE
        Map<String, Object> payload = Map.of("tipo", "CORRENTE", "cliente", Map.of("id", 1L));

        Mockito.when(contaService.criarConta(Mockito.any(Conta.class)))
                .thenThrow(new IllegalArgumentException("Dados inválidos para criação da conta"));

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/contas")

                // ASSERT
                .then()
                .statusCode(400)
                .body("erro", org.hamcrest.Matchers.equalTo("Dados inválidos para criação da conta"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testCriarContaLançaNotFoundException() {
        // ARRANGE
        Map<String, Object> payload = Map.of("tipo", "CORRENTE", "cliente", Map.of("id", 99L));

        Mockito.when(contaService.criarConta(Mockito.any(Conta.class)))
                .thenThrow(new jakarta.ws.rs.NotFoundException("Cliente não encontrado"));

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/contas")

                // ASSERT
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta não encontrada"));
    }

    @Test
    public void testDepositarLançaUnsupportedOperationException() {
        // ARRANGE
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 100.0);

        Mockito.when(transacaoService.depositar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new UnsupportedOperationException("Tipo de conta não permite depósito manual"));

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/deposito")

                // ASSERT
                .then()
                .statusCode(422)
                .body("erro", org.hamcrest.Matchers.equalTo("Tipo de conta não permite depósito manual"));
    }

    @Test
    public void testDepositarLançaIllegalArgumentException() {
        // ARRANGE
        Long contaId = 99L;
        Map<String, Object> payload = Map.of("valor", 100.0);

        Mockito.when(transacaoService.depositar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Conta destino inexistente"));

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/deposito")

                // ASSERT
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta destino inexistente"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testSacarLançaIllegalArgumentException() {
        // ARRANGE
        Long contaId = 99L;
        Map<String, Object> payload = Map.of("valor", 50.0);

        Mockito.when(transacaoService.sacar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Conta não encontrada"));

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/saque")

                // ASSERT
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta não encontrada"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testSacarLançaIllegalStateException() {
        // ARRANGE
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 5000.0);

        Mockito.when(transacaoService.sacar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalStateException("Saldo insuficiente para o saque"));

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/saque")

                // ASSERT
                .then()
                .statusCode(422)
                .body("erro", org.hamcrest.Matchers.equalTo("Saldo insuficiente para o saque"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirLançaIllegalArgumentException() {
        // ARRANGE
        Long contaOrigemId = 1L;
        Map<String, Object> payload = Map.of(
                "valor", 150.0,
                "contaDestino", Map.of("id", 99L)
        );

        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Conta de destino não foi localizada"));

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")

                // ASSERT
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta de destino não foi localizada"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirLançaIllegalStateException() {
        // ARRANGE
        Long contaOrigemId = 1L;
        Map<String, Object> payload = Map.of(
                "valor", 90000.0,
                "contaDestino", Map.of("id", 2L)
        );

        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalStateException("Limite diário de transferência excedido"));

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")

                // ASSERT
                .then()
                .statusCode(422)
                .body("erro", org.hamcrest.Matchers.equalTo("Limite diário de transferência excedido"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testBuscarContaPorIdComoGerente() {
        // ARRANGE
        Long contaId = 1L;

        Cliente titularMock = new Cliente();
        titularMock.setNome("Fulano");
        titularMock.setCpf("123.456.789-00");
        titularMock.setEmail("cliente@comum.com"); // Email diferente do gerente

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setNumero("12345-6");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(1000.0));
        contaMock.setTitular(titularMock);

        Mockito.when(contaService.buscarContaPorId(contaId)).thenReturn(contaMock);

        // ACT
        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")

                // ASSERT
                .then()
                .statusCode(200)
                .body("numero", org.hamcrest.Matchers.equalTo("12345-6"));
    }

    @Test
    @TestSecurity(user = "dono@conta.com", roles = {"CLIENTE"})
    public void testBuscarPropriaContaComoCliente() {
        // ARRANGE
        Long contaId = 2L;

        Cliente titularMock = new Cliente();
        titularMock.setNome("Ciclano");
        titularMock.setCpf("987.654.321-11");
        titularMock.setEmail("dono@conta.com"); // Mesmo email definido no @TestSecurity

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setNumero("54321-0");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.POUPANCA);
        contaMock.setSaldo(BigDecimal.valueOf(50.0));
        contaMock.setTitular(titularMock);

        Mockito.when(contaService.buscarContaPorId(contaId)).thenReturn(contaMock);

        // ACT
        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")

                // ASSERT
                .then()
                .statusCode(200)
                .body("numero", org.hamcrest.Matchers.equalTo("54321-0"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testBuscarContaPorIdInexistente() {
        // ARRANGE
        Long contaId = 999L;
        Mockito.when(contaService.buscarContaPorId(contaId)).thenReturn(null);

        // ACT
        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")

                // ASSERT
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta Id não encontrada"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirComSucesso() {
        // ARRANGE
        Long contaOrigemId = 1L;
        Long contaDestinoId = 2L;

        Map<String, Object> payload = Map.of(
                "valor", 100.0,
                "contaDestino", Map.of("id", contaDestinoId)
        );

        Cliente origTitular = new Cliente();
        origTitular.setId(10L);
        origTitular.setNome("Origem");

        Cliente destTitular = new Cliente();
        destTitular.setId(20L);
        destTitular.setNome("Destino");

        Conta contaOrigemMock = new Conta();
        contaOrigemMock.setId(contaOrigemId);
        contaOrigemMock.setNumero("11111-1");
        contaOrigemMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaOrigemMock.setSaldo(BigDecimal.valueOf(400.0));
        contaOrigemMock.setTitular(origTitular);

        Conta contaDestinoMock = new Conta();
        contaDestinoMock.setId(contaDestinoId);
        contaDestinoMock.setNumero("22222-2");
        contaDestinoMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaDestinoMock.setSaldo(BigDecimal.valueOf(200.0));
        contaDestinoMock.setTitular(destTitular);

        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(303L);
        transacaoMock.setValor(BigDecimal.valueOf(100.0));
        transacaoMock.setTipo(br.com.adacourse.enums.TipoTransacao.TRANSFERENCIA);
        transacaoMock.setDataHora(java.time.LocalDateTime.now());
        transacaoMock.setContaOrigem(contaOrigemMock);
        transacaoMock.setContaDestino(contaDestinoMock);

        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn(transacaoMock);
        Mockito.when(contaRepository.findById(contaOrigemId)).thenReturn(contaOrigemMock);
        Mockito.when(contaRepository.findById(contaDestinoId)).thenReturn(contaDestinoMock);

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")

                // ASSERT
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "cliente_invasor@teste.com", roles = {"CLIENTE"})
    public void testBuscarContaDeOutroClienteLançaForbidden() {
        // ARRANGE
        Long contaId = 1L;

        Cliente titularMock = new Cliente();
        titularMock.setNome("Fulano");
        titularMock.setCpf("123.456.789-00");
        titularMock.setEmail("fulano_dono@teste.com"); // Email diferente do "cliente_invasor"

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setNumero("12345-6");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(1000.0));
        contaMock.setTitular(titularMock);

        Mockito.when(contaService.buscarContaPorId(contaId)).thenReturn(contaMock);

        // ACT
        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")

                // ASSERT
                .then()
                .statusCode(403)
                .body("erro", org.hamcrest.Matchers.equalTo("Acesso não autorizado"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirLançaIllegalArgumentExceptionContaNaoEncontrada() {
        // ARRANGE
        Long contaOrigemId = 1L;
        Long contaDestinoInexistenteId = 999L;

        Map<String, Object> payload = Map.of(
                "valor", 100.0,
                "contaDestino", Map.of("id", contaDestinoInexistenteId)
        );

        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Conta de origem ou destino não encontrada no sistema"));

        // ACT
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")

                // ASSERT
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta de origem ou destino não encontrada no sistema"));
    }
}