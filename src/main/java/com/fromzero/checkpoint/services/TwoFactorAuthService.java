package com.fromzero.checkpoint.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class TwoFactorAuthService {

    private final Map<String, String> codigos = new HashMap<>();
    private final Map<String, Long> expiracoes = new HashMap<>();
    private final Random random = new Random();

    private static final long TEMPO_EXPIRACAO_MS = 5 * 60 * 1000; // 5 minutos

    public String gerarCodigo(String email) {
        String codigo = String.format("%06d", random.nextInt(999999));
        codigos.put(email, codigo);
        expiracoes.put(email, System.currentTimeMillis() + TEMPO_EXPIRACAO_MS);

        System.out.println("📤 [2FA] Código gerado para " + email + ": " + codigo + " (expira em 5 minutos)");
        return codigo;
    }

    public boolean verificarCodigo(String email, String codigoInformado) {
        String codigoSalvo = codigos.get(email);
        Long expiraEm = expiracoes.get(email);

        System.out.println("📩 [2FA] Verificando código para: " + email);
        System.out.println("✅ Código salvo: " + codigoSalvo);
        System.out.println("🕒 Expira em: " + expiraEm + " | Agora: " + System.currentTimeMillis());
        System.out.println("🆚 Código informado: " + codigoInformado);

        boolean valido = codigoSalvo != null
            && codigoSalvo.equals(codigoInformado)
            && expiraEm != null
            && System.currentTimeMillis() <= expiraEm;

        if (valido) {
            codigos.remove(email);
            expiracoes.remove(email);
            System.out.println("🟢 [2FA] Código válido. Acesso concedido.");
        } else {
            System.out.println("🔴 [2FA] Código inválido ou expirado.");
        }

        return valido;
    }
}