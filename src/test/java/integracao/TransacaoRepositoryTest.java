package integracao;

import br.com.adacourse.models.Transacao;
import br.com.adacourse.repositories.TransacaoRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class TransacaoRepositoryTest {

    @Inject
    TransacaoRepository transacaoRepository;

    @Inject
    EntityManager em;

    @Test
    @Transactional
    public void testBuscarTransacoesPorConta() {
        // ARRANGE
        Long contaId = 1L;

        // ACT
        List<Transacao> transacoes = transacaoRepository.buscarTransacoesPorConta(contaId);

        // ASSERT
        assertNotNull(transacoes);
    }

    @Test
    @Transactional
    public void testBuscarTransacoesPorContaInexistente() {
        // ARRANGE
        Long contaIdInexistente = 999L;

        // ACT
        List<Transacao> transacoes = transacaoRepository.buscarTransacoesPorConta(contaIdInexistente);

        // ASSERT
        assertNotNull(transacoes); // a lista não deve ser nula
        org.junit.jupiter.api.Assertions.assertTrue(transacoes.isEmpty(),
                "Esperado que não haja transações para a conta inexistente");
    }
}