-- Create airports table
CREATE TABLE airports (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(50) NOT NULL DEFAULT 'India',
    timezone VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on airport code for fast lookups
CREATE INDEX idx_airports_code ON airports(code);
CREATE INDEX idx_airports_city ON airports(city);
CREATE INDEX idx_airports_active ON airports(active);

-- Insert sample airports
INSERT INTO airports (code, name, city, timezone) VALUES
('DEL', 'Indira Gandhi International Airport', 'Delhi', 'Asia/Kolkata'),
('BOM', 'Chhatrapati Shivaji Maharaj International Airport', 'Mumbai', 'Asia/Kolkata'),
('BLR', 'Kempegowda International Airport', 'Bangalore', 'Asia/Kolkata'),
('MAA', 'Chennai International Airport', 'Chennai', 'Asia/Kolkata'),
('CCU', 'Netaji Subhas Chandra Bose International Airport', 'Kolkata', 'Asia/Kolkata'),
('HYD', 'Rajiv Gandhi International Airport', 'Hyderabad', 'Asia/Kolkata'),
('PNQ', 'Pune Airport', 'Pune', 'Asia/Kolkata'),
('AMD', 'Sardar Vallabhbhai Patel International Airport', 'Ahmedabad', 'Asia/Kolkata'),
('GOI', 'Goa International Airport', 'Goa', 'Asia/Kolkata'),
('COK', 'Cochin International Airport', 'Kochi', 'Asia/Kolkata');
