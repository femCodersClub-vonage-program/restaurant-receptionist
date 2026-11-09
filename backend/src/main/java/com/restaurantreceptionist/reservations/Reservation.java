package com.restaurantreceptionist.reservations;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "customer_first_name", nullable = false)
	private String customerFirstName;

	@Column(name = "customer_last_name", nullable = false)
	private String customerLastName;

	@Column(name = "phone_number", nullable = false)
	private String phoneNumber;

	@Column(name = "reservation_date", nullable = false)
	private LocalDate reservationDate;

	@Column(name = "arrival_time", nullable = false)
	private LocalTime arrivalTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private ReservationService service;

	@Column(name = "party_size", nullable = false)
	private short partySize;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ReservationStatus status = ReservationStatus.CONFIRMED;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Reservation() {
	}

	public Reservation(String customerFirstName, String customerLastName, String phoneNumber,
			LocalDate reservationDate, LocalTime arrivalTime, ReservationService service, short partySize) {
		this.customerFirstName = customerFirstName;
		this.customerLastName = customerLastName;
		this.phoneNumber = phoneNumber;
		this.reservationDate = reservationDate;
		this.arrivalTime = arrivalTime;
		this.service = service;
		this.partySize = partySize;
	}

	public UUID getId() {
		return id;
	}

	public String getCustomerFirstName() {
		return customerFirstName;
	}

	public String getCustomerLastName() {
		return customerLastName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public LocalDate getReservationDate() {
		return reservationDate;
	}

	public LocalTime getArrivalTime() {
		return arrivalTime;
	}

	public ReservationService getService() {
		return service;
	}

	public short getPartySize() {
		return partySize;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
