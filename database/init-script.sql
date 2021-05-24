CREATE DATABASE websphere_kinesis;
USE websphere_kinesis;

CREATE TABLE eventstore (id INT(8) primary key AUTO_INCREMENT not null,
                       message VARCHAR(128) not null,
                       message_direction ENUM ('INCOMING','OUTGOING') not null,
                       message_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null);