# Simplified Entity Relationship Diagram

## Clean ASCII Version for Documentation

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│  AIRLINES   │         │   AIRPORTS  │         │   ROUTES    │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ id (PK)     │         │ id (PK)     │         │ id (PK)     │
│ code (UK)   │◄────────│ code (UK)   │────────►│ origin_id   │
│ name        │         │ city        │         │ dest_id     │
└─────────────┘         └─────────────┘         │ airline_id  │
                                                 └─────────────┘
                                                        │
                                                        ▼
                                                 ┌─────────────┐
                                                 │   FLIGHTS   │
                                                 ├─────────────┤
                                                 │ id (PK)     │
                                                 │ number (UK) │
                                                 │ route_id    │
                                                 │ total_seats │
                                                 └─────────────┘
                                                        │
                                                        ▼
                                                 ┌─────────────┐
                                                 │  SCHEDULES  │
                                                 ├─────────────┤
                                                 │ id (PK)     │
                                                 │ flight_num  │
                                                 │ departure   │
                                                 │ avail_seats │
                                                 └─────────────┘
                                                        │
                                                        ▼
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│ SEAT_BLOCKS │◄────────│  BOOKINGS   │────────►│ PASSENGERS  │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ id (PK)     │         │ id (PK)     │         │ id (PK)     │
│ schedule_id │         │ pnr (UK)    │         │ booking_id  │
│ seats       │         │ schedule_id │         │ name        │
│ expiry (TTL)│         │ amount      │         │ id_number   │
└─────────────┘         └─────────────┘         └─────────────┘
                               │
                               ▼
                        ┌─────────────┐
                        │  PAYMENTS   │
                        ├─────────────┤
                        │ id (PK)     │
                        │ booking_id  │
                        │ amount      │
                        │ status      │
                        └─────────────┘
```

## Core Relationships

### Flight Path
```
AIRLINES ──operates──► ROUTES ◄──connects── AIRPORTS
                         │
                         ▼
                      FLIGHTS
                         │
                         ▼
                  FLIGHT_SCHEDULES
```

### Booking Flow
```
FLIGHT_SCHEDULES ──has──► BOOKINGS ──contains──► PASSENGERS
        │                     │
        ▼                     ▼
   SEAT_BLOCKS           PAYMENTS
   (Redis TTL)
```

## Simplified Table Structure

### Core Tables
| Table | Purpose | Key Fields |
|-------|---------|------------|
| **airlines** | Airline companies | code (AI), name |
| **airports** | Airport locations | code (DEL), city |
| **routes** | Flight paths | origin, destination, airline |
| **flights** | Flight definitions | flight_number (AI101), route |
| **flight_schedules** | Daily instances | departure_time, available_seats |

### Transaction Tables
| Table | Purpose | Key Fields |
|-------|---------|------------|
| **bookings** | Customer bookings | pnr (6-char), schedule_id |
| **passengers** | Traveler details | booking_id, name, id_number |
| **payments** | Payment records | booking_id, amount, status |
| **seat_blocks** | Temp reservations | schedule_id, expiry (Redis) |

## Key Design Principles

1. **UUID Primary Keys**: Distributed system friendly
2. **Unique Constraints**: 
   - Airlines/Airports: 3-letter codes
   - Flights: flight_number
   - Bookings: 6-character PNR
3. **Performance Indexes**: On foreign keys and search fields
4. **Partitioning**: Monthly for schedules and bookings
5. **Redis Integration**: For temporary seat blocks with TTL
