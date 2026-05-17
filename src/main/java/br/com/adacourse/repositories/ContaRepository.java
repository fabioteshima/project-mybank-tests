package br.com.adacourse.repositories;

import br.com.adacourse.models.Conta;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContaRepository implements PanacheRepository<Conta> {

    public Conta buscarContaPorCliente(Long clienteId) {
        return find("titular.id", clienteId).firstResult();
    }
}