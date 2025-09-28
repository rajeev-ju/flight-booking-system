-- Create routes table
CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,
    origin_airport_code VARCHAR(3) NOT NULL,
    destination_airport_code VARCHAR(3) NOT NULL,
    distance INTEGER NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_route_origin FOREIGN KEY (origin_airport_code) REFERENCES airports(code),
    CONSTRAINT fk_route_destination FOREIGN KEY (destination_airport_code) REFERENCES airports(code),
    
    -- Ensure origin != destination
    CONSTRAINT chk_different_airports CHECK (origin_airport_code != destination_airport_code),
    
    -- Unique constraint for route combination
    CONSTRAINT uk_route_combination UNIQUE (origin_airport_code, destination_airport_code)
);

-- Create indexes for fast route lookups
CREATE INDEX idx_routes_origin ON routes(origin_airport_code);
CREATE INDEX idx_routes_destination ON routes(destination_airport_code);
CREATE INDEX idx_routes_active ON routes(active);
CREATE INDEX idx_routes_origin_destination ON routes(origin_airport_code, destination_airport_code);

-- Insert sample routes (major city pairs)
INSERT INTO routes (origin_airport_code, destination_airport_code, distance, base_price) VALUES
-- Delhi routes
('DEL', 'BOM', 1150, 5500.00),
('DEL', 'BLR', 1740, 6200.00),
('DEL', 'MAA', 1760, 6300.00),
('DEL', 'CCU', 1320, 5800.00),
('DEL', 'HYD', 1270, 5700.00),

-- Mumbai routes
('BOM', 'DEL', 1150, 5500.00),
('BOM', 'BLR', 840, 4800.00),
('BOM', 'MAA', 1030, 5200.00),
('BOM', 'CCU', 1650, 6100.00),
('BOM', 'PNQ', 150, 2500.00),

-- Bangalore routes
('BLR', 'DEL', 1740, 6200.00),
('BLR', 'BOM', 840, 4800.00),
('BLR', 'MAA', 290, 3200.00),
('BLR', 'HYD', 500, 3800.00),
('BLR', 'COK', 460, 3600.00),

-- Chennai routes
('MAA', 'DEL', 1760, 6300.00),
('MAA', 'BOM', 1030, 5200.00),
('MAA', 'BLR', 290, 3200.00),
('MAA', 'CCU', 1370, 5900.00),
('MAA', 'HYD', 630, 4000.00),

-- Kolkata routes
('CCU', 'DEL', 1320, 5800.00),
('CCU', 'BOM', 1650, 6100.00),
('CCU', 'MAA', 1370, 5900.00),
('CCU', 'BLR', 1560, 6000.00),

-- Hyderabad routes
('HYD', 'DEL', 1270, 5700.00),
('HYD', 'BOM', 620, 4200.00),
('HYD', 'BLR', 500, 3800.00),
('HYD', 'MAA', 630, 4000.00);
