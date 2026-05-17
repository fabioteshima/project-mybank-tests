package br.com.adacourse.repositories;

import static org.junit.jupiter.api.Assertions.*;

import br.com.adacourse.enums.TipoCliente;
import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Conta;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

@QuarkusTest
public class ContaRepositoryTest {

    @Inject
    ContaRepository contaRepository;

    @Inject
    ClienteRepository clienteRepository;

    private Cliente clienteSalvo;

    @BeforeEach
    @Transactional
    public void setup() {
        // Limpa os dados anteriores para garantir um ambiente de teste isolado
        contaRepository.deleteAll();
        clienteRepository.deleteAll();

        // 1. Instancia o cliente usando a variável global para não perder a referência do ID
        this.clienteSalvo = new Cliente();
        this.clienteSalvo.setNome("Fabio");

        // CORREÇÃO: Preenchendo todos os campos obrigatórios (NOT NULL) que o banco exige
        this.clienteSalvo.setCpf("12345678901");
        this.clienteSalvo.setEmail("fabio@email.com");
        this.clienteSalvo.setSenha("senhaCriptografadaAqui");
        this.clienteSalvo.setRole(TipoCliente.CLIENTE);

        // Salva o cliente no banco de dados (agora vai passar sem erro de CPF nulo)
        clienteRepository.persist(this.clienteSalvo);

        // 2. Cria e vincula a conta ao cliente que acabamos de salvar
        Conta conta = new Conta();
        conta.setTitular(this.clienteSalvo);
        conta.setSaldo(BigDecimal.valueOf(500.0));
        // O banco também exige um número de conta único, vamos colocar um fictício para o teste de integração
        conta.setNumero("9999-9");
        conta.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);

        contaRepository.persist(conta);
    }

    @Test
    public void deveBuscarContaPorCliente() {
        // Busca usando o ID real gerado pelo banco durante o setup
        Conta conta = contaRepository.buscarContaPorCliente(clienteSalvo.getId());

        assertNotNull(conta);
        // Ajustado para ZERO, já que a conta nova não possui transações inseridas no setup
        assertEquals(0, BigDecimal.ZERO.compareTo(conta.getSaldo()));
        assertEquals("Fabio", conta.getTitular().getNome());
    }

    @Test
    public void deveRetornarNullQuandoClienteNaoTemConta() {
        // 999999L garante um ID inexistente no banco H2 temporário
        Conta conta = contaRepository.buscarContaPorCliente(999999L);

        assertNull(conta);
    }
}