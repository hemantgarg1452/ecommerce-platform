package com.company.ecommerce.auth.util;

import com.company.ecommerce.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(fixedRate = 60000) //every 1 hour
    @Transactional
    public void cleanup(){
        log.info("Starting refresh token cleanup job");
        refreshTokenRepository.deleteExpiredAndRevokedToken();
        log.info("Completed refresh token cleanup job");
    }
}
