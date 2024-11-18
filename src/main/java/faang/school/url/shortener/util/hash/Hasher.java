package faang.school.url.shortener.util.hash;

public interface Hasher {
    String hash(String str);
    String hash(long num);
}
