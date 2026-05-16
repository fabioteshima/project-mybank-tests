package br.com.adacourse.services;

import br.com.adacourse.enums.TipoCliente;
import br.com.adacourse.models.Cliente;
import br.com.adacourse.repositories.ClienteRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@QuarkusTest
public class AuthServiceTest {

    @Inject
    AuthService authService;

    @InjectMock
    ClienteRepository clienteRepository;

    private Cliente clienteMock;

    @BeforeEach
    public void setup() {
        clienteMock = new Cliente();
        clienteMock.setEmail("teste@ada.com");
        clienteMock.setSenha(BCrypt.hashpw("senha123", BCrypt.gensalt()));
        clienteMock.setRole(TipoCliente.CLIENTE);
    }

    @Test
    public void testAutenticacaoSucesso() {
        // Criando um mock para o PanacheQuery retornado pelo repository
        var panacheQueryMock = Mockito.mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(panacheQueryMock.firstResult()).thenReturn(clienteMock);
        when(clienteRepository.find("email", "teste@ada.com")).thenReturn(panacheQueryMock);

        String token = authService.autenticacao("teste@ada.com", "senha123");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testAutenticacaoUsuarioNaoEncontrado() {
        var panacheQueryMock = Mockito.mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(panacheQueryMock.firstResult()).thenReturn(null);
        when(clienteRepository.find("email", "invalido@ada.com")).thenReturn(panacheQueryMock);

        assertThrows(WebApplicationException.class, () -> {
            authService.autenticacao("invalido@ada.com", "senha123");
        });
    }

    @Test
    public void testAutenticacaoSenhaIncorreta() {
        var panacheQueryMock = Mockito.mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(panacheQueryMock.firstResult()).thenReturn(clienteMock);
        when(clienteRepository.find("email", "teste@ada.com")).thenReturn(panacheQueryMock);

        assertThrows(WebApplicationException.class, () -> {
            authService.autenticacao("teste@ada.com", "senha_errada");
        });
    }
}
