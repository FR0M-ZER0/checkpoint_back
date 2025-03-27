import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fromzero.checkpoint.services.FolgaService;
import com.fromzero.checkpoint.models.SolicitacaoFolga;

@RestController
@RequestMapping("/api/folga")
@CrossOrigin(origins = "http://localhost:5173") // ou a porta do seu frontend
public class FolgaController {

    @Autowired
    private FolgaService folgaService;

    @GetMapping("/saldo")
    public ResponseEntity<String> getSaldoHoras(@RequestParam Integer colaboradorId) {
        try {
            String saldo = folgaService.obterSaldoHoras(colaboradorId);
            return ResponseEntity.ok(saldo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Retorna a mensagem de erro
        }
    }

    @PostMapping
    public ResponseEntity<SolicitacaoFolga> solicitarFolga(@RequestBody SolicitacaoFolga solicitacao) {
        try {
            SolicitacaoFolga folgaAgendada = folgaService.agendarFolga(solicitacao);
            return ResponseEntity.ok(folgaAgendada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // Retorna a mensagem de erro
        }
    }

    // ... outros endpoints ...
}