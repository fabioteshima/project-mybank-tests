package unitario;

import br.com.adacourse.enums.TipoCliente;
import br.com.adacourse.models.Cliente;
import br.com.adacourse.repositories.ClienteRepository;
import br.com.adacourse.services.AuthService;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
public class AuthServiceTest {

    @Inject
    AuthService authService;

    @InjectMock
    ClienteRepository clienteRepository;

    @Test
    public void testAutenticacaoUsuarioNaoEncontrado() {
        // ARRANGE
        PanacheQuery<Cliente> queryMock = mock(PanacheQuery.class);
        when(queryMock.firstResult()).thenReturn(null);
        when(clienteRepository.find(anyString(), (Object[]) any())).thenReturn(queryMock);

        // ACT
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            authService.autenticacao("usuario.invalido@email.com", "senha123");
        });

        // ASSERT
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Credenciais inválidas", exception.getMessage());
    }

    @Test
    public void testAutenticacaoSenhaIncorreta() {
        // ARRANGE
        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@email.com");
        cliente.setSenha(BCrypt.hashpw("senhaCorreta", BCrypt.gensalt()));
        cliente.setRole(TipoCliente.CLIENTE);

        PanacheQuery<Cliente> queryMock = mock(PanacheQuery.class);
        when(queryMock.firstResult()).thenReturn(cliente);
        when(clienteRepository.find(anyString(), (Object[]) any())).thenReturn(queryMock);

        // ACT
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            authService.autenticacao("cliente@email.com", "senhaIncorreta");
        });

        // ASSERT
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Credenciais inválidas", exception.getMessage());
    }

    @Test
    public void testAutenticacaoSucesso() {
        // ARRANGE
        Cliente cliente = new Cliente();
        cliente.setEmail("gerente@email.com");
        cliente.setSenha(BCrypt.hashpw("senha123", BCrypt.gensalt()));
        cliente.setRole(TipoCliente.GERENTE);

        PanacheQuery<Cliente> queryMock = mock(PanacheQuery.class);
        when(queryMock.firstResult()).thenReturn(cliente);
        when(clienteRepository.find(anyString(), (Object[]) any())).thenReturn(queryMock);

        // ACT
        String tokenJwt = authService.autenticacao("gerente@email.com", "senha123");

        // ASSERT
        assertNotNull(tokenJwt);
        assertFalse(tokenJwt.trim().isEmpty());
    }
}