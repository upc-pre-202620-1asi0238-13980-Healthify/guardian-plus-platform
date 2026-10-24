package com.healthify.guardian.platform.iam.infrastructure.scheduling;

import com.healthify.guardian.platform.iam.domain.repositories.OneTimePasswordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Periodically deletes the one-time codes that expired without being used. Nothing depends on it to reject an old
 * code (that is decided by its {@code expiresAt}); it only keeps {@code otp_codes} from growing forever.
 */
@Slf4j
@Component
public class ExpiredOtpCleanupScheduler {

    private final OneTimePasswordRepository oneTimePasswordRepository;

    public ExpiredOtpCleanupScheduler(OneTimePasswordRepository oneTimePasswordRepository) {
        this.oneTimePasswordRepository = oneTimePasswordRepository;
    }

    @Scheduled(cron = "${iam.otp.cleanup.cron:0 0 * * * *}", zone = "${iam.zone-id:America/Lima}")
    public void deleteExpiredCodes() {
        try {
            var deleted = oneTimePasswordRepository.deleteExpiredUnusedBefore(Instant.now());
            if (deleted > 0) {
                log.info("Deleted {} expired one-time codes", deleted);
            }
        } catch (RuntimeException e) {
            log.error("Expired one-time code cleanup failed", e);
        }
    }
}
