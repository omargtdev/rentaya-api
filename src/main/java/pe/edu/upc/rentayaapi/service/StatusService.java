package pe.edu.upc.rentayaapi.service;

import org.springframework.stereotype.Service;

@Service
public class StatusService {

    public String getStatusMessage() {
        return "Api is running...";
    }
}
