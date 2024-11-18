package faang.school.url.shortener.service.url.util.service;

import faang.school.url.shortener.entity.url.ArchiveURL;
import faang.school.url.shortener.entity.url.RegisteredURL;
import faang.school.url.shortener.repository.url.ArchiveURLRepository;
import faang.school.url.shortener.repository.url.RegisteredURLRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisteredURLService {
    private final RegisteredURLRepository registeredURLs;
    private final ArchiveURLRepository archiveURLs;


    @Transactional
    public List<String> getShortURLsThatShouldBeReused() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return archiveURLs.getShortURLsThatShouldBeReused(now)
                .stream().map(ArchiveURL::getShortURL)
                .filter(Objects::nonNull).toList();
    }

    @Async
    @Transactional
    public void archiveRegisteredURLs(List<RegisteredURL> alreadyNotRegisteredURLs) {
        log.info("DB: Archiving {} URLs", alreadyNotRegisteredURLs.size());
        archiveURLs.saveAll(alreadyNotRegisteredURLs.stream()
                .map(ArchiveURL::new).toList());
    }

    public List<RegisteredURL> getURLsThatShouldBeArchived() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return registeredURLs.findURLsThatShouldBeArchived(now);
    }

    public RegisteredURL assignHashToFullURLAndRegisterIt(RegisteredURL toRegisterURL) {
        checkForRegisteredFullURL(toRegisterURL);
        return registeredURLs.save(toRegisterURL);
    }

    @Async
    @Transactional
    public void upCounter(RegisteredURL url) {
        registeredURLs.incrementCount(url);
    }

    @Transactional(readOnly = true)
    public RegisteredURL getRegisteredURLByShortURL(String shortURL) {
        return registeredURLs.findByShortUrl(shortURL)
                .orElseThrow(() -> new RuntimeException("URL" + shortURL + " is not registered"));
    }

    private void checkForRegisteredFullURL(RegisteredURL url) {
        if (registeredURLs.checkForRegisteredFullURL(
                url.getFullUrl(),
                url.getCreatorId(),
                url.getProjectId(),
                url.getExpiresAt()).isPresent()) {
            throw new RuntimeException("URL" + url.getFullUrl() + " is already registered");
        }
    }



}
