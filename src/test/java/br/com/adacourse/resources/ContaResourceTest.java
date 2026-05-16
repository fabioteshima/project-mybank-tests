package br.com.adacourse.resources;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Transacao;
import br.com.adacourse.models.Conta;
import br.com.adacourse.repositories.ContaRepository;
import br.com.adacourse.services.TransacaoService;
import br.com.adacourse.enums.TipoTransacao;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static io.restassured.RestAssured.given;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;



@QuarkusTest
public class ContaResourceTest {

    @InjectMock
    TransacaoService transacaoService;

    @InjectMock
    ContaRepository contaRepository;

    @Test
    @TestSecurity(user = "user@teste.com", roles = {"CLIENTE"})
    public void testSacarEndpointComSucesso() {
        // ------------------------------------------------------------------------
        // ARRANGE (Preparação dos Mockitos e Dados de Entrada)
        // ------------------------------------------------------------------------
        Long contaId = 1L;
        Map<String, Object> payload = Map.of("valor", 50.0);

        // Criação do Titular (Cliente) para evitar NullPointerException no DTO de resposta
        Cliente clienteMock = new Cliente();
        clienteMock.setId(1L);
        clienteMock.setNome("Cliente Teste");

        // Criação da Conta e vinculação com o Titular
        Conta contaMock = new Conta();
        contaMock.setId(contaId);
        contaMock.setSaldo(BigDecimal.valueOf(500));
        contaMock.setTitular(clienteMock);

        // Criação da Transação interligada com a conta de origem
        Transacao transacaoMock = new Transacao();
        transacaoMock.setId(100L);
        transacaoMock.setValor(BigDecimal.valueOf(50.0));
        transacaoMock.setTipo(TipoTransacao.SAQUE);
        transacaoMock.setContaOrigem(contaMock);

        // Configuração dos comportamentos dos Mocks (Comportamento esperado dos Services/Repositories)
        when(contaRepository.findById(contaId)).thenReturn(contaMock);
        when(transacaoService.sacar(anyLong(), any(BigDecimal.class))).thenReturn(transacaoMock);

        // ------------------------------------------------------------------------
        // ACT & ASSERT (Execução da requisição HTTP e Validação do Status 200)
        // ------------------------------------------------------------------------
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
        // Sem a anotação @TestSecurity, o endpoint protegido com @RolesAllowed deve barrar
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("valor", 50.0))
                .pathParam("id", 1L)
                .when().post("/contas/{id}/saque")
                .then()
                .statusCode(401); // Unauthorized
    }
}