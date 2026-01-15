package dopamine.soundock.controller;

import dopamine.soundock.dto.PWLRegisterRequest;
import dopamine.soundock.service.X1280Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class X1280Controller {
    private final X1280Service service;

    public X1280Controller(X1280Service service) {
        this.service = service;
    }

    // React calls: GET /api/v1/auth/status?user=...
    @GetMapping("/status")
    public String checkUserStatus(@RequestParam("userId") String email) {
        return service.isAp(email);
    }

    // React calls: POST /api/v1/auth/register
    @PostMapping("/register")
    public String registerUser(@RequestBody PWLRegisterRequest pwlRegisterRequest) {
        return service.joinAp(pwlRegisterRequest.getEmail());
    }

    // React calls: POST /api/v1/auth/login-trigger
    @PostMapping("/login-trigger")
    public String triggerLogin(@RequestParam("userId") String email, @RequestParam String ip) {
        // This handles the internal getToken -> AES -> getSp sequence privately
        return service.getSp(email, ip);
    }

    // New: Check auth result
    @GetMapping("/result")
    public String checkResult(@RequestParam("userId") String email) {
        return service.checkResult(email);
    }

    // New: Cancel auth
    @PostMapping("/cancel")
    public String cancel(@RequestParam("userId") String email, @RequestParam String sessionId) {
        return service.cancel(email, sessionId);
    }

    // New: Withdrawal
    @PostMapping("/withdrawal")
    public String withdrawal(@RequestParam("userId") String email) {
        return service.withdrawalAp(email);
    }
}
