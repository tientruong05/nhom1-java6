package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import poly.edu.entity.UserEntity;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private HttpSession session;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();
        session.setAttribute("securityUri", uri);
        UserEntity user = (UserEntity) session.getAttribute("user");

        if (user == null) { // Chưa đăng nhập
            response.sendRedirect("/java5/asm/login");
            return false;
        }
        if (uri.startsWith("/java5/asm/crud") && !user.isRole()) { // Không phải admin
            response.sendRedirect("/java5/asm/login");
            return false;
        }
        return true;
    }
}