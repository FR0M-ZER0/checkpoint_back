package com.fromzero.checkpoint.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Gestor;
import com.fromzero.checkpoint.repositories.GestorRepository;
import com.fromzero.checkpoint.services.EmailService;
import com.fromzero.checkpoint.services.TwoFactorAuthService;

@RestController
public class GestorController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private TwoFactorAuthService authService;
	
    @Autowired
    private GestorRepository repository;

    @PostMapping("/gestor")
    public Gestor cadastrarGestor(@RequestBody Gestor gestor) {
        return repository.save(gestor);
    }

    @GetMapping("/gestor")
    public List<Gestor> obterGestores() {
        return repository.findAll();
    }

    @GetMapping("/gestor/{id}")
    public ResponseEntity<Gestor> obterGestor(@PathVariable Long id) {
        return repository.findById(id)
            .map(gestor -> ResponseEntity.ok(gestor))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> logarGestor(@RequestBody Gestor g) {
        Gestor gestor = repository.findByEmail(g.getEmail()).orElseThrow(
            () -> new RuntimeException("Gestor não encontrado")
        );

        if (gestor.getSenha().equals(g.getSenha())) {
            // Enviar código 2FA por e-mail
            String codigo = authService.gerarCodigo(g.getEmail());
            emailService.enviarCodigoVerificacao(g.getEmail(), codigo);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Código de verificação enviado para o e-mail.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha inválida");
        }
    }

    @PostMapping("/admin/verificar-codigo")
    public ResponseEntity<?> verificarCodigo(@RequestParam String email, @RequestParam String codigo) {
        if (authService.verificarCodigo(email, codigo)) {
            Gestor gestor = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Gestor não encontrado"));
            return ResponseEntity.ok(gestor); // Retorna os dados do gestor para o front salvar
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido.");
        }
    }
}