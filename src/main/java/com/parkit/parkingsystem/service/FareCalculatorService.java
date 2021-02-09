package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        long duration = outHour - inHour;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(Fare.CAR_RATE_PER_HOUR * duration/1000./60./60.);
                break;
            }
            case BIKE: {
                ticket.setPrice(Fare.BIKE_RATE_PER_HOUR * duration/1000./60./60);
                break;
            }
            case FREE_CAR: {
                ticket.setPrice(Fare.LESS_THAN_HALF_HOUR * duration/1000./60./60.);
                break;
            }
            case FREE_BIKE: {
                ticket.setPrice(Fare.LESS_THAN_HALF_HOUR * duration/1000./60./60.);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}