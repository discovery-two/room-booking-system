package win.pluvivanto.roombook.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class AuthUtil {

  private AuthUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Get the current authenticated user's email from JWT token
   *
   * @return user email or "anonymous" if not authenticated
   */
  public static String getCurrentUserEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      // Try different claim names that Cognito might use
      String email = jwt.getClaimAsString("email");
      if (email != null) {
        return email;
      }

      // Fallback to username if email not available
      String username = jwt.getClaimAsString("cognito:username");
      if (username != null) {
        return username;
      }

      // Last resort - use subject
      return jwt.getSubject();
    }

    return "anonymous";
  }

  /**
   * Get the current authenticated user's subject (unique ID) from JWT token
   *
   * @return user subject or null if not authenticated
   */
  public static String getCurrentUserSubject() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      return jwt.getSubject();
    }

    return null;
  }

  /**
   * Check if current user is authenticated
   *
   * @return true if authenticated, false otherwise
   */
  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof Jwt;
  }
}
