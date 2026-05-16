package br.com.adacourse.services;

import br.com.adacourse.models.Cliente;
import br.com.adacourse.repositories.ClienteRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;
import java.time.Instant;

@ApplicationScoped
public class AuthService {

    @Inject
    ClienteRepository clienteRepository;

    public String autenticacao(String email, String senha){
         Cliente entidade = clienteRepository.find("email", email).firstResult();
        if(entidade == null || !BCrypt.checkpw(senha, entidade.getSenha())){
            throw new WebApplicationException("Credenciais inválidas", Response.Status.UNAUTHORIZED);
        }
        return Jwt.issuer("adacourse")
                .upn(entidade.getEmail())
                .groups(entidade.getRole().toString())
                .expiresAt(Instant.now().plusSeconds(3600))
                .sign();
    }
}
