package br.com.adacourse.resources;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Transacao;
import br.com.adacourse.models.Conta;
import br.com.adacourse.repositories.ContaRepository;
import br.com.adacourse.services.ContaService;
import br.com.adacourse.services.TransacaoService;
import br.com.adacourse.enums.TipoTransacao;

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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
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

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/saque")
                .then()
                .statusCode(200);
    }

    @Test
    public void testSacarSemAutenticacaoDeveDar401() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("valor", 50.0))
                .pathParam("id", 1L)
                .when().post("/contas/{id}/saque")
                .then()
                .statusCode(401);
    }

    // ------------------------------------------------------------------------
    // NOVOS TESTES: Depósito, Transferência e Consulta
    // ------------------------------------------------------------------------

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testDepositarEndpointComSucesso() {
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 100.0);

        // CORREÇÃO: Criando o titular para evitar o NullPointerException no DTO de resposta
        Cliente clienteMock = new Cliente();
        clienteMock.setId(1L);
        clienteMock.setNome("Cliente Teste");

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setSaldo(BigDecimal.valueOf(600));
        contaMock.setTitular(clienteMock); // Vincula o cliente à conta

        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(101L);
        transacaoMock.setValor(BigDecimal.valueOf(100.0));
        transacaoMock.setTipo(TipoTransacao.DEPOSITO);
        transacaoMock.setContaDestino(contaMock); // Vincula a conta à transação

        when(contaRepository.findById(contaId)).thenReturn(contaMock);
        when(transacaoService.depositar(anyLong(), any(BigDecimal.class))).thenReturn(transacaoMock);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/deposito")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirEndpointComSucesso() {
        Long contaOrigemId = 1L;
        Long contaDestinoId = 2L;

        Map<String, Object> contaDestinoPayload = Map.of("id", contaDestinoId);
        Map<String, Object> payload = Map.of(
                "contaDestino", contaDestinoPayload,
                "valor", 150.0
        );

        // Mock do Titular (necessário para o DTO converter)
        br.com.adacourse.models.Cliente titularMock = new br.com.adacourse.models.Cliente();
        titularMock.setNome("Fulano");
        titularMock.setCpf("123.456.789-00");
        titularMock.setEmail("user@teste.com");

        // Mock da Conta de Origem
        Conta origenMock = new Conta();
        origenMock.setId(contaOrigemId);
        origenMock.setNumero("12345-6");
        origenMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        origenMock.setSaldo(BigDecimal.valueOf(500.0));
        origenMock.setTitular(titularMock);

        // Mock da Conta de Destino
        Conta destinoMock = new Conta();
        destinoMock.setId(contaDestinoId);
        destinoMock.setNumero("78910-1");
        destinoMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        destinoMock.setSaldo(BigDecimal.valueOf(150.0));
        destinoMock.setTitular(titularMock);

        // Mock da Transação
        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(102L);
        transacaoMock.setValor(BigDecimal.valueOf(150.0));
        transacaoMock.setTipo(TipoTransacao.TRANSFERENCIA);
        transacaoMock.setDataHora(LocalDateTime.now());
        transacaoMock.setContaOrigem(origenMock);
        transacaoMock.setContaDestino(destinoMock);

        // 1. Moca o serviço de transferência
        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn(transacaoMock);

        // 2. Moca o REPOSITORY com o método exato que o Resource usa (findById)
        Mockito.when(contaRepository.findById(contaOrigemId)).thenReturn(origenMock);
        Mockito.when(contaRepository.findById(contaDestinoId)).thenReturn(destinoMock);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testBuscarSaldoComSucesso() {
        // Garantindo que o ID usado no mock e no path seja rigorosamente o mesmo tipo (Long)
        Long contaId = 1L;

        br.com.adacourse.models.Cliente titularMock = new br.com.adacourse.models.Cliente();
        titularMock.setNome("Fulano");
        titularMock.setCpf("123.456.789-00");
        titularMock.setEmail("user@teste.com"); // Deve coincidir perfeitamente com o user do @TestSecurity

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setNumero("12345-6");
        // Use a atribuição exata do seu Enum de TipoConta (CORRENTE, POUPANCA, etc.)
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(1000.0));
        contaMock.setTitular(titularMock);

        // ABORDAGEM SEGURA: Usamos Mockito.any() ou Mockito.anyLong() para blindar o mock
        // contra discrepâncias de passagem de parâmetro (como int vs Long nas entrelinhas do REST)
        Mockito.when(contaService.buscarContaPorId(Mockito.anyLong())).thenReturn(contaMock);

        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testCriarContaComSucesso() {
        // Payload estruturado baseado no ContaReqDTO
        Map<String, Object> clientePayload = Map.of("id", 1L);
        Map<String, Object> payload = Map.of(
                "tipo", "CORRENTE",
                "cliente", clientePayload
        );

        // Mocks das entidades internas
        Cliente titularMock = new Cliente();
        titularMock.setId(1L);
        titularMock.setNome("Cliente Teste");

        Conta contaCriadaMock = new Conta();
        contaCriadaMock.setId(50L);
        contaCriadaMock.setNumero("99999-9");
        contaCriadaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaCriadaMock.setTitular(titularMock);

        // Moca o comportamento do service
        Mockito.when(contaService.criarConta(Mockito.any(Conta.class)))
                .thenReturn(contaCriadaMock);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/contas")
                .then()
                .statusCode(201) // Created
                .header("Location", org.hamcrest.Matchers.containsString("/contas/50"))
                .body("id", org.hamcrest.Matchers.equalTo(50))
                .body("numero", org.hamcrest.Matchers.equalTo("99999-9"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testListarContasComSucesso() {
        Cliente titularMock = new Cliente();
        titularMock.setNome("Fulano");
        titularMock.setCpf("123.456.789-00");

        Conta contaMock = new Conta();
        contaMock.setId(1L);
        contaMock.setNumero("12345-6");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(1000.0));
        contaMock.setTitular(titularMock);

        // Moca o retorno de uma lista com uma conta
        Mockito.when(contaService.listarContas()).thenReturn(List.of(contaMock));

        given()
                .when()
                .get("/contas")
                .then()
                .statusCode(200)
                .body("$.size()", org.hamcrest.Matchers.equalTo(1))
                .body("[0].numero", org.hamcrest.Matchers.equalTo("12345-6"));
    }

    @Test
    public void testDepositarComSucesso() {
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 200.0);

        // Criando o titular para o DTO mapear sem estourar NullPointerException
        br.com.adacourse.models.Cliente titularMock = new br.com.adacourse.models.Cliente();
        titularMock.setId(1L);
        titularMock.setNome("Fulano");

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setNumero("12345-6");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(1200.0));
        contaMock.setTitular(titularMock); // Adicionado aqui para resolver o NPE!

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

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/deposito")
                .then()
                .statusCode(200)
                .body("saldoAtual", org.hamcrest.Matchers.equalTo(1200.0f)); // Ajustado para a chave correta do JSON
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testSacarComSucesso() {
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 50.0);

        br.com.adacourse.models.Cliente titularMock = new br.com.adacourse.models.Cliente();
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

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/saque")
                .then()
                .statusCode(200)
                .body("saldoAtual", org.hamcrest.Matchers.equalTo(450.0f)); // Corrigido de contaAtualizada.saldo para saldoAtual
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testCriarContaLançaIllegalArgumentException() {
        Map<String, Object> payload = Map.of("tipo", "CORRENTE", "cliente", Map.of("id", 1L));

        Mockito.when(contaService.criarConta(Mockito.any(Conta.class)))
                .thenThrow(new IllegalArgumentException("Dados inválidos para criação da conta"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/contas")
                .then()
                .statusCode(400)
                .body("erro", org.hamcrest.Matchers.equalTo("Dados inválidos para criação da conta"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testCriarContaLançaNotFoundException() {
        Map<String, Object> payload = Map.of("tipo", "CORRENTE", "cliente", Map.of("id", 99L));

        Mockito.when(contaService.criarConta(Mockito.any(Conta.class)))
                .thenThrow(new jakarta.ws.rs.NotFoundException("Cliente não encontrado"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/contas")
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta não encontrada"));
    }

    @Test
    public void testDepositarLançaUnsupportedOperationException() {
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 100.0);

        Mockito.when(transacaoService.depositar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new UnsupportedOperationException("Tipo de conta não permite depósito manual"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/deposito")
                .then()
                .statusCode(422)
                .body("erro", org.hamcrest.Matchers.equalTo("Tipo de conta não permite depósito manual"));
    }

    @Test
    public void testDepositarLançaIllegalArgumentException() {
        Long contaId = 99L;
        Map<String, Object> payload = Map.of("valor", 100.0);

        Mockito.when(transacaoService.depositar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Conta destino inexistente"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/deposito")
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta destino inexistente"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testSacarLançaIllegalArgumentException() {
        Long contaId = 99L;
        Map<String, Object> payload = Map.of("valor", 50.0);

        Mockito.when(transacaoService.sacar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Conta não encontrada"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/saque")
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta não encontrada"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testSacarLançaIllegalStateException() {
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 5000.0);

        Mockito.when(transacaoService.sacar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalStateException("Saldo insuficiente para o saque"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaId)
                .when()
                .post("/contas/{id}/saque")
                .then()
                .statusCode(422)
                .body("erro", org.hamcrest.Matchers.equalTo("Saldo insuficiente para o saque"));
    }

    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirLançaIllegalArgumentException() {
        Long contaOrigemId = 1L;
        Map<String, Object> payload = Map.of(
                "valor", 150.0,
                "contaDestino", Map.of("id", 99L)
        );

        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Conta de destino não foi localizada"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta de destino não foi localizada"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirLançaIllegalStateException() {
        Long contaOrigemId = 1L;
        Map<String, Object> payload = Map.of(
                "valor", 90000.0,
                "contaDestino", Map.of("id", 2L)
        );

        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalStateException("Limite diário de transferência excedido"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")
                .then()
                .statusCode(422)
                .body("erro", org.hamcrest.Matchers.equalTo("Limite diário de transferência excedido"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testBuscarContaPorIdComoGerente() {
        Long contaId = 1L;

        br.com.adacourse.models.Cliente titularMock = new br.com.adacourse.models.Cliente();
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

        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")
                .then()
                .statusCode(200)
                .body("numero", org.hamcrest.Matchers.equalTo("12345-6"));
    }

    @Test
    @TestSecurity(user = "dono@conta.com", roles = {"CLIENTE"})
    public void testBuscarPropriaContaComoCliente() {
        Long contaId = 2L;

        br.com.adacourse.models.Cliente titularMock = new br.com.adacourse.models.Cliente();
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

        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")
                .then()
                .statusCode(200)
                .body("numero", org.hamcrest.Matchers.equalTo("54321-0"));
    }

    @Test
    @TestSecurity(user = "gerente@mybank.com", roles = {"GERENTE"})
    public void testBuscarContaPorIdInexistente() {
        Long contaId = 999L;

        Mockito.when(contaService.buscarContaPorId(contaId)).thenReturn(null);

        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")
                .then()
                .statusCode(404)
                .body("erro", org.hamcrest.Matchers.equalTo("Conta Id não encontrada"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirComSucesso() {
        Long contaOrigemId = 1L;
        Long contaDestinoId = 2L;

        // Payload estruturado respeitando o DTO de entrada
        Map<String, Object> payload = Map.of(
                "valor", 100.0,
                "contaDestino", Map.of("id", contaDestinoId)
        );

        // Mock Titulares
        br.com.adacourse.models.Cliente origTitular = new br.com.adacourse.models.Cliente();
        origTitular.setId(10L); origTitular.setNome("Origem");

        br.com.adacourse.models.Cliente destTitular = new br.com.adacourse.models.Cliente();
        destTitular.setId(20L); destTitular.setNome("Destino");

        // Mock Contas pós-transferência
        Conta contaOrigemMock = new Conta();
        contaOrigemMock.setId(contaOrigemId);
        contaOrigemMock.setNumero("11111-1");
        contaOrigemMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaOrigemMock.setSaldo(BigDecimal.valueOf(400.0)); // Tinha 500, restam 400
        contaOrigemMock.setTitular(origTitular);

        Conta contaDestinoMock = new Conta();
        contaDestinoMock.setId(contaDestinoId);
        contaDestinoMock.setNumero("22222-2");
        contaDestinoMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaDestinoMock.setSaldo(BigDecimal.valueOf(200.0)); // Tinha 100, agora tem 200
        contaDestinoMock.setTitular(destTitular);

        // Mock Transação gerada
        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(303L);
        transacaoMock.setValor(BigDecimal.valueOf(100.0));
        transacaoMock.setTipo(br.com.adacourse.enums.TipoTransacao.TRANSFERENCIA);
        transacaoMock.setDataHora(java.time.LocalDateTime.now());
        transacaoMock.setContaOrigem(contaOrigemMock);
        transacaoMock.setContaDestino(contaDestinoMock);

        // Configuração dos comportamentos no Mockito
        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn(transacaoMock);
        Mockito.when(contaRepository.findById(contaOrigemId)).thenReturn(contaOrigemMock);
        Mockito.when(contaRepository.findById(contaDestinoId)).thenReturn(contaDestinoMock);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")
                .then()
                .statusCode(200);
        // Se o seu TransferenciaRespDTO contiver chaves específicas como 'saldoOrigem',
        // você pode adicionar asserções como: .body("saldoOrigem", org.hamcrest.Matchers.equalTo(400.0f))
    }

    @Test
    @TestSecurity(user = "cliente_invasor@teste.com", roles = {"CLIENTE"})
    public void testBuscarContaDeOutroClienteLançaForbidden() {
        Long contaId = 1L;

        // Criando uma conta que pertence ao "Fulano" (email diferente do usuário logado)
        br.com.adacourse.models.Cliente titularMock = new br.com.adacourse.models.Cliente();
        titularMock.setNome("Fulano");
        titularMock.setCpf("123.456.789-00");
        titularMock.setEmail("fulano_dono@teste.com"); // Email diferente do "cliente_invasor"

        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setNumero("12345-6");
        contaMock.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);
        contaMock.setSaldo(BigDecimal.valueOf(1000.0));
        contaMock.setTitular(titularMock);

        // Moca o service para encontrar a conta do Fulano
        Mockito.when(contaService.buscarContaPorId(contaId)).thenReturn(contaMock);

        given()
                .pathParam("id", contaId)
                .when()
                .get("/contas/{id}")
                .then()
                .statusCode(403) // Garante que barrou o acesso
                .body("erro", org.hamcrest.Matchers.equalTo("Acesso não autorizado"));
    }

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testTransferirLançaIllegalArgumentExceptionContaNaoEncontrada() {
        Long contaOrigemId = 1L;
        Long contaDestinoInexistenteId = 999L;

        // Payload enviando um ID de destino que vai falhar
        Map<String, Object> payload = Map.of(
                "valor", 100.0,
                "contaDestino", Map.of("id", contaDestinoInexistenteId)
        );

        // Configura o service para lançar a exceção capturada por esse catch específico
        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Conta de origem ou destino não encontrada no sistema"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", contaOrigemId)
                .when()
                .post("/contas/{id}/transferencia")
                .then()
                .statusCode(404) // Valida o Response.Status.NOT_FOUND do catch
                .body("erro", org.hamcrest.Matchers.equalTo("Conta de origem ou destino não encontrada no sistema"));
    }
}