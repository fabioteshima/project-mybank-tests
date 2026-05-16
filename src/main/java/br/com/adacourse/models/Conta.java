package br.com.adacourse.models;

import br.com.adacourse.enums.TipoConta;
import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoConta tipo;

    @OneToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", nullable = false)
    private Cliente titular;

    @OneToMany(mappedBy = "contaOrigem")
    private List<Transacao> transacoesOrigem = new ArrayList<>();

    @OneToMany(mappedBy = "contaDestino")
    private List<Transacao> transacoesDestino = new ArrayList<>();

    @Formula("( (SELECT COALESCE(SUM(t.valor),0) FROM transacao t WHERE t.conta_destino_id = id) - " +
            "(SELECT COALESCE(SUM(t.valor),0) FROM transacao t WHERE t.conta_origem_id = id) )")
    private BigDecimal saldo;

    public Conta() {}

    public Conta(Long id, String numero, TipoConta tipo, Cliente titular) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.titular = titular;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public void setTipo(TipoConta tipo) {
        this.tipo = tipo;
    }

    public Cliente getTitular() {
        return titular;
    }

    public void setTitular(Cliente titular) {
        this.titular = titular;
    }

    public List<Transacao> getTransacoesOrigem() {
        return transacoesOrigem;
    }

    public void setTransacoesOrigem(List<Transacao> transacoesOrigem) {
        this.transacoesOrigem = transacoesOrigem;
    }

    public List<Transacao> getTransacoesDestino() {
        return transacoesDestino;
    }

    public void setTransacoesDestino(List<Transacao> transacoesDestino) {
        this.transacoesDestino = transacoesDestino;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Conta conta = (Conta) o;
        return id == conta.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
