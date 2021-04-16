package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	private TicketDAO ticketDAO;

	public FareCalculatorService(TicketDAO ticketDAO) {
		this.ticketDAO = ticketDAO;
	}

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		long inHour = ticket.getInTime().getTime();
		long outHour = ticket.getOutTime().getTime();

		long duration = outHour - inHour;
		if (duration <= 30 * 1000 * 60) {
			ticket.setPrice(0);
			return;
		}

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR:
			ticket.setPrice(Fare.CAR_RATE_PER_HOUR * duration / 1000. / 60. / 60.);
			break;

		case BIKE:
			ticket.setPrice(Fare.BIKE_RATE_PER_HOUR * duration / 1000. / 60. / 60);
			break;

		default:
			throw new IllegalArgumentException("Unkown Parking Type");
		}

		if (ticketDAO.isRecurrentUser(ticket.getVehicleRegNumber())) {
			ticket.setPrice(ticket.getPrice() * 0.95);
		}
	}
}