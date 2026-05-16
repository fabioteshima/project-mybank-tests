package br.com.adacourse.repositories;

import br.com.adacourse.models.Transacao;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TransacaoRepositoryTest {

    @Inject
    TransacaoRepository transacaoRepository;

    @Inject
    EntityManager em;

    @Test
    @Transactional
    public void testBuscarTransacoesPorConta() {
        // Como o repositório usa uma query JPQL real com o EntityManager,
        // este teste roda integrado com o H2/Postgres de teste (Dev Services)

        // Cenário: Criar contas e transações mockadas no banco de dados temporário
        // e validar se o método `buscarTransacoesPorConta` retorna os dados corretos.

        List<Transacao> transacoes = transacaoRepository.buscarTransacoesPorConta(1L);
        assertNotNull(transacoes);
        // Adicione asserts adicionais baseados nos dados populados no seu import.sql ou criados no teste
    }
}