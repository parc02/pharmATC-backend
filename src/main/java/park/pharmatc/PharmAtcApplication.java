package park.pharmatc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PharmAtcApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmAtcApplication.class, args);
    }

}
