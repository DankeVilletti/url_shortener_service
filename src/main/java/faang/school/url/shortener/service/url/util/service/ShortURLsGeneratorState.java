package faang.school.url.shortener.service.url.util.service;

import faang.school.url.shortener.repository.url.UniqueIncrementerURLRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShortURLsGeneratorState {
    private final UniqueIncrementerURLRepository incrementerURL;

    @Async
    @Transactional
    public void activate() {
        incrementerURL.setActiveGenerationState();
    }

    @Async
    @Transactional
    public void deactivate() {
        incrementerURL.setNonActiveGenerationState();
    }
}
