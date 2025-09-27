package cheqideh.controller;

import cheqideh.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Controller {

    @Autowired
    protected JwtUtil jwt;

    protected ResponseEntity<?> handleSecureRequest(Supplier<ResponseEntity<?>> endpointLogic) {
        String token = extractToken();

        if (token == null || !jwt.validate(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is invalid or missing."));
        }

        try {
            List<String> roles = jwt.getRoles(token);
            if (roles == null || !roles.contains("TELLER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Insufficient permissions."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token format."));
        }

        return endpointLogic.get();
    }

    private String extractToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;
        String authHeader = attributes.getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7);
    }
}