package integracao;

import br.com.adacourse.enums.TipoCliente;
import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Conta;
import br.com.adacourse.repositories.ClienteRepository;
import br.com.adacourse.repositories.ContaRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

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
        // ARRANGE
        contaRepository.deleteAll();
        clienteRepository.deleteAll();

        this.clienteSalvo = new Cliente();
        this.clienteSalvo.setNome("Fabio");
        this.clienteSalvo.setCpf("12345678901");
        this.clienteSalvo.setEmail("fabio@email.com");
        this.clienteSalvo.setSenha("senhaCriptografadaAqui");
        this.clienteSalvo.setRole(TipoCliente.CLIENTE);

        clienteRepository.persist(this.clienteSalvo);

        Conta conta = new Conta();
        conta.setTitular(this.clienteSalvo);
        conta.setSaldo(BigDecimal.valueOf(500.0));
        conta.setNumero("9999-9"); // número fictício
        conta.setTipo(br.com.adacourse.enums.TipoConta.CORRENTE);

        contaRepository.persist(conta);
    }

    @Test
    public void deveBuscarContaPorCliente() {
        // ACT
        Conta conta = contaRepository.buscarContaPorCliente(clienteSalvo.getId());

        // ASSERT
        assertNotNull(conta);
        assertEquals(0, BigDecimal.ZERO.compareTo(conta.getSaldo())); // saldo inicial
        assertEquals("Fabio", conta.getTitular().getNome());
    }

    @Test
    public void deveRetornarNullQuandoClienteNaoTemConta() {
        // ACT
        Conta conta = contaRepository.buscarContaPorCliente(999999L);

        // ASSERT
        assertNull(conta);
    }
}