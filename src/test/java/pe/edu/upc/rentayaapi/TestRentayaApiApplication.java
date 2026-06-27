package pe.edu.upc.rentayaapi;

import org.springframework.boot.SpringApplication;

public class TestRentayaApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(RentayaApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
