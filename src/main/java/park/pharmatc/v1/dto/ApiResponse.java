package park.pharmatc.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "success")
public class ApiResponse {
    private String status = "success";
    private Object message;
}