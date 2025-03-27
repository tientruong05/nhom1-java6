package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
public class AppConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;
//    @Autowired
//    private LogInterceptor logInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/java5/asm/crud/**", "/java5/asm/profile", "/java5/asm/cart/**", 
                        "/java5/asm/statistics/**", "/java5/asm/changePassword", "/java5/asm/users/shopping-history", 
                        "/java5/asm/logout", "/java5/asm/change_pw")
                .excludePathPatterns(
                        "/java5/asm/index", "/java5/asm/list-product", "/java5/asm/forgot-pass", 
                        "/java5/asm/products/detail/**", "/java5/asm/login", "/java5/asm/register", 
                        "/java5/asm/activate", "/java5/asm/account/resetPassword", "/java5/asm/checkout", 
                        "/java5/asm/crud/orders/placeOrder"
                );
    }
}