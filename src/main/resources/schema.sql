DROP TABLE IF EXISTS users, items, bookings, requests, comments;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(400) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT constraint_user UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    description VARCHAR(200) NOT NULL,
    requestor_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_requests PRIMARY KEY (id),
    CONSTRAINT fk_requests_to_requestor_id FOREIGN KEY (requestor_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    is_available BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL,
    request_id BIGINT,
    CONSTRAINT pk_items PRIMARY KEY (id),
    CONSTRAINT fk_items_to_owner_id FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT fk_items_to_request_id FOREIGN KEY (request_id) REFERENCES requests(id)
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id BIGINT NOT NULL,
    booker_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT pk_bookings PRIMARY KEY (id),
    CONSTRAINT fk_bookings_to_item_id FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_bookings_to_broker_id FOREIGN KEY (booker_id) REFERENCES  users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    text VARCHAR(500) NOT NULL,
    item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_to_item_id FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_comments_to_author_id FOREIGN KEY (author_id) REFERENCES users(id)
);