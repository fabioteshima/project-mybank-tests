package br.com.adacourse.services;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.repositories.ClienteRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ClienteServiceTest {

    @Inject
    ClienteService clienteService;

    @InjectMock
    ClienteRepository clienteRepository;

    @Test
    public void testCadastrarClienteSucesso() {
        Cliente cliente = new Cliente();
        cliente.setCpf("12345678900");
        cliente.setSenha("123");

        var queryMock = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(queryMock.firstResult()).thenReturn(null); // CPF livre
        when(clienteRepository.find("cpf", "12345678900")).thenReturn(queryMock);

        Cliente resultado = clienteService.cadastrarCliente(cliente);
        assertNotNull(resultado);
        assertNotEquals("123", resultado.getSenha()); // Senha deve estar criptografada
        verify(clienteRepository, times(1)).persist(cliente);
    }

    @Test
    public void testCadastrarClienteCpfDuplicado() {
        Cliente cliente = new Cliente();
        cliente.setCpf("12345678900");

        var queryMock = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(queryMock.firstResult()).thenReturn(new Cliente()); // CPF já existe
        when(clienteRepository.find("cpf", "12345678900")).thenReturn(queryMock);

        assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarCliente(cliente);
        });
    }

    @Test
    public void testListarClientes() {
        when(clienteRepository.listAll()).thenReturn(List.of(new Cliente(), new Cliente()));
        List<Cliente> lista = clienteService.listarClientes();
        assertEquals(2, lista.size());
    }

    @Test
    public void testBuscarClientePorId() {
        Cliente c = new Cliente();
        when(clienteRepository.findById(1L)).thenReturn(c);
        assertNotNull(clienteService.buscarClientePorId(1L));
    }

    @Test
    public void testAtualizarClienteNaoEncontrado() {
        when(clienteRepository.findById(1L)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> {
            clienteService.atualizarCliente(1L, new Cliente());
        });
    }
}