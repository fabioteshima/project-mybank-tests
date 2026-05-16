package br.com.adacourse.services;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Conta;
import br.com.adacourse.repositories.ContaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class ContaService {

    @Inject
    ClienteService clienteService;

    @Inject
    ContaRepository contaRepository;

    @Inject
    EntityManager em;

    @Transactional
    public Conta criarConta(Conta conta){
        if (conta == null || conta.getTitular() == null || conta.getTitular().getId() == null) {
            throw new IllegalArgumentException("Requisição inválida: titular id é obrigatório");
        }
        Cliente titular = clienteService.buscarClientePorId(conta.getTitular().getId());
        if (titular == null) {
            throw new IllegalArgumentException("Cliente com id " + conta.getTitular().getId() + " não encontrado");
        }
        conta.setTitular(titular);
        conta.setNumero("0000-0");
        conta.setSaldo(BigDecimal.ZERO);
        contaRepository.persist(conta); // <--- Alterado em.persist por contaRepository.persist
        em.flush();
        String principal = String.format("%04d", conta.getId());
        int digito = (conta.getId().intValue() % 10);
        conta.setNumero(principal + "-" + digito);
        em.merge(conta);
        return conta;
    }

    public List<Conta> listarContas(){
        return contaRepository.listAll();
    }

    public Conta buscarContaPorId(Long id){
        return contaRepository.findById(id);
    }
}