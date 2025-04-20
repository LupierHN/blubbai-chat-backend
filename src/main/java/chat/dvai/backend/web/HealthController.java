package chat.dvai.backend.web;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;

@Controller
@AllArgsConstructor
@RequestMapping("api/health")
public class HealthController {
    private final DataSource dataSource;

    @GetMapping()
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
