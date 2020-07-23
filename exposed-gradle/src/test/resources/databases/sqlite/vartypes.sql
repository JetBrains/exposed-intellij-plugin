DROP TABLE IF EXISTS long_types;
DROP TABLE IF EXISTS char_types;
DROP TABLE IF EXISTS floating_point_types;
DROP TABLE IF EXISTS miscellanious_types;
DROP TABLE IF EXISTS decimal_types;
DROP TABLE IF EXISTS integer_types;
DROP TABLE IF EXISTS datetime_types;

CREATE TABLE long_types(
l1 bigint NOT NULL,
l2 int8 NOT NULL
);
INSERT INTO long_types VALUES(462374682332,545794879594);
CREATE TABLE char_types(
ch1 character(19) NOT NULL,
ch2 varchar(255) NOT NULL,
ch3 varying character(255) NOT NULL,
ch4 text NOT NULL
);
INSERT INTO char_types VALUES('nineteen symbols!!!','a string of arbitrary length no more than 255','another string of arbitrary length','Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris aliquet convallis pretium. Nulla dictum sapien id massa viverra mollis. Curabitur congue bibendum eleifend. Proin placerat libero arcu, id pellentesque urna sagittis eu. Cras semper ipsum at tortor accumsan pretium. Sed nibh purus, volutpat ac egestas in, dictum at felis. Integer sodales tellus ipsum, quis egestas massa dapibus fermentum. Aenean ultrices sed lectus in viverra. In eget urna pellentesque, rutrum magna non, semper dui. Vivamus vel consectetur velit. Ut porttitor nibh sed tellus viverra, sed euismod ante vulputate. Etiam in mauris lacinia, maximus quam at, vulputate massa. Curabitur et convallis ligula, interdum pretium felis.');
CREATE TABLE floating_point_types(
f1 real NOT NULL,
f2 float NOT NULL,
f3 double precision NOT NULL,
f4 double NOT NULL
);
INSERT INTO floating_point_types VALUES(3.1415926535897931159,3.1415926530000000127,3.1415926535897931159,3.1415926535800000607);
CREATE TABLE miscellanious_types(
b1 boolean NOT NULL,
b2 blob NOT NULL
);
INSERT INTO miscellanious_types VALUES(0,'abracadabra?');
CREATE TABLE decimal_types(
n1 numeric NOT NULL,
n2 decimal(10, 5) NOT NULL
);
INSERT INTO decimal_types VALUES(46234673,1234567890.1234500408);
CREATE TABLE integer_types(
i1 int NOT NULL,
i2 integer NOT NULL,
i3 mediumint NOT NULL,
i4 tinyint NOT NULL,
i5 smallint NOT NULL,
i6 int2 NOT NULL
);
INSERT INTO integer_types VALUES(2147483647,2147483647,2147483647,127,65535,65535);

CREATE TABLE datetime_types(
d1 datetime NOT NULL,
d2 date NOT NULL
);