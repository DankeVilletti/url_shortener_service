package faang.school.url.shortener.validator;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.Set;

@Component
@RequiredArgsConstructor
public class URLValidator {
    private final Validator urlValidator;

    public void validate(Object url) {
        Set<ConstraintViolation<Object>> violations = urlValidator.validate(url);

        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation errors:");
            for (ConstraintViolation<Object> violation : violations) {
                errorMessage.append("\n")
                        .append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage());
            }
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }
}