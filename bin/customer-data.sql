insert into customer (id, password, name, active) values (1, 'secret', 'John John', true);
insert into customer (id, password, name, active) values (2, 'love', 'Elton Lennon', true);
insert into customer (id, password, name, active) values (3, 'vlad3', 'Vladimir III', false);
insert into customer (id, password, name, active) values (4, 'clever', 'Karen', true);


insert into product (id, name) values (1, 'Cookies');
insert into product (id, name) values (2, 'Candles');
insert into product (id, name) values (3, 'Socks');
insert into product (id, name) values (4, 'A little hat');


insert into orderline (id, customer_id, product_id, price) values (1,1,1,100);
insert into orderline (id, customer_id, product_id, price) values (2,1,2,50);
insert into orderline (id, customer_id, product_id, price) values (3,1,3,99);

insert into orderline (id, customer_id, product_id, price) values (4,2,2,50);
insert into orderline (id, customer_id, product_id, price) values (5,2,4,1.5);

insert into orderline (id, customer_id, product_id, price) values (6,4,1,100);
insert into orderline (id, customer_id, product_id, price) values (7,4,2,50);
insert into orderline (id, customer_id, product_id, price) values (8,4,3,99);
insert into orderline (id, customer_id, product_id, price) values (9,4,4,1.5);
