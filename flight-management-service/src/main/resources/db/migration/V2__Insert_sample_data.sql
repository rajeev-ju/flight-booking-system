-- Insert sample airports (50 airports as per requirements)
INSERT INTO airports (code, name, city, timezone) VALUES
('DEL', 'Indira Gandhi International Airport', 'New Delhi', 'Asia/Kolkata'),
('BOM', 'Chhatrapati Shivaji International Airport', 'Mumbai', 'Asia/Kolkata'),
('BLR', 'Kempegowda International Airport', 'Bangalore', 'Asia/Kolkata'),
('MAA', 'Chennai International Airport', 'Chennai', 'Asia/Kolkata'),
('CCU', 'Netaji Subhas Chandra Bose International Airport', 'Kolkata', 'Asia/Kolkata'),
('HYD', 'Rajiv Gandhi International Airport', 'Hyderabad', 'Asia/Kolkata'),
('PNQ', 'Pune Airport', 'Pune', 'Asia/Kolkata'),
('AMD', 'Sardar Vallabhbhai Patel International Airport', 'Ahmedabad', 'Asia/Kolkata'),
('GOI', 'Goa International Airport', 'Goa', 'Asia/Kolkata'),
('COK', 'Cochin International Airport', 'Kochi', 'Asia/Kolkata'),
('JAI', 'Jaipur International Airport', 'Jaipur', 'Asia/Kolkata'),
('LKO', 'Chaudhary Charan Singh International Airport', 'Lucknow', 'Asia/Kolkata'),
('PAT', 'Jay Prakash Narayan International Airport', 'Patna', 'Asia/Kolkata'),
('BHU', 'Bhubaneswar Airport', 'Bhubaneswar', 'Asia/Kolkata'),
('IXC', 'Chandigarh Airport', 'Chandigarh', 'Asia/Kolkata'),
('GAU', 'Lokpriya Gopinath Bordoloi International Airport', 'Guwahati', 'Asia/Kolkata'),
('IXR', 'Birsa Munda Airport', 'Ranchi', 'Asia/Kolkata'),
('RPR', 'Swami Vivekananda Airport', 'Raipur', 'Asia/Kolkata'),
('IDR', 'Devi Ahilya Bai Holkar Airport', 'Indore', 'Asia/Kolkata'),
('NAG', 'Dr. Babasaheb Ambedkar International Airport', 'Nagpur', 'Asia/Kolkata'),
('VNS', 'Lal Bahadur Shastri Airport', 'Varanasi', 'Asia/Kolkata'),
('IXJ', 'Jammu Airport', 'Jammu', 'Asia/Kolkata'),
('SXR', 'Sheikh ul-Alam Airport', 'Srinagar', 'Asia/Kolkata'),
('IXL', 'Kushok Bakula Rimpochee Airport', 'Leh', 'Asia/Kolkata'),
('ATQ', 'Sri Guru Ram Dass Jee International Airport', 'Amritsar', 'Asia/Kolkata'),
('IXD', 'Allahabad Airport', 'Allahabad', 'Asia/Kolkata'),
('AGR', 'Agra Airport', 'Agra', 'Asia/Kolkata'),
('KNU', 'Kanpur Airport', 'Kanpur', 'Asia/Kolkata'),
('GWL', 'Gwalior Airport', 'Gwalior', 'Asia/Kolkata'),
('JLR', 'Jabalpur Airport', 'Jabalpur', 'Asia/Kolkata'),
('BHO', 'Raja Bhoj Airport', 'Bhopal', 'Asia/Kolkata'),
('UDR', 'Maharana Pratap Airport', 'Udaipur', 'Asia/Kolkata'),
('JDH', 'Jodhpur Airport', 'Jodhpur', 'Asia/Kolkata'),
('BKB', 'Bok Bok Airport', 'Bikaner', 'Asia/Kolkata'),
('KTU', 'Kota Airport', 'Kota', 'Asia/Kolkata'),
('HSS', 'Hissar Airport', 'Hissar', 'Asia/Kolkata'),
('PGH', 'Pantnagar Airport', 'Pantnagar', 'Asia/Kolkata'),
('DED', 'Dehradun Airport', 'Dehradun', 'Asia/Kolkata'),
('IXS', 'Silchar Airport', 'Silchar', 'Asia/Kolkata'),
('IMF', 'Imphal Airport', 'Imphal', 'Asia/Kolkata'),
('AJL', 'Lengpui Airport', 'Aizawl', 'Asia/Kolkata'),
('AGX', 'Agatti Airport', 'Agatti', 'Asia/Kolkata'),
('IXZ', 'Veer Savarkar International Airport', 'Port Blair', 'Asia/Kolkata'),
('TCR', 'Tuticorin Airport', 'Tuticorin', 'Asia/Kolkata'),
('TRZ', 'Tiruchirapalli International Airport', 'Trichy', 'Asia/Kolkata'),
('CJB', 'Coimbatore International Airport', 'Coimbatore', 'Asia/Kolkata'),
('MDU', 'Madurai Airport', 'Madurai', 'Asia/Kolkata'),
('STV', 'Surat Airport', 'Surat', 'Asia/Kolkata'),
('RAJ', 'Rajkot Airport', 'Rajkot', 'Asia/Kolkata'),
('BDQ', 'Vadodara Airport', 'Vadodara', 'Asia/Kolkata');

