package unitario;

import br.com.adacourse.enums.TipoConta;
import br.com.adacourse.enums.TipoTransacao;
import br.com.adacourse.models.Conta;
import br.com.adacourse.models.Transacao;
import br.com.adacourse.repositories.ContaRepository;
import br.com.adacourse.repositories.TransacaoRepository;
import br.com.adacourse.services.TransacaoService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class TransacaoServiceTest {

    @Inject
    TransacaoService transacaoService;

    @InjectMock
    ContaRepository contaRepository;

    @InjectMock
    TransacaoRepository transacaoRepository;

    @InjectMock
    EntityManager em;

    @Test
    public void testDepositoContaNaoEncontrada() {
        // ARRANGE
        when(contaRepository.findById(any())).thenReturn(null);

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class, () -> {
            transacaoService.depositar(1L, BigDecimal.valueOf(100));
        });
    }

    @Test
    public void testDepositoSucesso() {
        // ARRANGE
        Conta conta = new Conta();
        conta.setId(1L);
        conta.setTipo(TipoConta.CORRENTE);
        conta.setSaldo(BigDecimal.ZERO);
        when(contaRepository.findById(any())).thenReturn(conta);

        // ACT
        Transacao t = transacaoService.depositar(1L, BigDecimal.valueOf(100));

        // ASSERT
        assertNotNull(t);
        assertEquals(TipoTransacao.DEPOSITO, t.getTipo());
        assertEquals(BigDecimal.valueOf(100), t.getValor());
        verify(transacaoRepository, times(1)).persist(any(Transacao.class));
    }

    @Test
    public void testDepositoContaEletronicaFalha() {
        // ARRANGE
        Conta conta = new Conta();
        conta.setId(1L);
        conta.setTipo(TipoConta.ELETRONICA);
        conta.setSaldo(BigDecimal.ZERO);
        when(contaRepository.findById(any())).thenReturn(conta);

        // ACT + ASSERT
        assertThrows(UnsupportedOperationException.class, () -> {
            transacaoService.depositar(1L, BigDecimal.valueOf(100));
        });
    }

    @Test
    public void testSacarContaNaoEncontrada() {
        // ARRANGE
        when(contaRepository.findById(any())).thenReturn(null);

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class, () -> {
            transacaoService.sacar(1L, BigDecimal.valueOf(50));
        });
    }

    @Test
    public void testSacarContaEletronicaFalha() {
        // ARRANGE
        Conta conta = new Conta();
        conta.setId(1L);
        conta.setTipo(TipoConta.ELETRONICA);
        conta.setSaldo(BigDecimal.valueOf(100));
        when(contaRepository.findById(any())).thenReturn(conta);

        // ACT + ASSERT
        assertThrows(UnsupportedOperationException.class, () -> {
            transacaoService.sacar(1L, BigDecimal.valueOf(50));
        });
    }

    @Test
    public void testSacarSaldoInsuficiente() {
        // ARRANGE
        Conta conta = new Conta();
        conta.setId(1L);
        conta.setTipo(TipoConta.CORRENTE);
        conta.setSaldo(BigDecimal.valueOf(10));
        when(contaRepository.findById(any())).thenReturn(conta);

        // ACT + ASSERT
        assertThrows(IllegalStateException.class, () -> {
            transacaoService.sacar(1L, BigDecimal.valueOf(50));
        });
    }

    @Test
    public void testSacarSucesso() {
        // ARRANGE
        Conta conta = new Conta();
        conta.setId(1L);
        conta.setTipo(TipoConta.CORRENTE);
        conta.setSaldo(BigDecimal.valueOf(100));
        when(contaRepository.findById(any())).thenReturn(conta);

        // ACT
        Transacao t = transacaoService.sacar(1L, BigDecimal.valueOf(50));

        // ASSERT
        assertNotNull(t);
        assertEquals(TipoTransacao.SAQUE, t.getTipo());
        assertEquals(BigDecimal.valueOf(50), t.getValor());
        verify(transacaoRepository, times(1)).persist(any(Transacao.class));
    }

    @Test
    public void testTransferirOrigemNaoEncontrada() {
        // ARRANGE
        when(contaRepository.findById(1L)).thenReturn(null);

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class, () -> {
            transacaoService.transferir(1L, 2L, BigDecimal.valueOf(50));
        });
    }

    @Test
    public void testTransferirDestinoNaoEncontrado() {
        // ARRANGE
        Conta origem = new Conta();
        origem.setId(1L);
        when(contaRepository.findById(1L)).thenReturn(origem);
        when(contaRepository.findById(2L)).thenReturn(null);

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class, () -> {
            transacaoService.transferir(1L, 2L, BigDecimal.valueOf(50));
        });
    }

    @Test
    public void testTransferirSaldoInsuficiente() {
        // ARRANGE
        Conta origem = new Conta();
        origem.setId(1L);
        origem.setSaldo(BigDecimal.valueOf(10));
        Conta destino = new Conta();
        destino.setId(2L);
        when(contaRepository.findById(1L)).thenReturn(origem);
        when(contaRepository.findById(2L)).thenReturn(destino);

        // ACT + ASSERT
        assertThrows(IllegalStateException.class, () -> {
            transacaoService.transferir(1L, 2L, BigDecimal.valueOf(50));
        });
    }

    @Test
    public void testTransferirSucesso() {
        // ARRANGE
        Conta origem = new Conta();
        origem.setId(1L);
        origem.setSaldo(BigDecimal.valueOf(100));
        Conta destino = new Conta();
        destino.setId(2L);
        when(contaRepository.findById(1L)).thenReturn(origem);
        when(contaRepository.findById(2L)).thenReturn(destino);

        // ACT
        Transacao t = transacaoService.transferir(1L, 2L, BigDecimal.valueOf(50));

        // ASSERT
        assertNotNull(t);
        assertEquals(TipoTransacao.TRANSFERENCIA, t.getTipo());
        assertEquals(BigDecimal.valueOf(50), t.getValor());
        assertEquals(origem, t.getContaOrigem());
        assertEquals(destino, t.getContaDestino());
        verify(transacaoRepository, times(1)).persist(any(Transacao.class));
    }

    @Test
    public void testListarTransacoes() {
        // ARRANGE
        Transacao transacao = new Transacao();
        when(transacaoRepository.listAll()).thenReturn(List.of(transacao));

        // ACT
        List<Transacao> resultado = transacaoService.listarTransacoes();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(transacaoRepository, times(1)).listAll();
    }

    @Test
    public void testBuscarTransacaoPorId() {
        // ARRANGE
        Transacao transacao = new Transacao();
        transacao.setId(10L);
        when(transacaoRepository.findById(10L)).thenReturn(transacao);

        // ACT
        Transacao resultado = transacaoService.buscarTransacaoPorId(10L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        verify(transacaoRepository, times(1)).findById(10L);
    }

    @Test
    public void testBuscarTransacoesPorConta() {
        // ARRANGE
        Transacao transacao = new Transacao();
        when(transacaoRepository.buscarTransacoesPorConta(1L)).thenReturn(List.of(transacao));

        // ACT
        List<Transacao> resultado = transacaoService.buscarTransacoesPorConta(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(transacaoRepository, times(1)).buscarTransacoesPorConta(1L);
    }
}