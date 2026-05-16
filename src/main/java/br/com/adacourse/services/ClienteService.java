package br.com.adacourse.services;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.repositories.ClienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

@ApplicationScoped
public class ClienteService {

    @Inject
    ClienteRepository clienteRepository; // <--- Injeção do repositório

    @Transactional
    public Cliente cadastrarCliente(Cliente cliente){
        if(clienteRepository.find("cpf", cliente.getCpf()).firstResult() != null){
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        String hash = BCrypt.hashpw(cliente.getSenha(), BCrypt.gensalt(10));
        cliente.setSenha(hash);
        clienteRepository.persist(cliente); // <--- Usando repositório
        return cliente;
    }

    public List<Cliente> listarClientes(){
        return clienteRepository.listAll();
    }

    public Cliente buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Transactional
    public Cliente atualizarCliente(Long id, Cliente atualizado){
        Cliente existente = clienteRepository.findById(id);
        if(existente == null){
            throw new EntityNotFoundException("Cliente não encontrado com id");
        }
        return validarEAtualizar(existente, atualizado);
    }

    private Cliente validarEAtualizar(Cliente existente, Cliente atualizado){
        if (atualizado.getCpf() != null && !atualizado.getCpf().equals(existente.getCpf())) {
            throw new IllegalArgumentException("CPF não pode ser atualizado");
        }
        existente.setNome(atualizado.getNome());
        existente.setEmail(atualizado.getEmail());
        String novaHash = BCrypt.hashpw(atualizado.getSenha(), BCrypt.gensalt(10));
        existente.setSenha(novaHash);
        return existente;
    }
}
