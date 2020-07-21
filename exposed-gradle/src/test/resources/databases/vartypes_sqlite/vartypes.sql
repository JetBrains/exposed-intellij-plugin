DROP TABLE IF EXISTS long_types;
DROP TABLE IF EXISTS char_types;
DROP TABLE IF EXISTS floating_point_types;
DROP TABLE IF EXISTS miscellanious_types;
DROP TABLE IF EXISTS decimal_types;
DROP TABLE IF EXISTS integer_types;

CREATE TABLE long_types(
l1 bigint, 
l2 int8
);
INSERT INTO long_types VALUES(462374682332,545794879594);
CREATE TABLE char_types(
ch1 character(19),
ch2 varchar(255),
ch3 varying character(255),
ch4 text 
);
INSERT INTO char_types VALUES('nineteen symbols!!!','a string of arbitrary length no more than 255','another string of arbitrary length','Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris aliquet convallis pretium. Nulla dictum sapien id massa viverra mollis. Curabitur congue bibendum eleifend. Proin placerat libero arcu, id pellentesque urna sagittis eu. Cras semper ipsum at tortor accumsan pretium. Sed nibh purus, volutpat ac egestas in, dictum at felis. Integer sodales tellus ipsum, quis egestas massa dapibus fermentum. Aenean ultrices sed lectus in viverra. In eget urna pellentesque, rutrum magna non, semper dui. Vivamus vel consectetur velit. Ut porttitor nibh sed tellus viverra, sed euismod ante vulputate. Etiam in mauris lacinia, maximus quam at, vulputate massa. Curabitur et convallis ligula, interdum pretium felis.');
CREATE TABLE floating_point_types(
f1 real,
f2 float, 
f3 double precision, 
f4 double
);
INSERT INTO floating_point_types VALUES(3.1415926535897931159,3.1415926530000000127,3.1415926535897931159,3.1415926535800000607);
CREATE TABLE miscellanious_types(
b1 boolean, 
b2 blob
);
INSERT INTO miscellanious_types VALUES(0,'abracadabra?');
CREATE TABLE decimal_types(
n1 numeric,
n2 decimal(10, 5)
);
INSERT INTO decimal_types VALUES(46234673,1234567890.1234500408);
CREATE TABLE integer_types(
i1 int, 
i2 integer,  
i3 mediumint,
i4 tinyint, 
i5 smallint,
i6 int2
);
INSERT INTO integer_types VALUES(2147483647,2147483647,2147483647,127,65535,65535);
