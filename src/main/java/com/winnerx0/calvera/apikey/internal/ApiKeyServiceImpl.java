package com.winnerx0.calvera.apikey.internal;

import com.winnerx0.calvera.apikey.ApiKeyService;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyServiceImpl(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public boolean validate(String apiKeyHash) {

        return apiKeyRepository.validate(apiKeyHash);
    }
}
