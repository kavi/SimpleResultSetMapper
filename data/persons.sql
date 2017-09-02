DROP TABLE IF EXISTS CAR;
DROP TABLE IF EXISTS CAR_TYPE;
DROP TABLE IF EXISTS PERSONS;
DROP TABLE IF EXISTS ADDRESS;

CREATE TABLE ADDRESS (
  id bigint not null primary key auto_increment,
  street_name varchar(32),
  is_public int
);

CREATE TABLE PERSONS  (
  id int not null primary key auto_increment,
  name varchar(32),
  age int,
  smoker varchar(1),
  address_id bigint,
  foreign key (address_id) references address(id)
);

CREATE TABLE CAR_TYPE (
  id int not null primary key auto_increment,
  make varchar(32),
  model varchar(64)
);

CREATE TABLE CAR  (
  id int not null primary key auto_increment,
  registration varchar(7),
  person_id int not null,
  car_type_id int not null,
  foreign key (person_id) references persons(id),  
  foreign key (car_type_id) references car_type(id)  
);


