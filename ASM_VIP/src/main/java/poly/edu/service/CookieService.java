package poly.edu.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class CookieService {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public CookieService(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public Cookie get(String name) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equalsIgnoreCase(name))
                        .findFirst())
                .orElse(null);
    }

    public String getValue(String name) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(name))
                        .map(Cookie::getValue)
                        .findFirst())
                .orElse("");
    }

    public Cookie add(String name, String value, int hours) {
        return Optional.of(new Cookie(name, value))
                .map(cookie -> {
                    cookie.setMaxAge(hours * 60 * 60);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    response.addCookie(cookie);
                    return cookie;
                }).get();
    }

    public void remove(String name) {
        Optional.ofNullable(get(name))
                .ifPresent(cookie -> {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    response.addCookie(cookie);
                });
    }
}