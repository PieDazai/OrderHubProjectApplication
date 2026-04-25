create table orders(
    id BIGSERIAL primary key,
    status VARCHAR(50) NOT NULL,
    creat_at TIMESTAMP WITH TIME ZONE NOT NULL

    CONSTRAINT check_status CHECK (status IN ('CREATED', 'PAID', 'CANCELLED'))
);

CREATE TABLE order_items (
    id BIGSERIAL primary key,
    order_id BIGINT,
    product_id bigint not null,
    product_name varchar(255) not null,
    quantity integer not null check ( quantity > 0 ),
    price decimal(10, 2) not null check ( price >= 0 ),

    constraint fk_order_items_order
                         foreign key (order_id)
                         references orders(id)
                         on delete cascade

);

create index idx_order_items_id on order_items(id);
create index idx_order_status on orders(status);