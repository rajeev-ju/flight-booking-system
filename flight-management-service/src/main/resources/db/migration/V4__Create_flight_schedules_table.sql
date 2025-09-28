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
    
    -- Foreign key constraints
    CONSTRAINT fk_schedule_flight FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE,
    
    -- Business constraints
    CONSTRAINT chk_schedule_available_seats_positive CHECK (available_seats >= 0),
    CONSTRAINT chk_schedule_price_positive CHECK (price > 0),
    CONSTRAINT chk_schedule_arrival_after_departure CHECK (arrival_date_time > departure_date_time),
    CONSTRAINT chk_schedule_status CHECK (status IN ('SCHEDULED', 'BOARDING', 'DEPARTED', 'ARRIVED', 'CANCELLED', 'DELAYED')),
    
    -- Unique constraint to prevent duplicate schedules
    CONSTRAINT uk_flight_schedule_date UNIQUE (flight_id, schedule_date)
);

-- Create indexes for fast schedule lookups
CREATE INDEX idx_flight_schedules_flight_id ON flight_schedules(flight_id);
CREATE INDEX idx_flight_schedules_date ON flight_schedules(schedule_date);
CREATE INDEX idx_flight_schedules_departure ON flight_schedules(departure_date_time);
CREATE INDEX idx_flight_schedules_status ON flight_schedules(status);
CREATE INDEX idx_flight_schedules_available_seats ON flight_schedules(available_seats);

-- Create composite index for common search patterns
CREATE INDEX idx_flight_schedules_search ON flight_schedules(schedule_date, status, available_seats);

-- Function to automatically generate flight schedules for the next 30 days
-- This will be called by the application, but we can create a sample here

-- Insert sample flight schedules for the next 7 days for demonstration
DO $$
DECLARE
    flight_record RECORD;
    schedule_date DATE;
    departure_datetime TIMESTAMP;
    arrival_datetime TIMESTAMP;
    day_offset INTEGER;
BEGIN
    -- Generate schedules for next 7 days
    FOR day_offset IN 0..6 LOOP
        schedule_date := CURRENT_DATE + day_offset;
        
        -- For each active flight, create a schedule
        FOR flight_record IN 
            SELECT id, departure_time, arrival_time, total_seats, base_price 
            FROM flights 
            WHERE active = true 
        LOOP
            -- Calculate departure and arrival datetime
            departure_datetime := schedule_date + flight_record.departure_time;
            arrival_datetime := schedule_date + flight_record.arrival_time;
            
            -- Handle overnight flights (arrival next day)
            IF flight_record.arrival_time < flight_record.departure_time THEN
                arrival_datetime := arrival_datetime + INTERVAL '1 day';
            END IF;
            
            -- Insert schedule with full seats available initially
            INSERT INTO flight_schedules (
                flight_id, 
                schedule_date, 
                departure_date_time, 
                arrival_date_time, 
                available_seats, 
                price, 
                status
            ) VALUES (
                flight_record.id,
                departure_datetime,
                departure_datetime,
                arrival_datetime,
                flight_record.total_seats,
                flight_record.base_price,
                'SCHEDULED'
            );
        END LOOP;
    END LOOP;
END $$;
