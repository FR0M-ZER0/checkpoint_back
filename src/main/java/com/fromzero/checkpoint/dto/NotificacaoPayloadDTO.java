// Crie este arquivo: src/main/java/com/fromzero/checkpoint/dto/NotificacaoPayloadDTO.java
package com.fromzero.checkpoint.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoPayloadDTO {
    private String eventType; // Ex: "ferias_aprovada", "ferias_rejeitada"
    private String message;
    private Long relatedId; // ID da solicitação de férias relacionada
    // Adicione outros campos se precisar enviar mais dados
}