package br.com.adacourse.repositories;

import br.com.adacourse.models.Transacao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class TransacaoRepository implements PanacheRepository<Transacao> {

    @Inject
    EntityManager em;

    public List<Transacao> buscarTransacoesPorConta(Long contaId) {
        return em.createQuery(
                        "SELECT t FROM Transacao t " +
                                "WHERE t.contaOrigem.id = :contaId OR t.contaDestino.id = :contaId " +
                                "ORDER BY t.dataHora ASC", Transacao.class)
                .setParameter("contaId", contaId)
                .getResultList();
    }
}