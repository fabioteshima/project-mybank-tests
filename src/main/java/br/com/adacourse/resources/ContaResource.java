package br.com.adacourse.resources;

import br.com.adacourse.dto.conta.ContaReqDTO;
import br.com.adacourse.dto.conta.ContaRespCriadoDTO;
import br.com.adacourse.dto.conta.ContaRespDetalhadoDTO;
import br.com.adacourse.dto.transacao.*;
import br.com.adacourse.models.Cliente;
import br.com.adacourse.models.Conta;
import br.com.adacourse.models.Transacao;
import br.com.adacourse.services.ContaService;
import br.com.adacourse.services.TransacaoService;
import br.com.adacourse.repositories.ContaRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/contas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContaResource {

    @Inject
    ContaService contaService;

    @Inject
    TransacaoService transacaoService;

    @Inject
    ContaRepository contaRepository;

    @POST
    @RolesAllowed("GERENTE")
    public Response criarConta(@Valid ContaReqDTO dto){
        try {
            Conta entidade = new Conta();
            entidade.setTipo(dto.tipo());
            Cliente titular = new Cliente();
            titular.setId(dto.cliente().id());
            entidade.setTitular(titular);

            Conta criada = contaService.criarConta(entidade);
            ContaRespCriadoDTO responseDTO = ContaRespCriadoDTO.converterParaDTO(criada);
            return Response.created(URI.create("/contas/" + criada.getId())).entity(responseDTO).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"erro\":\"" + e.getMessage() + "\"}")
                    .build();
        }
        catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"erro\":\"Conta não encontrada\"}")
                    .build();
        }
    }

    @GET
    @RolesAllowed("GERENTE")
    public Response listarContas(){
        List<ContaRespDetalhadoDTO> lista = contaService.listarContas()
                .stream()
                .map(ContaRespDetalhadoDTO::converteParaDTO)
                .collect(Collectors.toList());
        return Response.ok(lista).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public Response buscarContaPorId(@PathParam("id") Long id, @Context SecurityContext sc){
        Conta entidade = contaService.buscarContaPorId(id);
        if(entidade == null){
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"erro\":\"Conta Id não encontrada\"}")
                    .build();
        }
        String usuarioLogado = sc.getUserPrincipal().getName();
        if (sc.isUserInRole("GERENTE")) {
            return Response.ok(ContaRespDetalhadoDTO.converteParaDTO(entidade)).build();
        }
        if (sc.isUserInRole("CLIENTE") && entidade.getTitular().getEmail().equals(usuarioLogado)) {
            return Response.ok(ContaRespDetalhadoDTO.converteParaDTO(entidade)).build();
        }
        return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"erro\":\"Acesso não autorizado\"}")
                .build();
    }

    @POST
    @Path("/{id}/deposito")
    public Response depositar(@PathParam("id") Long id, @Valid DepositoReqDTO dto){
        try {
            Transacao transacao = transacaoService.depositar(id, dto.valor());
            Conta contaAtualizada = contaRepository.findById(id); // <--- Alterado de Conta.findById
            return Response.ok(DepositoRespDTO.converterParaDTO(transacao, contaAtualizada)).build();
        } catch (UnsupportedOperationException e){
            return Response.status(422).entity(Map.of("erro", e.getMessage())).build();
        } catch (IllegalArgumentException e){
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("erro", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{id}/saque")
    @RolesAllowed("CLIENTE")
    public Response sacar(@PathParam("id") Long id, @Valid SaqueReqDTO dto){
        try {
            Transacao transacao = transacaoService.sacar(id, dto.valor());
            Conta contaAtualizada = contaRepository.findById(id);
            return Response.ok(SaqueRespDTO.converterParaDTO(transacao, contaAtualizada)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("erro", e.getMessage())).build();
        } catch (UnsupportedOperationException | IllegalStateException e){
            return Response.status(422).entity(Map.of("erro", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{id}/transferencia")
    @RolesAllowed("CLIENTE")
    public Response transferir(@PathParam("id") Long contaOrigemId, @Valid TransferenciaReqDTO dto){
        try {
            Transacao transacao = transacaoService.transferir(contaOrigemId, dto.contaDestino().id(), dto.valor());
            Conta contaOrigemAtualizado = contaRepository.findById(contaOrigemId); // <--- Alterado
            Conta contaDestinoAtualizado = contaRepository.findById(dto.contaDestino().id()); // <--- Alterado
            return Response.ok(TransferenciaRespDTO.converterParaDTO(
                            transacao, contaOrigemAtualizado, contaDestinoAtualizado))
                    .build();
        } catch(IllegalArgumentException e){
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("erro", e.getMessage())).build();
        } catch (IllegalStateException e){
            return Response.status(422).entity(Map.of("erro", e.getMessage())).build();
        }
    }
}