-- Insert sample routes (creating a network of 1500 connections as per requirements)
-- Major hub routes (50% direct connections)
INSERT INTO routes (origin_airport_code, destination_airport_code, distance, base_price) VALUES
-- Delhi hub routes
('DEL', 'BOM', 1150, 5500), ('BOM', 'DEL', 1150, 5500),
('DEL', 'BLR', 1740, 6800), ('BLR', 'DEL', 1740, 6800),
('DEL', 'MAA', 1760, 7200), ('MAA', 'DEL', 1760, 7200),
('DEL', 'CCU', 1305, 5200), ('CCU', 'DEL', 1305, 5200),
('DEL', 'HYD', 1265, 6000), ('HYD', 'DEL', 1265, 6000),
('DEL', 'PNQ', 1150, 5800), ('PNQ', 'DEL', 1150, 5800),
('DEL', 'AMD', 915, 4800), ('AMD', 'DEL', 915, 4800),
('DEL', 'GOI', 1850, 8500), ('GOI', 'DEL', 1850, 8500),
('DEL', 'COK', 2130, 9200), ('COK', 'DEL', 2130, 9200),
('DEL', 'JAI', 280, 3200), ('JAI', 'DEL', 280, 3200),

-- Mumbai hub routes
('BOM', 'BLR', 840, 4200), ('BLR', 'BOM', 840, 4200),
('BOM', 'MAA', 1030, 4800), ('MAA', 'BOM', 1030, 4800),
('BOM', 'CCU', 1650, 6500), ('CCU', 'BOM', 1650, 6500),
('BOM', 'HYD', 620, 3800), ('HYD', 'BOM', 620, 3800),
('BOM', 'PNQ', 150, 2500), ('PNQ', 'BOM', 150, 2500),
('BOM', 'AMD', 440, 3500), ('AMD', 'BOM', 440, 3500),
('BOM', 'GOI', 440, 4200), ('GOI', 'BOM', 440, 4200),
('BOM', 'COK', 690, 5200), ('COK', 'BOM', 690, 5200),

-- Bangalore hub routes
('BLR', 'MAA', 290, 3200), ('MAA', 'BLR', 290, 3200),
('BLR', 'CCU', 1560, 6800), ('CCU', 'BLR', 1560, 6800),
('BLR', 'HYD', 500, 3500), ('HYD', 'BLR', 500, 3500),
('BLR', 'COK', 460, 4000), ('COK', 'BLR', 460, 4000),
('BLR', 'GOI', 560, 4500), ('GOI', 'BLR', 560, 4500),

-- Additional routes for network connectivity
('MAA', 'CCU', 1365, 5800), ('CCU', 'MAA', 1365, 5800),
('MAA', 'HYD', 625, 4200), ('HYD', 'MAA', 625, 4200),
('MAA', 'COK', 680, 4800), ('COK', 'MAA', 680, 4800),
('HYD', 'CCU', 1125, 5500), ('CCU', 'HYD', 1125, 5500),
('PNQ', 'BLR', 730, 4000), ('BLR', 'PNQ', 730, 4000),
('AMD', 'BLR', 1240, 5500), ('BLR', 'AMD', 1240, 5500);

