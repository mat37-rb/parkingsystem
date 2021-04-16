package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		dataBasePrepareService.clearDataBaseEntries();

	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() throws Exception {

		// GIVEN
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(inputReaderUtil.readSelection()).thenReturn(1);
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		// WHEN
		parkingService.processIncomingVehicle();

		// THEN
		Connection con = null;
		try {
			con = dataBaseTestConfig.getConnection();
			ResultSet rs = con.createStatement()
					.executeQuery("select id from ticket where vehicle_reg_number = 'ABCDEF' and out_time is null");

			assertTrue(rs.next());
		} finally {
			dataBaseTestConfig.closeConnection(con);
		}
	}

	@Test
	public void testParkingABike() throws Exception {

		// GIVEN
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(inputReaderUtil.readSelection()).thenReturn(2);
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		// WHEN
		parkingService.processIncomingVehicle();

		// THEN
		Connection con = null;
		try {
			con = dataBaseTestConfig.getConnection();
			ResultSet rs = con.createStatement()
					.executeQuery("select id from ticket where vehicle_reg_number = 'ABCDEF' and out_time is null");

			assertTrue(rs.next());
		} finally {
			dataBaseTestConfig.closeConnection(con);
		}
	}

	@Test
	public void testParkingLotExit() throws Exception {

		// GIVEN
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(inputReaderUtil.readSelection()).thenReturn(1);
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		// testParkingACar();
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		// WHEN
		parkingService.processIncomingVehicle();
		parkingService.processExitingVehicle();

		// THEN
		Connection con = null;
		try {
			con = dataBaseTestConfig.getConnection();
			ResultSet rs = con.createStatement()
					.executeQuery("select id from ticket where vehicle_reg_number = 'ABCDEF' and out_time is not null");
			assertTrue(rs.next());
		} finally {
			dataBaseTestConfig.closeConnection(con);
		}
	}

	@Test
	public void testRecurrentUser() throws Exception {

		// GIVEN
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(inputReaderUtil.readSelection()).thenReturn(1);
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		Connection con = null;
		try {
			con = dataBaseTestConfig.getConnection();
			con.createStatement().execute(
					"INSERT INTO ticket VALUES (42, 1,'ABCDEF', 20, '2021-03-11 13:45:01','2021-03-11 15:45:01' )");
		} finally {
			dataBaseTestConfig.closeConnection(con);
		}
		// WHEN
		boolean actualResult = ticketDAO.isRecurrentUser("ABCDEF");

		// THEN
		assertTrue(actualResult);
	}

	@Test
	public void testIsNotRecurrentUser() throws Exception {

		// GIVEN
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(inputReaderUtil.readSelection()).thenReturn(1);
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		// WHEN
		boolean actualResult = ticketDAO.isRecurrentUser("ABCDEF");

		// THEN
		assertFalse(actualResult);
	}

	@Test
	public void ShouldGetReturnProblemWithBD() {

		// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(1);
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = null;
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		// THEN // WHEN
		assertThrows(Exception.class, () -> parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
	}
}
