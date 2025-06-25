CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    license VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE trucks (
    id BIGSERIAL PRIMARY KEY,
    plate VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    manufacturing_year INTEGER,
    driver_id BIGINT REFERENCES drivers(id)
);

CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    destination VARCHAR(255) NOT NULL,
    delivery_date_time TIMESTAMP NOT NULL,
    cargo_type VARCHAR(50) NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    is_valuable BOOLEAN NOT NULL DEFAULT FALSE,
    has_insurance BOOLEAN NOT NULL DEFAULT FALSE,
    is_dangerous BOOLEAN NOT NULL DEFAULT FALSE,
    truck_id BIGINT REFERENCES trucks(id),
    driver_id BIGINT REFERENCES drivers(id)
); 