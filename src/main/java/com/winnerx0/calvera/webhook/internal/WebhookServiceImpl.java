package com.winnerx0.calvera.webhook.internal;

import com.winnerx0.calvera.common.ApiResponse;
import com.winnerx0.calvera.webhook.CreateWebhookSecretEvent;
import com.winnerx0.calvera.webhook.WebhookService;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final WebhookSecretRepository webhookSecretRepository;

    @Value("${webhook.encryption.key}")
    private String encryptionKey;

    @Override
    public Optional<String> getSecret(Long projectId) {
        return webhookSecretRepository.findByProjectId(projectId)
                .map(ws -> Encryptors.delux(encryptionKey, ws.getSalt()).decrypt(ws.getSecret()));
    }

    @Override
    public ApiResponse<String> createWebhook(Long projectId){
        handleCreateWebhookSecret(projectId);
        return new ApiResponse<>(true, null, "Webhook secret created successfully");
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCreateWebhookSecret(CreateWebhookSecretEvent event){
        log.info("creating webhook secret");
        handleCreateWebhookSecret(event.projectId());
    }

    private void handleCreateWebhookSecret(Long projectId){

        WebhookSecret webhookSecret = webhookSecretRepository.findByProjectId(projectId)
                .orElseGet(() -> {
                    WebhookSecret ws = new WebhookSecret();
                    ws.setProjectId(projectId);
                    return ws;
                });

        String salt = generateSalt();
        String encryptedSecret = Encryptors.delux(encryptionKey, salt).encrypt(generateSecret());

        webhookSecret.setSalt(salt);
        webhookSecret.setSecret(encryptedSecret);
        webhookSecretRepository.save(webhookSecret);
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return "clv_" + Base64.getEncoder().encodeToString(bytes);
    }

    private String generateSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
