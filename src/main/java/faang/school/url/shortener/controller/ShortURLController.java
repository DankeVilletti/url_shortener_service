package faang.school.url.shortener.controller;

import faang.school.url.shortener.dto.URLToRegisterDTO;
import faang.school.url.shortener.entity.url.RegisteredURL;
import faang.school.url.shortener.service.url.URLManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s")
@Validated
public class ShortURLController {

    public final URLManager urlManager;

    @GetMapping("/{shortURL}")
    @ResponseStatus(HttpStatus.FOUND)
    public String redirectToFullURL(@PathVariable String shortURL) {
        return urlManager.redirectToFullURL(shortURL);
    }


    @PostMapping("/r")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisteredURL assignHashToFullURLAndRegisterIt(@Valid @RequestBody URLToRegisterDTO toRegisterDTO) {
        return urlManager.assignHashToFullURLAndRegisterIt(toRegisterDTO);
    }


}
