-- Create flights table
CREATE TABLE flights (
    id BIGSERIAL PRIMARY KEY,
    flight_number VARCHAR(10) NOT NULL UNIQUE,
    airline_code VARCHAR(3) NOT NULL,
    origin_airport_code VARCHAR(3) NOT NULL,
    destination_airport_code VARCHAR(3) NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    duration INTEGER NOT NULL, -- Duration in minutes
    aircraft VARCHAR(50) NOT NULL,
    total_seats INTEGER NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_flight_origin FOREIGN KEY (origin_airport_code) REFERENCES airports(code),
    CONSTRAINT fk_flight_destination FOREIGN KEY (destination_airport_code) REFERENCES airports(code),
    
    -- Business constraints
    CONSTRAINT chk_flight_different_airports CHECK (origin_airport_code != destination_airport_code),
    CONSTRAINT chk_flight_duration_positive CHECK (duration > 0),
    CONSTRAINT chk_flight_seats_positive CHECK (total_seats > 0),
    CONSTRAINT chk_flight_price_positive CHECK (base_price > 0),
    CONSTRAINT chk_flight_effective_dates CHECK (effective_to > effective_from)
);

-- Create indexes for fast flight lookups
CREATE INDEX idx_flights_flight_number ON flights(flight_number);
CREATE INDEX idx_flights_airline ON flights(airline_code);
CREATE INDEX idx_flights_route ON flights(origin_airport_code, destination_airport_code);
CREATE INDEX idx_flights_departure_time ON flights(departure_time);
CREATE INDEX idx_flights_active ON flights(active);
CREATE INDEX idx_flights_effective_dates ON flights(effective_from, effective_to);

-- Insert sample flights
INSERT INTO flights (flight_number, airline_code, origin_airport_code, destination_airport_code, 
                    departure_time, arrival_time, duration, aircraft, total_seats, base_price, 
                    effective_from, effective_to) VALUES

-- Air India flights
('AI101', 'AI', 'DEL', 'BOM', '06:00', '08:15', 135, 'Boeing 737', 180, 5500.00, '2024-01-01', '2024-12-31'),
('AI102', 'AI', 'BOM', 'DEL', '09:30', '11:45', 135, 'Boeing 737', 180, 5500.00, '2024-01-01', '2024-12-31'),
('AI201', 'AI', 'DEL', 'BLR', '10:00', '12:30', 150, 'Airbus A320', 160, 6200.00, '2024-01-01', '2024-12-31'),
('AI202', 'AI', 'BLR', 'DEL', '14:00', '16:30', 150, 'Airbus A320', 160, 6200.00, '2024-01-01', '2024-12-31'),

-- IndiGo flights
('6E301', '6E', 'DEL', 'BOM', '12:00', '14:15', 135, 'Airbus A320', 180, 5200.00, '2024-01-01', '2024-12-31'),
('6E302', '6E', 'BOM', 'DEL', '15:30', '17:45', 135, 'Airbus A320', 180, 5200.00, '2024-01-01', '2024-12-31'),
('6E401', '6E', 'BOM', 'BLR', '08:00', '09:30', 90, 'Airbus A320', 180, 4800.00, '2024-01-01', '2024-12-31'),
('6E402', '6E', 'BLR', 'BOM', '11:00', '12:30', 90, 'Airbus A320', 180, 4800.00, '2024-01-01', '2024-12-31'),

-- SpiceJet flights
('SG501', 'SG', 'DEL', 'MAA', '07:00', '09:45', 165, 'Boeing 737', 189, 6300.00, '2024-01-01', '2024-12-31'),
('SG502', 'SG', 'MAA', 'DEL', '13:00', '15:45', 165, 'Boeing 737', 189, 6300.00, '2024-01-01', '2024-12-31'),
('SG601', 'SG', 'BLR', 'MAA', '16:00', '17:00', 60, 'Boeing 737', 189, 3200.00, '2024-01-01', '2024-12-31'),
('SG602', 'SG', 'MAA', 'BLR', '18:30', '19:30', 60, 'Boeing 737', 189, 3200.00, '2024-01-01', '2024-12-31');
