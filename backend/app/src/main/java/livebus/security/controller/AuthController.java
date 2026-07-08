package livebus.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import livebus.security.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        try {
            
               UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(
                       loginRequest.username(), loginRequest.password());

            
            Authentication authentication = authenticationManager.authenticate(token);

           
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            
            securityContextRepository.saveContext(context, request, response);

            String username = authentication.getName();
            String roleName = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .findFirst()
                    .orElse("ROLE_PASSENGER");
            
            if (roleName.startsWith("ROLE_")) {
                roleName = roleName.substring(5);
            }
            livebus.security.model.Role role = livebus.security.model.Role.valueOf(roleName);

            return ResponseEntity.ok(new livebus.security.dto.LoginResponse(username, role));
            
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully");
    }
}