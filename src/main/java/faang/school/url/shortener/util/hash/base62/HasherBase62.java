package faang.school.url.shortener.util.hash.base62;

import faang.school.url.shortener.util.hash.Hasher;
import org.springframework.stereotype.Component;

@Component
public class HasherBase62 implements Hasher {

    private static final String BASE62_CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public String hash(String str) {
        return toBase62(str.hashCode());
    }

    @Override
    public String hash(long num) {
        return toBase62(num);
    }

    private String toBase62(long value) {
        StringBuilder result = new StringBuilder();
        while (value > 0) {
            int remainder = (int) (value % 62);
            result.insert(0, BASE62_CHARSET.charAt(remainder));
            value /= 62;
        }
        return result.toString();
    }
}
