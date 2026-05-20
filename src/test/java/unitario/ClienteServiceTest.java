package unitario;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.repositories.ClienteRepository;
import br.com.adacourse.services.ClienteService;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ClienteServiceTest {

    @Inject
    ClienteService clienteService;

    @InjectMock
    ClienteRepository clienteRepository;

    @Test
    public void testCadastrarClienteComSucesso() {
        // ARRANGE
        Cliente clienteInput = new Cliente();
        clienteInput.setNome("João Silva");
        clienteInput.setCpf("12345678901");
        clienteInput.setEmail("joao@email.com");
        clienteInput.setSenha("senha123");

        PanacheQuery<Cliente> queryMock = mock(PanacheQuery.class);
        when(queryMock.firstResult()).thenReturn(null);
        when(clienteRepository.find(anyString(), (Object[]) any())).thenReturn(queryMock);

        // ACT
        Cliente resultado = clienteService.cadastrarCliente(clienteInput);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertNotEquals("senha123", resultado.getSenha()); // senha deve ser criptografada
        verify(clienteRepository, times(1)).persist(any(Cliente.class));
    }

    @Test
    public void testCadastrarClienteCpfJaExistente() {
        // ARRANGE
        Cliente clienteInput = new Cliente();
        clienteInput.setCpf("12345678901");
        clienteInput.setSenha("senha123");

        Cliente clienteExistente = new Cliente();

        PanacheQuery<Cliente> queryMock = mock(PanacheQuery.class);
        when(queryMock.firstResult()).thenReturn(clienteExistente);
        when(clienteRepository.find(anyString(), (Object[]) any())).thenReturn(queryMock);

        // ACT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarCliente(clienteInput);
        });

        // ASSERT
        assertEquals("CPF já cadastrado", exception.getMessage());
        verify(clienteRepository, never()).persist(any(Cliente.class));
    }

    @Test
    public void testBuscarClientePorIdComSucesso() {
        // ARRANGE
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria Souza");
        when(clienteRepository.findById(1L)).thenReturn(cliente);

        // ACT
        Cliente resultado = clienteService.buscarClientePorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Maria Souza", resultado.getNome());
    }

    @Test
    public void testBuscarClientePorIdNaoEncontrado() {
        // ARRANGE
        when(clienteRepository.findById(99L)).thenReturn(null);

        // ACT
        Cliente resultado = clienteService.buscarClientePorId(99L);

        // ASSERT
        assertNull(resultado);
    }

    @Test
    public void testListarClientes() {
        // ARRANGE
        Cliente cliente = new Cliente();
        cliente.setNome("Lucas");
        when(clienteRepository.listAll()).thenReturn(List.of(cliente));

        // ACT
        List<Cliente> resultado = clienteService.listarClientes();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Lucas", resultado.get(0).getNome());
        verify(clienteRepository, times(1)).listAll();
    }

    @Test
    public void testAtualizarClienteNaoEncontrado() {
        // ARRANGE
        when(clienteRepository.findById(1L)).thenReturn(null);
        Cliente dadosNovos = new Cliente();
        dadosNovos.setNome("Novo Nome");

        // ACT
        jakarta.persistence.EntityNotFoundException exception = assertThrows(
                jakarta.persistence.EntityNotFoundException.class, () -> {
                    clienteService.atualizarCliente(1L, dadosNovos);
                }
        );

        // ASSERT
        assertEquals("Cliente não encontrado com id", exception.getMessage());
    }

    @Test
    public void testAtualizarClienteTentandoAlterarCpf() {
        // ARRANGE
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setCpf("12345678901");
        clienteExistente.setNome("Nome Antigo");
        when(clienteRepository.findById(1L)).thenReturn(clienteExistente);

        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setCpf("99999999999"); // CPF diferente
        clienteAtualizado.setNome("Nome Novo");

        // ACT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.atualizarCliente(1L, clienteAtualizado);
        });

        // ASSERT
        assertEquals("CPF não pode ser atualizado", exception.getMessage());
    }

    @Test
    public void testAtualizarClienteComSucesso() {
        // ARRANGE
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setCpf("12345678901");
        clienteExistente.setNome("Nome Antigo");
        clienteExistente.setEmail("antigo@email.com");
        clienteExistente.setSenha("hashAntiga");
        when(clienteRepository.findById(1L)).thenReturn(clienteExistente);

        Cliente dadosNovos = new Cliente();
        dadosNovos.setCpf("12345678901"); // mesmo CPF
        dadosNovos.setNome("Nome Atualizado");
        dadosNovos.setEmail("atualizado@email.com");
        dadosNovos.setSenha("novaSenha123");

        // ACT
        Cliente resultado = clienteService.atualizarCliente(1L, dadosNovos);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("Nome Atualizado", resultado.getNome());
        assertEquals("atualizado@email.com", resultado.getEmail());
        assertNotEquals("novaSenha123", resultado.getSenha());
        assertNotEquals("hashAntiga", resultado.getSenha());
    }

    @Test
    public void testAtualizarClienteComCpfNuloNaoDeveLancarExcecao() {
        // ARRANGE
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setCpf("12345678901");
        clienteExistente.setNome("Nome Antigo");
        clienteExistente.setEmail("antigo@email.com");
        clienteExistente.setSenha("hashAntiga");
        when(clienteRepository.findById(1L)).thenReturn(clienteExistente);

        Cliente dadosNovos = new Cliente();
        dadosNovos.setCpf(null);
        dadosNovos.setNome("Nome Atualizado");
        dadosNovos.setEmail("atualizado@email.com");
        dadosNovos.setSenha("novaSenha123");

        // ACT
        Cliente resultado = clienteService.atualizarCliente(1L, dadosNovos);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("Nome Atualizado", resultado.getNome());
        assertEquals("12345678901", resultado.getCpf()); // CPF antigo deve ser mantido
    }
}