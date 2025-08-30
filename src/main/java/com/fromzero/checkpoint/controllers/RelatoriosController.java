import com.fromzero.checkpoint.dto.FiltroDataRequest;
import com.fromzero.checkpoint.services.RelatoriosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/relatorios")
public class RelatoriosController {

    private final RelatoriosService relatoriosService;

    public RelatoriosController(RelatoriosService relatoriosService) {
        this.relatoriosService = relatoriosService;
    }

    // Endpoint para dados dinâmicos (férias/folgas)
    @GetMapping("/ferias-folgas")
    public ResponseEntity<List<?>> getRelatorios(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim) {
        return ResponseEntity.ok(relatoriosService.buscarDados(dataInicio, dataFim));
    }

    // Endpoint para gerar PDF
    @PostMapping("/gerar-pdf")
    public ResponseEntity<byte[]> gerarPdf(@RequestBody FiltroDataRequest filtro) {
        byte[] pdf = relatoriosService.gerarPdf(filtro);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}