package faang.school.url.shortener.service.url.util.service;

import faang.school.url.shortener.config.url.URLConfig;
import faang.school.url.shortener.entity.url.FreeURL;
import faang.school.url.shortener.entity.url.UniqueIncrementerURL;
import faang.school.url.shortener.repository.url.FreeURLRepository;
import faang.school.url.shortener.repository.url.UniqueIncrementerURLRepository;
import faang.school.url.shortener.util.hash.Hasher;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.LongStream;


@Slf4j
@Component
@RequiredArgsConstructor
public class FreeShortURLService {
    private final Hasher hasherBase62;
    private final FreeURLRepository freeURLs;
    private final ShortURLsGeneratorState generatorState;
    private final UniqueIncrementerURLRepository incrementerURL;
    private final URLConfig URLConfig;


    @Async
    @Transactional
    public void tryStartGenerationAndSaveToDBShortURLsAsync() {
        String threadID = LocalDateTime.now() + UUID.randomUUID().toString();
        if (!isGenerationNeeded(threadID)) return;
        if (!tryToBeStarterForGeneration(threadID)) {
            log.warn("DB: No locked counter for Thread ID {}. Generation will be skipped.", threadID);
            return;
        }
        if (!permissionToStartGeneration(threadID)) return;
        startGenerationAndSaveToDBShortURLsAsync(threadID);
    }

    @Async
    @Transactional
    public void inputFreeShortURLsForReuse(List<String> shortURLs) {
        log.info("DB: {} Short URLs to be reused from archive and save to the pool", shortURLs.size());
        freeURLs.saveAll(shortURLs.stream()
                .filter(Objects::nonNull)
                .map(FreeURL::new)
                .toList());
    }

    @Transactional
    public List<FreeURL> getAndRemoveFreeURLsFromPool(long countURLs) {
        List<FreeURL> shortURLs = freeURLs.getAndRemoveFreeURLs(countURLs);
        log.info("DB: Get and remove {} shortURLs from pool.", shortURLs.size());
        return shortURLs;
    }

    public List<FreeURL> generateShortURLsWithoutSave(long requiredShortURLs) {
        log.info("DB: Short URLs in the pool are not enough. Start generation without saving to pool for {} short URLs.", requiredShortURLs);
        List<Long> uniqueKeys = getUniqueKeysForURL(requiredShortURLs, "ThreadID: ExtraWithoutSave");
        return generateShortURLs(uniqueKeys);
    }

    private List<FreeURL> generateShortURLs(List<Long> uniqueKeys) {
        return uniqueKeys.stream()
                .map(this::extraGenerateWithoutSave)
                .map(FreeURL::new)
                .toList();
    }

    private void generateAndSaveToPool(String threadID, List<Long> uniqueKeys) {
        List<FreeURL> shortURLs = generateShortURLs(uniqueKeys);
        freeURLs.saveAll(shortURLs);
        freeURLs.flush();
        giveAccessToGenerateToOthers();
        log.info("DB: Finished shortURLs generation in DB by Thread ID {}. All shortURLs was saved.", threadID);
    }

    private void giveAccessToGenerateToOthers() {
        generatorState.deactivate();
    }

    private String extraGenerateWithoutSave(long key) {
        return hasherBase62.hash(key);
    }

    private long count() {
        return freeURLs.count();
    }

    private void startGenerationAndSaveToDBShortURLsAsync(String threadID) {
        notifyAboutShortURLsGenerationByThisThread(threadID);
        long batchSize = countBatchSize();
        List<Long> uniqueKeys = getUniqueKeysForURL(batchSize, threadID);
        generateAndSaveToPool(threadID, uniqueKeys);
    }

    private List<Long> getUniqueKeysForURL(long batchSize, String threadID) {
        boolean success = false;
        long newCounter = 0;
        while (!success) {
            try {
                newCounter = incrementerURL.getAndIncrease(batchSize);
                success = true;
            } catch (Exception e) {
                log.warn("DB: Failed to get unique keys for generate shortURLs. Try again. Thread ID: {}", threadID);
            }
        }
        List<Long> uniqueKeys = LongStream.rangeClosed((newCounter - batchSize + 1), newCounter).boxed().toList();
        log.info("DB: Get unique keys for generate {} shortURLs. Thread ID: {}", uniqueKeys.size(), threadID);
        return uniqueKeys;
    }

    private void notifyAboutShortURLsGenerationByThisThread(String threadID) {
        log.info("DB: Start shortURLs generation in DB by Thread ID {}", threadID);
        generatorState.activate();
    }


    private long countBatchSize() {
        return URLConfig.getDbMaxSize() - count();
    }


    private boolean isGenerationNeeded(String threadID) {
        boolean isNeeded = count() < URLConfig.getDbLimit();
        log.info("DB: Checked if generation is needed. Result: {}. Thread ID: {}.", isNeeded, threadID);
        return isNeeded;
    }

    private boolean permissionToStartGeneration(String threadID) {
        Optional<UniqueIncrementerURL> counterOpt = incrementerURL.findAll().stream().findFirst();
        return counterOpt.filter(counter -> counter.permissionToStartGeneration(threadID)).isPresent();
    }

    private boolean tryToBeStarterForGeneration(String threadID) {
        boolean isApply = false;
        try {
            long lockedCounter = incrementerURL.tryToLockForGeneration(threadID);
            if (lockedCounter == 1) {
                isApply = true;
                log.info("DB: Thread ID: {} is starter for generation", threadID);
            }
        } catch (OptimisticLockException e) {
            log.warn("DB: Thread ID: {} didn't become starter for generation. Generation skipped.", threadID);
        }
        return isApply;
    }


}
