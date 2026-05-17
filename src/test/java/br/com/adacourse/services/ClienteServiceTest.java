package br.com.adacourse.services;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.repositories.ClienteRepository;
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

    // ==========================================
    // CENÁRIOS DO MÉTODO: cadastrarCliente
    // ==========================================

    @Test
    public void testCadastrarClienteComSucesso() {
        Cliente clienteInput = new Cliente();
        clienteInput.setNome("João Silva");
        clienteInput.setCpf("12345678901");
        clienteInput.setEmail("joao@email.com");
        clienteInput.setSenha("senha123");

        // Correção do mock para bater perfeitamente com find(String, Object...)
        PanacheQuery<Cliente> queryMock = mock(PanacheQuery.class);
        when(queryMock.firstResult()).thenReturn(null);
        when(clienteRepository.find(anyString(), (Object[]) any())).thenReturn(queryMock);

        Cliente resultado = clienteService.cadastrarCliente(clienteInput);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertNotEquals("senha123", resultado.getSenha()); // Garante que foi criptografada

        verify(clienteRepository, times(1)).persist(any(Cliente.class));
    }

    @Test
    public void testCadastrarClienteCpfJaExistente() {
        Cliente clienteInput = new Cliente();
        clienteInput.setCpf("12345678901");
        clienteInput.setSenha("senha123");

        Cliente clienteExistente = new Cliente();

        // Correção do mock para bater perfeitamente com find(String, Object...)
        PanacheQuery<Cliente> queryMock = mock(PanacheQuery.class);
        when(queryMock.firstResult()).thenReturn(clienteExistente);
        when(clienteRepository.find(anyString(), (Object[]) any())).thenReturn(queryMock);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarCliente(clienteInput);
        });

        assertEquals("CPF já cadastrado", exception.getMessage());
        verify(clienteRepository, never()).persist(any(Cliente.class));
    }

    // ==========================================
    // CENÁRIOS DO MÉTODO: buscarClientePorId
    // ==========================================

    @Test
    public void testBuscarClientePorIdComSucesso() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria Souza");

        when(clienteRepository.findById(1L)).thenReturn(cliente);

        Cliente resultado = clienteService.buscarClientePorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Maria Souza", resultado.getNome());
    }

    @Test
    public void testBuscarClientePorIdNaoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(null);

        Cliente resultado = clienteService.buscarClientePorId(99L);

        assertNull(resultado);
    }

    // ==========================================
    // CENÁRIOS DO MÉTODO: listarClientes
    // ==========================================

    @Test
    public void testListarClientes() {
        Cliente cliente = new Cliente();
        cliente.setNome("Lucas");

        when(clienteRepository.listAll()).thenReturn(List.of(cliente));

        List<Cliente> resultado = clienteService.listarClientes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Lucas", resultado.get(0).getNome());
        verify(clienteRepository, times(1)).listAll();
    }

    @Test
    public void testAtualizarClienteNaoEncontrado() {
        // Simula que o cliente com id 1L não existe no banco
        when(clienteRepository.findById(1L)).thenReturn(null);

        Cliente dadosNovos = new Cliente();
        dadosNovos.setNome("Novo Nome");

        // Valida se lança EntityNotFoundException (certifique-se de importar jakarta.persistence.EntityNotFoundException)
        jakarta.persistence.EntityNotFoundException exception = assertThrows(
                jakarta.persistence.EntityNotFoundException.class, () -> {
                    clienteService.atualizarCliente(1L, dadosNovos);
                }
        );

        assertEquals("Cliente não encontrado com id", exception.getMessage());
    }

    @Test
    public void testAtualizarClienteTentandoAlterarCpf() {
        // Cliente que já está guardado no banco
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setCpf("12345678901");
        clienteExistente.setNome("Nome Antigo");

        when(clienteRepository.findById(1L)).thenReturn(clienteExistente);

        // Cliente com dados novos tentando mudar o CPF
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setCpf("99999999999"); // CPF diferente!
        clienteAtualizado.setNome("Nome Novo");

        // Deve lançar IllegalArgumentException devido à regra do método privado
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.atualizarCliente(1L, clienteAtualizado);
        });

        assertEquals("CPF não pode ser atualizado", exception.getMessage());
    }

    @Test
    public void testAtualizarClienteComSucesso() {
        // Cliente original simulado do banco
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setCpf("12345678901");
        clienteExistente.setNome("Nome Antigo");
        clienteExistente.setEmail("antigo@email.com");
        clienteExistente.setSenha("hashAntiga");

        when(clienteRepository.findById(1L)).thenReturn(clienteExistente);

        // Payload enviado para a atualização (mesmo CPF ou nulo, nova senha em texto limpo)
        Cliente dadosNovos = new Cliente();
        dadosNovos.setCpf("12345678901"); // Mantendo o mesmo CPF
        dadosNovos.setNome("Nome Atualizado");
        dadosNovos.setEmail("atualizado@email.com");
        dadosNovos.setSenha("novaSenha123");

        // Executa a atualização
        Cliente resultado = clienteService.atualizarCliente(1L, dadosNovos);

        // Asserções para validar se o método privado copiou e modificou os dados corretamente
        assertNotNull(resultado);
        assertEquals("Nome Atualizado", resultado.getNome());
        assertEquals("atualizado@email.com", resultado.getEmail());

        // Garante que a senha foi modificada e criptografada (não pode ser o texto limpo e nem a hash antiga)
        assertNotEquals("novaSenha123", resultado.getSenha());
        assertNotEquals("hashAntiga", resultado.getSenha());
    }

    @Test
    public void testAtualizarClienteComCpfNuloNaoDeveLancarExcecao() {
        // Cliente original simulado do banco
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setCpf("12345678901");
        clienteExistente.setNome("Nome Antigo");
        clienteExistente.setEmail("antigo@email.com");
        clienteExistente.setSenha("hashAntiga");

        when(clienteRepository.findById(1L)).thenReturn(clienteExistente);

        // Payload enviado para a atualização com o CPF nulo
        Cliente dadosNovos = new Cliente();
        dadosNovos.setCpf(null); // <-- Força a primeira parte do IF a ser falsa!
        dadosNovos.setNome("Nome Atualizado");
        dadosNovos.setEmail("atualizado@email.com");
        dadosNovos.setSenha("novaSenha123");

        // Executa a atualização - Não deve lançar IllegalArgumentException
        Cliente resultado = clienteService.atualizarCliente(1L, dadosNovos);

        // Asserções para garantir que a atualização passou direto pelo IF amarelo
        assertNotNull(resultado);
        assertEquals("Nome Atualizado", resultado.getNome());
        assertEquals("12345678901", resultado.getCpf()); // O CPF antigo deve ser mantido intacto
    }
}