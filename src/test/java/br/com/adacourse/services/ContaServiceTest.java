package br.com.adacourse.services;

import br.com.adacourse.enums.TipoConta;
import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Conta;
import br.com.adacourse.repositories.ContaRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ContaServiceTest {

    @Inject
    ContaService contaService;

    @InjectMock
    ClienteService clienteService;

    @InjectMock
    ContaRepository contaRepository;

    @InjectMock
    EntityManager em;

    // ==========================================
    // CENÁRIOS DO MÉTODO: criarConta
    // ==========================================

    @Test
    public void testCriarContaNulaDeveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.criarConta(null);
        });

        assertEquals("Requisição inválida: titular id é obrigatório", exception.getMessage());
    }

    @Test
    public void testCriarContaComTitularNuloDeveLancarExcecao() {
        Conta conta = new Conta();
        conta.setTitular(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.criarConta(conta);
        });

        assertEquals("Requisição inválida: titular id é obrigatório", exception.getMessage());
    }

    @Test
    public void testCriarContaComTitularIdNuloDeveLancarExcecao() {
        Conta conta = new Conta();
        Cliente titular = new Cliente();
        titular.setId(null);
        conta.setTitular(titular);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.criarConta(conta);
        });

        assertEquals("Requisição inválida: titular id é obrigatório", exception.getMessage());
    }

    @Test
    public void testCriarContaComClienteInexistenteDeveLancarExcecao() {
        Conta conta = new Conta();
        Cliente titular = new Cliente();
        titular.setId(999L);
        conta.setTitular(titular);

        // Simula que o clienteService retorna null ao buscar o ID 999
        when(clienteService.buscarClientePorId(999L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.criarConta(conta);
        });

        assertEquals("Cliente com id 999 não encontrado", exception.getMessage());
    }

    @Test
    public void testCriarContaComSucesso() {
        Cliente clienteDoBanco = new Cliente();
        clienteDoBanco.setId(1L);
        clienteDoBanco.setNome("Titular Valido");

        Conta contaReq = new Conta();
        contaReq.setTipo(TipoConta.CORRENTE);
        contaReq.setTitular(clienteDoBanco);

        // Configura as dependências e o ID simulando o banco gerando o ID 123L pós-persist
        when(clienteService.buscarClientePorId(1L)).thenReturn(clienteDoBanco);

        doAnswer(invocation -> {
            Conta c = invocation.getArgument(0);
            c.setId(123L); // Simula o banco injetando o ID
            return null;
        }).when(contaRepository).persist(any(Conta.class));

        // Execução
        Conta resultado = contaService.criarConta(contaReq);

        // Validações das regras de negócio do seu método
        assertNotNull(resultado);
        assertEquals(clienteDoBanco, resultado.getTitular());
        assertEquals(BigDecimal.ZERO, resultado.getSaldo());
        assertEquals("0123-3", resultado.getNumero()); // 123 formatado com 4 dígitos + o dígito verificador (123 % 10 = 3)

        verify(contaRepository, times(1)).persist(any(Conta.class));
        verify(em, times(1)).flush();
        verify(em, times(1)).merge(any(Conta.class));
    }

    // ==========================================
    // CENÁRIOS DOS MÉTODOS DE BUSCA E LISTAGEM
    // ==========================================

    @Test
    public void testListarContas() {
        Conta conta = new Conta();
        conta.setId(1L);
        conta.setNumero("0001-1");

        when(contaRepository.listAll()).thenReturn(Collections.singletonList(conta));

        List<Conta> resultado = contaService.listarContas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("0001-1", resultado.get(0).getNumero());
    }

    @Test
    public void testBuscarContaPorIdEncontrada() {
        Conta conta = new Conta();
        conta.setId(1L);

        when(contaRepository.findById(1L)).thenReturn(conta);

        Conta resultado = contaService.buscarContaPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    public void testBuscarContaPorIdNaoEncontrada() {
        when(contaRepository.findById(2L)).thenReturn(null);

        Conta resultado = contaService.buscarContaPorId(2L);

        assertNull(resultado);
    }
}