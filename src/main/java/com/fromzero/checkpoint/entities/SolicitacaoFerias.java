package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime; // Se for usar criadoEm

@Data
@Entity
@Table(name = "solicitacao_ferias")
public class SolicitacaoFerias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sol_fer_id")
    private Long id;

    @Column(name = "sol_fer_data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "sol_fer_data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "sol_fer_observacao")
    private String observacao;

    @Column(name = "sol_fer_status", nullable = false)
    private String status;

    // ***** ADICIONE ESTE CAMPO *****
    @Column(name = "sol_fer_comentario_gestor") // Nome da coluna que você criou no banco
    private String comentarioGestor;
    // ****************************

    @Column(name = "colaborador_id", nullable = false)
    private Long colaboradorId;

    // Mude para EAGER se a Solução 1 do erro anterior foi escolhida,
    // ou mantenha LAZY/remova FetchType se estiver usando JOIN FETCH nos repositórios.
    // Vamos manter LAZY por enquanto, confiando no JOIN FETCH do findByIdWithColaborador.
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "colaborador_id", referencedColumnName = "col_id", insertable = false, updatable = false)
    private Colaborador colaborador;

    // Se quiser mapear criado_em da tabela solicitacao_ferias:
    @Column(name = "criado_em", insertable = false, updatable = false) // Exemplo, ajuste se precisar inserir/atualizar
    private LocalDateTime criadoEm; 
}