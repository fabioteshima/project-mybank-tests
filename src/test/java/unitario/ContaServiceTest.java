package unitario;

import br.com.adacourse.enums.TipoConta;
import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Conta;
import br.com.adacourse.repositories.ContaRepository;
import br.com.adacourse.services.ClienteService;
import br.com.adacourse.services.ContaService;
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

    @Test
    public void testCriarContaNulaDeveLancarExcecao() {
        // ACT + ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.criarConta(null);
        });

        assertEquals("Requisição inválida: titular id é obrigatório", exception.getMessage());
    }

    @Test
    public void testCriarContaComTitularNuloDeveLancarExcecao() {
        // ARRANGE
        Conta conta = new Conta();
        conta.setTitular(null);

        // ACT + ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.criarConta(conta);
        });

        assertEquals("Requisição inválida: titular id é obrigatório", exception.getMessage());
    }

    @Test
    public void testCriarContaComTitularIdNuloDeveLancarExcecao() {
        // ARRANGE
        Conta conta = new Conta();
        Cliente titular = new Cliente();
        titular.setId(null);
        conta.setTitular(titular);

        // ACT + ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.criarConta(conta);
        });

        assertEquals("Requisição inválida: titular id é obrigatório", exception.getMessage());
    }

    @Test
    public void testCriarContaComClienteInexistenteDeveLancarExcecao() {
        // ARRANGE
        Conta conta = new Conta();
        Cliente titular = new Cliente();
        titular.setId(999L);
        conta.setTitular(titular);

        when(clienteService.buscarClientePorId(999L)).thenReturn(null);

        // ACT + ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.criarConta(conta);
        });

        assertEquals("Cliente com id 999 não encontrado", exception.getMessage());
    }

    @Test
    public void testCriarContaComSucesso() {
        // ARRANGE
        Cliente clienteDoBanco = new Cliente();
        clienteDoBanco.setId(1L);
        clienteDoBanco.setNome("Titular Valido");

        Conta contaReq = new Conta();
        contaReq.setTipo(TipoConta.CORRENTE);
        contaReq.setTitular(clienteDoBanco);

        when(clienteService.buscarClientePorId(1L)).thenReturn(clienteDoBanco);

        doAnswer(invocation -> {
            Conta c = invocation.getArgument(0);
            c.setId(123L); // Simula o banco injetando o ID
            return null;
        }).when(contaRepository).persist(any(Conta.class));

        // ACT
        Conta resultado = contaService.criarConta(contaReq);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(clienteDoBanco, resultado.getTitular());
        assertEquals(BigDecimal.ZERO, resultado.getSaldo());
        assertEquals("0123-3", resultado.getNumero()); // número formatado

        verify(contaRepository, times(1)).persist(any(Conta.class));
        verify(em, times(1)).flush();
        verify(em, times(1)).merge(any(Conta.class));
    }

    @Test
    public void testListarContas() {
        // ARRANGE
        Conta conta = new Conta();
        conta.setId(1L);
        conta.setNumero("0001-1");
        when(contaRepository.listAll()).thenReturn(Collections.singletonList(conta));

        // ACT
        List<Conta> resultado = contaService.listarContas();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("0001-1", resultado.get(0).getNumero());
    }

    @Test
    public void testBuscarContaPorIdEncontrada() {
        // ARRANGE
        Conta conta = new Conta();
        conta.setId(1L);
        when(contaRepository.findById(1L)).thenReturn(conta);

        // ACT
        Conta resultado = contaService.buscarContaPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    public void testBuscarContaPorIdNaoEncontrada() {
        // ARRANGE
        when(contaRepository.findById(2L)).thenReturn(null);

        // ACT
        Conta resultado = contaService.buscarContaPorId(2L);

        // ASSERT
        assertNull(resultado);
    }
}