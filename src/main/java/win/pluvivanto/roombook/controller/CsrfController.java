package win.pluvivanto.roombook.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

  @GetMapping("/csrf")
  public Map<String, String> csrf(HttpServletRequest request) {
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    return Map.of(
        "token", csrfToken.getToken(),
        "headerName", csrfToken.getHeaderName(),
        "parameterName", csrfToken.getParameterName());
  }
}
