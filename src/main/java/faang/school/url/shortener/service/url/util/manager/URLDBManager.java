package faang.school.url.shortener.service.url.util.manager;

import faang.school.url.shortener.entity.url.FreeURL;
import faang.school.url.shortener.entity.url.RegisteredURL;
import faang.school.url.shortener.service.url.util.service.FreeShortURLService;
import faang.school.url.shortener.service.url.util.service.RegisteredURLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class URLDBManager implements ApplicationListener<ContextRefreshedEvent> {
    private final FreeShortURLService freeShortURLs;
    private final RegisteredURLService registeredURLs;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        freeShortURLs.tryStartGenerationAndSaveToDBShortURLsAsync();
    }

    @Async
    @Transactional
    public void checkShortURLsTobeReused() {
        List<String> shortURLs = registeredURLs.getShortURLsThatShouldBeReused();
        if (shortURLs.isEmpty()) {
            log.info("DB: No Short URLs to be reused from archive and save to the pool");
            return;
        }
        freeShortURLs.inputFreeShortURLsForReuse(shortURLs);
    }


    @Async
    @Transactional
    public void checkToArchiveShortURLs() {
        List<RegisteredURL> alreadyNotRegisteredURLs = registeredURLs.getURLsThatShouldBeArchived();
        if (alreadyNotRegisteredURLs.isEmpty()) {
            log.info("DB: No Short URLs to be archived");
            return;
        }
        registeredURLs.archiveRegisteredURLs(alreadyNotRegisteredURLs);
    }

    public List<FreeURL> getFreeURLsFromDBPool(long needURLs) {
        List<FreeURL> shortURLs = freeShortURLs.getAndRemoveFreeURLsFromPool(needURLs);
        shortURLs.addAll(generateMoreShortURLsIfNotEnough(needURLs, shortURLs));
        return shortURLs;
    }

    public RegisteredURL assignHashToFullURLAndRegisterIt(RegisteredURL toRegisterURL) {
        return registeredURLs.assignHashToFullURLAndRegisterIt(toRegisterURL);
    }

    public String redirectToFullURL(String shortURL) {
        RegisteredURL registeredURL = registeredURLs.getRegisteredURLByShortURL(shortURL);
        upCounter(registeredURL);
        return registeredURL.getFullUrl();
    }

    public void upCounter(RegisteredURL registeredURL) {
        registeredURLs.upCounter(registeredURL);
    }

    private List<FreeURL> generateMoreShortURLsIfNotEnough(long requiredShortURLs, List<FreeURL> shortURLs) {
        if (requiredShortURLs > shortURLs.size()) {
            requiredShortURLs -= shortURLs.size();
            freeShortURLs.tryStartGenerationAndSaveToDBShortURLsAsync();
            return freeShortURLs.generateShortURLsWithoutSave(requiredShortURLs);
        }
        return List.of();
    }


}
