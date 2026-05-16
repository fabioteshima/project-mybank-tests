package br.com.adacourse.services;

import br.com.adacourse.enums.TipoConta;
import br.com.adacourse.models.Conta;
import br.com.adacourse.models.Transacao;
import br.com.adacourse.repositories.ContaRepository;
import br.com.adacourse.repositories.TransacaoRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
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
    public void testDepositoSucesso() {
        Conta conta = new Conta();
        conta.setTipo(TipoConta.CORRENTE);
        when(contaRepository.findById(1L)).thenReturn(conta);

        Transacao t = transacaoService.depositar(1L, BigDecimal.valueOf(100));
        assertNotNull(t);
        assertEquals(BigDecimal.valueOf(100), t.getValor());
        verify(transacaoRepository, times(1)).persist(any(Transacao.class));
    }

    @Test
    public void testDepositoContaEletronicaFalha() {
        Conta conta = new Conta();
        conta.setTipo(TipoConta.ELETRONICA);
        when(contaRepository.findById(1L)).thenReturn(conta);

        assertThrows(UnsupportedOperationException.class, () -> {
            transacaoService.depositar(1L, BigDecimal.valueOf(100));
        });
    }

    @Test
    public void testSacarSaldoInsuficiente() {
        Conta conta = new Conta();
        conta.setTipo(TipoConta.CORRENTE);
        conta.setSaldo(BigDecimal.valueOf(10)); // Saldo baixo
        when(contaRepository.findById(1L)).thenReturn(conta);

        assertThrows(IllegalStateException.class, () -> {
            transacaoService.sacar(1L, BigDecimal.valueOf(100));
        });
    }
}
