package com.fromzero.checkpoint.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fromzero.checkpoint.repositories.HorasExtrasRepository;
import com.fromzero.checkpoint.models.HorasExtras;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;
import com.fromzero.checkpoint.models.SolicitacaoFolga;
import java.util.List;

@Service
public class FolgaService {

    @Autowired
    private HorasExtrasRepository horasExtrasRepository;
    @Autowired
    private SolicitacaoFolgaRepository solicitacaoFolgaRepository;

    public String obterSaldoHoras(Integer colaboradorId) {
        // 1. Buscar todas as horas extras do colaborador
        List<HorasExtras> horasExtras = horasExtrasRepository.findByColaboradorId(colaboradorId);

        // 2. Calcular o saldo total de horas
        int saldoHoras = 0;
        int saldoMinutos = 0;

        for (HorasExtras horaExtra : horasExtras) {
            // Supondo que ext_saldo armazena "Xh" ou "Xh Ymin"
            String saldo = horaExtra.getExtSaldo();
            String[] partes = saldo.split("h");
            int horas = Integer.parseInt(partes[0].trim());
            saldoHoras += horas;

            if (partes.length > 1) {
                String minutosStr = partes[1].replace("min", "").trim();
                if (!minutosStr.isEmpty()) {
                    int minutos = Integer.parseInt(minutosStr);
                    saldoMinutos += minutos;
                }
            }
        }

        // 3. Ajustar minutos e horas (se minutos >= 60)
        saldoHoras += saldoMinutos / 60;
        saldoMinutos = saldoMinutos % 60;

        // 4. Formatar o saldo para retornar
        return String.format("%02dh: %02dmin", saldoHoras, saldoMinutos);
    }

    public SolicitacaoFolga agendarFolga(SolicitacaoFolga solicitacao) {
        Integer colaboradorId = solicitacao.getColaboradorId();
        String saldoGasto = solicitacao.getSolFolSaldoGasto();

        // 1. Obter o saldo do colaborador
        String saldoDisponivel = obterSaldoHoras(colaboradorId);

        // 2. Validar o saldo
        if (!validarSaldo(saldoDisponivel, saldoGasto)) {
            throw new RuntimeException("Saldo de horas insuficiente.");
        }

        // 3. Se o saldo for suficiente, agendar a folga
        // 4. Se a solicitação for bem-sucedida
        return solicitacaoFolgaRepository.save(solicitacao);
        }

    private boolean validarSaldo(String saldoDisponivel, String saldoGasto) {
        // Implementar a lógica de validação aqui
        // Comparar o saldo disponível com o saldo gasto
        // Retornar true se o saldo for suficiente, false caso contrário
        // Exemplo (simplificado):

        String[] saldoDisponivelParts = saldoDisponivel.split("h");
        String[] saldoGastoParts = saldoGasto.split("h");

        int saldoDisponivelHoras = Integer.parseInt(saldoDisponivelParts[0].trim());
        int saldoGastoHoras = Integer.parseInt(saldoGastoParts[0].trim());

        return saldoDisponivelHoras >= saldoGastoHoras;

        }

    // ... outros métodos ...
}