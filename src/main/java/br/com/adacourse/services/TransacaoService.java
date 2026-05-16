package br.com.adacourse.services;

import br.com.adacourse.enums.TipoConta;
import br.com.adacourse.enums.TipoTransacao;
import br.com.adacourse.models.Conta;
import br.com.adacourse.models.Transacao;
import br.com.adacourse.repositories.ContaRepository;
import br.com.adacourse.repositories.TransacaoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TransacaoService {

    @Inject
    TransacaoRepository transacaoRepository; // <--- Injeção do repositório

    @Inject
    ContaRepository contaRepository; // <--- Injeção para buscar as contas

    @Inject
    EntityManager em;

    @Transactional
    public Transacao depositar(Long contaId, BigDecimal valor){
        Conta entidade = contaRepository.findById(contaId);
        if(entidade == null){
            throw new IllegalArgumentException("Conta não encontrada");
        }
        if(entidade.getTipo() == TipoConta.ELETRONICA){
            throw new UnsupportedOperationException(("Conta do tipo ELETRONICA não permite depósitos."));
        }

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.DEPOSITO);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setContaDestino(entidade);

        transacaoRepository.persist(transacao); // <--- Usando repositório
        em.flush();
        em.refresh(entidade);
        return transacao;
    }

    @Transactional
    public Transacao sacar(Long contaId, BigDecimal valor){
        Conta entidade = contaRepository.findById(contaId);
        if(entidade == null){
            throw new IllegalArgumentException("ContaId não encontrada");
        }
        if(entidade.getTipo() == TipoConta.ELETRONICA){
            throw new UnsupportedOperationException(("Conta do tipo ELETRONICA não permite saques"));
        }
        if(entidade.getSaldo().compareTo(valor) < 0){
            throw new IllegalStateException("Saldo insuficiente para realizar o saque");
        }

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setContaOrigem(entidade);

        transacaoRepository.persist(transacao); // <--- Usando repositório
        em.flush();
        em.refresh(entidade);
        return transacao;
    }

    @Transactional
    public Transacao transferir(Long contaOrigemId, Long contaDestinoId, BigDecimal valor){
        Conta contaOrigem = contaRepository.findById(contaOrigemId);
        if((contaOrigem == null)){
            throw new IllegalArgumentException("ContaId origem não encontrado");
        }
        Conta contaDestino = contaRepository.findById(contaDestinoId);
        if((contaDestino == null)){
            throw new IllegalArgumentException("ContaId destino não encontrado");
        }
        if (contaOrigem.getSaldo().compareTo(valor) < 0) {
            throw new IllegalStateException("Saldo insuficiente para realizar a transferência");
        }
        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.TRANSFERENCIA);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setContaOrigem(contaOrigem);
        transacao.setContaDestino(contaDestino);

        transacaoRepository.persist(transacao); // <--- Usando repositório
        em.flush();
        em.refresh(contaOrigem);
        em.refresh(contaDestino);
        return transacao;
    }

    public List<Transacao> listarTransacoes(){
        return transacaoRepository.listAll();
    }

    public Transacao buscarTransacaoPorId(Long id){
        return transacaoRepository.findById(id);
    }

    public List<Transacao> buscarTransacoesPorConta(Long contaId) {
        // Lógica delegada para o repositório específico
        return transacaoRepository.buscarTransacoesPorConta(contaId);
    }
}