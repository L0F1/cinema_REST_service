package cinema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class Cinema {
    private final int totalRows, totalColumns;
    private final boolean[][] availableSeats;
    @JsonIgnore
    private final Map<String, Ticket> tickets;
    @JsonIgnore
    private int currentIncome;

    public Cinema() {
        totalRows = 9;
        totalColumns = 9;
        availableSeats = new boolean[totalRows][totalColumns];
        tickets = new HashMap<>();
        currentIncome = 0;

        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalColumns; j++) {
                availableSeats[i][j] = true;
            }
        }
    }

    public ObjectNode purchaseSeat(int row, int column)
            throws UnavailableSeatException, IllegalArgumentException {

        ObjectMapper mapper = new ObjectMapper();

        try {
            if (availableSeats[row-1][column-1]) {

                String token = UUID.randomUUID().toString();
                int price = row <= 4 ? 10 : 8;
                Ticket ticket = new Ticket(row, column, price);
                availableSeats[row-1][column-1] = false;
                currentIncome += price;
                tickets.put(token, ticket);

                ObjectNode objNode = mapper.createObjectNode();
                return objNode.put("token", token)
                        .putPOJO("ticket", ticket);
            }
            throw new UnavailableSeatException();
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }
    }

    public ObjectNode returnTicket(String token) throws IllegalArgumentException {

        ObjectMapper mapper = new ObjectMapper();
        Ticket ticket = tickets.get(token);

        if (ticket != null){

            availableSeats[ticket.getRow()-1][ticket.getColumn()-1] = true;
            currentIncome -= ticket.getPrice();
            tickets.remove(token);

            return mapper.createObjectNode().putPOJO("returned_ticket", ticket);
        }
        throw new IllegalArgumentException();
    }

    @JsonIgnore
    public ObjectNode getStats() {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        return node.put("current_income", currentIncome)
                .put("number_of_available_seats", totalRows * totalColumns - tickets.size())
                .put("number_of_purchased_tickets", tickets.size());
    }

    public List<Map<String,Integer>> getAvailableSeats() {
        List<Map<String,Integer>> seats = new ArrayList<>();

        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalColumns; j++) {
                if (availableSeats[i][j]) {
                    int price = i <= 4 ? 10 : 8;
                    seats.add(Map.of("row", i+1,
                                     "column", j+1,
                                     "price", price));
                }
            }
        }

        return seats;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getTotalColumns() {
        return totalColumns;
    }

    private class Ticket {
        int row, column, price;

        public Ticket(int row, int column, int price) {
            this.row = row;
            this.column = column;
            this.price = price;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public int getPrice() {
            return price;
        }
    }
}
