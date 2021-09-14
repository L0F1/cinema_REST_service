package cinema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class Controller {

    // Check properties files and env for variable "password", default "123"
    @Value("${password:123}")
    private String PASSWORD;

    Cinema cinema = new Cinema();

    @GetMapping("/seats")
    Cinema getSeats() {
        return cinema;
    }

    @PostMapping("/purchase")
    ResponseEntity<ObjectNode> createPurchase(@RequestBody Map<String, Integer> seat) {

        String errorMessage;

        try {
            return new ResponseEntity<>(cinema.purchaseSeat(seat.get("row"), seat.get("column")),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            errorMessage = "The number of a row or a column is out of bounds!";
        } catch (UnavailableSeatException e) {
            errorMessage = "The ticket has been already purchased!";
        }

        ObjectMapper mapper = new ObjectMapper();
        return new ResponseEntity<>(mapper.createObjectNode().put("error", errorMessage),
                HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/return")
    ResponseEntity<ObjectNode> deletePurchase(@RequestBody Map<String, String> token) {

        try {
            return new ResponseEntity<>(cinema.returnTicket(token.get("token")), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            ObjectMapper mapper = new ObjectMapper();
            return new ResponseEntity<>(mapper.createObjectNode().put("error", "Wrong token!"),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/stats")
    ResponseEntity<ObjectNode> getStats(@RequestParam(required = false) String password) {

        if (password != null && password.equals(PASSWORD))
            return new ResponseEntity<>(cinema.getStats(), HttpStatus.OK);

        ObjectMapper mapper = new ObjectMapper();
        return new ResponseEntity<>(mapper.createObjectNode().put("error", "The password is wrong!"),
                HttpStatus.UNAUTHORIZED);
    }
}
