-- Create airports table
CREATE TABLE airports (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    timezone VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create routes table
CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,
    origin_airport_code VARCHAR(3) NOT NULL,
    destination_airport_code VARCHAR(3) NOT NULL,
    distance INTEGER NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(origin_airport_code, destination_airport_code),
    FOREIGN KEY (origin_airport_code) REFERENCES airports(code),
    FOREIGN KEY (destination_airport_code) REFERENCES airports(code)
);

-- Create flights table
CREATE TABLE flights (
    id BIGSERIAL PRIMARY KEY,
    flight_number VARCHAR(10) NOT NULL,
    airline_code VARCHAR(3) NOT NULL,
    origin_airport_code VARCHAR(3) NOT NULL,
    destination_airport_code VARCHAR(3) NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    duration INTEGER NOT NULL, -- in minutes
    aircraft VARCHAR(50) NOT NULL,
    total_seats INTEGER NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(flight_number, effective_from),
    FOREIGN KEY (origin_airport_code) REFERENCES airports(code),
    FOREIGN KEY (destination_airport_code) REFERENCES airports(code)
);

-- Create flight_schedules table
CREATE TABLE flight_schedules (
    id BIGSERIAL PRIMARY KEY,
    flight_id BIGINT NOT NULL,
    schedule_date TIMESTAMP NOT NULL,
    departure_date_time TIMESTAMP NOT NULL,
    arrival_date_time TIMESTAMP NOT NULL,
    available_seats INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flight_id) REFERENCES flights(id),
    UNIQUE(flight_id, schedule_date)
);

-- Create indexes for performance
CREATE INDEX idx_flights_route ON flights(origin_airport_code, destination_airport_code);
CREATE INDEX idx_flights_airline ON flights(airline_code);
CREATE INDEX idx_flights_active ON flights(active);

CREATE INDEX idx_schedules_date ON flight_schedules(schedule_date);
CREATE INDEX idx_schedules_flight_date ON flight_schedules(flight_id, schedule_date);
CREATE INDEX idx_schedules_status ON flight_schedules(status);
CREATE INDEX idx_schedules_available_seats ON flight_schedules(available_seats);

CREATE INDEX idx_routes_origin ON routes(origin_airport_code);
CREATE INDEX idx_routes_destination ON routes(destination_airport_code);
CREATE INDEX idx_routes_active ON routes(active);

CREATE INDEX idx_airports_code ON airports(code);
CREATE INDEX idx_airports_active ON airports(active);
