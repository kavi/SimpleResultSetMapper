drop table if exists orderline;
drop table if exists product;
drop table if exists customer;

create table customer (
  id bigint not null primary key auto_increment,
  name varchar(128) not null unique,
  password varchar(64) not null,
  joined TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  active boolean not null default true  
) ENGINE = INNODB;

create table product (
  id int not null primary key auto_increment,
  name varchar(32) not null unique
) ENGINE = INNODB;

create table orderline (
  id int not null primary key auto_increment,
  price float(10,2) not null,
  customer_id bigint not null,
  product_id int not null,
  foreign key (customer_id) references customer(id),
  foreign key (product_id) references product(id)
) ENGINE = INNODB;