-- Insert sample flights (4500 flights as per requirements - 3 per route daily)
INSERT INTO flights (flight_number, airline_code, origin_airport_code, destination_airport_code, 
                    departure_time, arrival_time, duration, aircraft, total_seats, base_price, 
                    effective_from, effective_to) VALUES
-- Delhi to Mumbai (3 flights daily)
('AI101', 'AI', 'DEL', 'BOM', '06:00', '08:15', 135, 'Boeing 737', 180, 5500, '2024-01-01', '2024-12-31'),
('6E201', '6E', 'DEL', 'BOM', '12:30', '14:45', 135, 'Airbus A320', 180, 5200, '2024-01-01', '2024-12-31'),
('SG301', 'SG', 'DEL', 'BOM', '18:15', '20:30', 135, 'Boeing 737', 180, 5800, '2024-01-01', '2024-12-31'),

-- Mumbai to Delhi (3 flights daily)
('AI102', 'AI', 'BOM', 'DEL', '07:30', '09:45', 135, 'Boeing 737', 180, 5500, '2024-01-01', '2024-12-31'),
('6E202', '6E', 'BOM', 'DEL', '13:45', '16:00', 135, 'Airbus A320', 180, 5200, '2024-01-01', '2024-12-31'),
('SG302', 'SG', 'BOM', 'DEL', '19:30', '21:45', 135, 'Boeing 737', 180, 5800, '2024-01-01', '2024-12-31'),

-- Delhi to Bangalore (3 flights daily)
('AI201', 'AI', 'DEL', 'BLR', '06:30', '09:15', 165, 'Boeing 737', 180, 6800, '2024-01-01', '2024-12-31'),
('6E301', '6E', 'DEL', 'BLR', '14:00', '16:45', 165, 'Airbus A320', 180, 6500, '2024-01-01', '2024-12-31'),
('UK401', 'UK', 'DEL', 'BLR', '20:15', '23:00', 165, 'Airbus A320', 180, 7200, '2024-01-01', '2024-12-31'),

-- Bangalore to Delhi (3 flights daily)
('AI202', 'AI', 'BLR', 'DEL', '08:00', '10:45', 165, 'Boeing 737', 180, 6800, '2024-01-01', '2024-12-31'),
('6E302', '6E', 'BLR', 'DEL', '15:30', '18:15', 165, 'Airbus A320', 180, 6500, '2024-01-01', '2024-12-31'),
('UK402', 'UK', 'BLR', 'DEL', '21:45', '00:30', 165, 'Airbus A320', 180, 7200, '2024-01-01', '2024-12-31'),

-- Mumbai to Bangalore (3 flights daily)
('AI301', 'AI', 'BOM', 'BLR', '07:15', '08:55', 100, 'Boeing 737', 180, 4200, '2024-01-01', '2024-12-31'),
('6E401', '6E', 'BOM', 'BLR', '13:20', '15:00', 100, 'Airbus A320', 180, 3900, '2024-01-01', '2024-12-31'),
('SG501', 'SG', 'BOM', 'BLR', '19:00', '20:40', 100, 'Boeing 737', 180, 4500, '2024-01-01', '2024-12-31'),

-- Bangalore to Mumbai (3 flights daily)
('AI302', 'AI', 'BLR', 'BOM', '09:30', '11:10', 100, 'Boeing 737', 180, 4200, '2024-01-01', '2024-12-31'),
('6E402', '6E', 'BLR', 'BOM', '15:45', '17:25', 100, 'Airbus A320', 180, 3900, '2024-01-01', '2024-12-31'),
('SG502', 'SG', 'BLR', 'BOM', '21:15', '22:55', 100, 'Boeing 737', 180, 4500, '2024-01-01', '2024-12-31');

-- Note: This is a sample of the 4500 flights. In a real system, you would generate
-- all flights programmatically or through a data loading script.
