package br.com.adacourse.services;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Conta;
import br.com.adacourse.repositories.ContaRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
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
    public void testCriarContaSucesso() {
        Cliente titular = new Cliente();
        titular.setId(1L);

        Conta conta = new Conta();
        conta.setId(100L);
        conta.setTitular(titular);

        when(clienteService.buscarClientePorId(1L)).thenReturn(titular);

        Conta criada = contaService.criarConta(conta);
        assertNotNull(criada);
        assertEquals("0100-0", criada.getNumero());
        verify(contaRepository, times(1)).persist(conta);
    }

    @Test
    public void testCriarContaIdTitularNulo() {
        Conta conta = new Conta();
        assertThrows(IllegalArgumentException.class, () -> contaService.criarConta(conta));
    }
}