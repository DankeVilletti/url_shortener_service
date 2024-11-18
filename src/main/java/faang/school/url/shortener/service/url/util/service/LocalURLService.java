package faang.school.url.shortener.service.url.util.service;

import faang.school.url.shortener.config.url.URLConfig;
import faang.school.url.shortener.entity.url.FreeURL;
import faang.school.url.shortener.service.url.util.manager.URLCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class LocalURLService {
    private final Queue<FreeURL> freeURLQueue = new ConcurrentLinkedQueue<>();
    private final AtomicReference<String> lockerUniqueID = new AtomicReference<>();

    private final URLConfig URLConfig;

    @Async
    public void tryToFillFreeURLsInLocal(URLCacheManager cacheManager) {
        String lockerUniqueID = UUID.randomUUID().toString();
        int needAddToLocal = checkForStartPoolFilling(lockerUniqueID);
        if (needAddToLocal > 0) {
            List<FreeURL> shortURLs = cacheManager.getFreeURLsFromCachePool(needAddToLocal);
            addFreeShortURLsToLocalAndFinishFillingPool(lockerUniqueID, shortURLs);
        }
    }


    public Optional<FreeURL> getAndRemoveFreeShortURLFromLocal() {
        FreeURL shortUrl = freeURLQueue.poll();
        if (shortUrl == null) {
            log.info("LOCAL: No free URL in local pool.");
            return Optional.empty();
        }
        log.info("LOCAL: Got and removed free URL from local pool. {}", shortUrl);
        return Optional.of(shortUrl);
    }

    private int checkForStartPoolFilling(String lockerUniqueID) {
        if (!isRequiredToAddShortURLsToLocal()) return 0;
        if (!tryLockForAddToLocal(lockerUniqueID)) return 0;
        return getBatchSizeForFilling();
    }

    private void addFreeShortURLsToLocalAndFinishFillingPool(String lockerUniqueID, List<FreeURL> freeURLS) {
        freeURLQueue.addAll(freeURLS);
        log.info("LOCAL: Added {} URLs to the local pool. Finished filling pool by Thread Locker ID {}.", freeURLS.size(), lockerUniqueID);
        unlock(lockerUniqueID);
    }

    private int getBatchSizeForFilling() {
        int amountToAdd = URLConfig.getLocalMaxSize() - freeURLQueue.size();
        log.info("LOCAL: Need to add {} short URLs.", amountToAdd);
        return amountToAdd;
    }

    private boolean tryLockForAddToLocal(String lockerUniqueID) {
        boolean locked = this.lockerUniqueID.compareAndSet(null, lockerUniqueID);
        if (locked) {
            log.info("LOCAL: This thread is adding short URLs. Thread Locker ID {}", lockerUniqueID);
        } else {
            log.info("LOCAL: Another Thread Locker ID {} is already adding short URLs. Current Thread Locker ID {}", this.lockerUniqueID, lockerUniqueID);
        }
        return locked;
    }

    private void unlock(String lockerUniqueID) {
        this.lockerUniqueID.set(null);
        log.info("LOCAL: Locker {} was released.", lockerUniqueID);
    }

    private boolean isRequiredToAddShortURLsToLocal() {
        boolean isNeeded = freeURLQueue.size() < URLConfig.getLocalMaxSize();
        if (isNeeded) {
            log.info("LOCAL: Need to fill URLs pool.");
        } else {
            log.info("LOCAL: No need to fill URLs pool.");
        }
        return isNeeded;
    }


}
