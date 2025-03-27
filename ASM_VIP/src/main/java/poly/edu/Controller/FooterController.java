package poly.edu.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/java5/asm/footer")
public class FooterController {

    @RequestMapping("about")
    public String about() {
        return "about";
    }

    @RequestMapping("warranty")
    public String warranty() {
        return "warranty";
    }

    @RequestMapping("term-payment")
    public String termsPayment() {
        return "terms_payment";
    }
}