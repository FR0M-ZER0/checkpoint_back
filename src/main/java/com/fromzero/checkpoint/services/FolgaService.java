package com.fromzero.checkpoint.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fromzero.checkpoint.repositories.HorasExtrasRepository;
import com.fromzero.checkpoint.entities.HorasExtras;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;
import com.fromzero.checkpoint.entities.SolicitacaoFolga;
import java.util.List;

@Service
public class FolgaService {

    @Autowired
    private HorasExtrasRepository horasExtrasRepository;
    @Autowired
    private SolicitacaoFolgaRepository solicitacaoFolgaRepository;

    public String obterSaldoHoras(Long colaboradorId) {
        List<HorasExtras> horasExtras = horasExtrasRepository.findByColaboradorId(colaboradorId);
        
        double totalHoras = 0.0;
    
        for (HorasExtras horaExtra : horasExtras) {
            String saldo = horaExtra.getSaldo(); // Agora funciona com getSaldo()
            
            try {
                String valor = saldo.replace("h", "").trim().replace(",", ".");
                totalHoras += Double.parseDouble(valor);
            } catch (NumberFormatException e) {
                System.err.println("Formato inválido para saldo: " + saldo);
            }
        }
    
        int horas = (int) totalHoras;
        int minutos = (int) ((totalHoras - horas) * 60);
    
        return String.format("%dh %02dmin", horas, minutos);
    }

    public SolicitacaoFolga agendarFolga(SolicitacaoFolga solicitacao) {
        // Corrigido para usar Long
        Long colaboradorId = solicitacao.getColaboradorId(); 
        String saldoGasto = solicitacao.getSolFolSaldoGasto();

        String saldoDisponivel = obterSaldoHoras(colaboradorId);

        if (!validarSaldo(saldoDisponivel, saldoGasto)) {
            throw new RuntimeException("Saldo de horas insuficiente.");
        }

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