DROP TABLE IF EXISTS "PUBLIC"."INTEGER_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."BOOLEAN_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."SMALL_INT_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."LONG_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."DECIMAL_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."DOUBLE_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."CHAR_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."VARCHAR_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."TEXT_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."BINARY_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."FLOAT_TYPES";
DROP TABLE IF EXISTS "PUBLIC"."MISC_TYPES";


CREATE CACHED TABLE "PUBLIC"."INTEGER_TYPES"(
    "I1" INT,
    "I2" INTEGER,
    "I3" MEDIUMINT,
    "I4" INT4,
    "I5" SIGNED
);            
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.INTEGER_TYPES;            
INSERT INTO "PUBLIC"."INTEGER_TYPES" VALUES
(-2147483648, 2147483647, -2147483648, 2147483647, -2147483648);   
CREATE CACHED TABLE "PUBLIC"."BOOLEAN_TYPES"(
    "B1" BOOLEAN,
    "B2" BIT,
    "B3" BOOL
); 
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.BOOLEAN_TYPES;            
INSERT INTO "PUBLIC"."BOOLEAN_TYPES" VALUES
(TRUE, FALSE, TRUE);               
CREATE CACHED TABLE "PUBLIC"."SMALL_INT_TYPES"(
    "T1" TINYINT,
    "S1" SMALLINT,
    "S2" INT2,
    "S3" YEAR
);           
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.SMALL_INT_TYPES;          
INSERT INTO "PUBLIC"."SMALL_INT_TYPES" VALUES
(127, -32768, 32767, 2020);      
CREATE CACHED TABLE "PUBLIC"."LONG_TYPES"(
    "L1" BIGINT,
    "L2" INT8,
    "L3" IDENTITY
);
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.LONG_TYPES;               
INSERT INTO "PUBLIC"."LONG_TYPES" VALUES
(-9223372036854775808, 9223372036854775807, 0);       
CREATE CACHED TABLE "PUBLIC"."DECIMAL_TYPES"(
    "D1" DECIMAL(10),
    "D2" DECIMAL(10, 5),
    "D3" DEC(5),
    "D4" DEC(15, 10),
    "D5" NUMBER(3),
    "D6" NUMBER(3, 2),
    "D7" NUMERIC(4),
    "D8" NUMERIC(4, 2)
);  
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.DECIMAL_TYPES;            
INSERT INTO "PUBLIC"."DECIMAL_TYPES" VALUES
(1234567890, 12345.67891, 12345, 12345.6789012345, 123, 1.23, 1234, 12.30);        
CREATE CACHED TABLE "PUBLIC"."DOUBLE_TYPES"(
    "D1" DOUBLE,
    "D2" DOUBLE PRECISION,
    "D3" FLOAT,
    "D4" FLOAT(27),
    "D5" FLOAT8
);
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.DOUBLE_TYPES;             
INSERT INTO "PUBLIC"."DOUBLE_TYPES" VALUES
(3.46, 3.141592653589793, 3.141592653589793, 3.141592653589793, 3.141592653589793); 
CREATE CACHED TABLE "PUBLIC"."CHAR_TYPES"(
    "C1" CHAR,
    "C2" CHAR(5),
    "C3" CHARACTER(5),
    "C4" NCHAR(5)
);        
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.CHAR_TYPES;               
INSERT INTO "PUBLIC"."CHAR_TYPES" VALUES
('text longer than five characters', 'five', 'chara', 'cters');       
CREATE CACHED TABLE "PUBLIC"."VARCHAR_TYPES"(
    "C1" VARCHAR,
    "C2" VARCHAR(5),
    "C3" CHARACTER VARYING(5),
    "C4" LONGVARCHAR,
    "C5" VARCHAR2(5),
    "C6" NVARCHAR,
    "C7" NVARCHAR2(5),
    "C8" VARCHAR_CASESENSITIVE(5),
    "C9" VARCHAR_IGNORECASE(5)
); 
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.VARCHAR_TYPES;            
INSERT INTO "PUBLIC"."VARCHAR_TYPES" VALUES
('text of arbitrary length', 'five ', 'chara', 'cters in this row', 'five ', 'or more', 'or 5 ', 'just ', CAST('five.' AS VARCHAR_IGNORECASE));    
CREATE CACHED TABLE "PUBLIC"."TEXT_TYPES"(
    "T1" TEXT,
    "T2" CLOB,
    "T3" CHARACTER LARGE OBJECT,
    "T4" TINYTEXT,
    "T5" MEDIUMTEXT,
    "T6" LONGTEXT,
    "T7" NTEXT,
    "T8" NCLOB,
    "T9" CLOB(10240)
);   
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.TEXT_TYPES;               
INSERT INTO "PUBLIC"."TEXT_TYPES" VALUES
('Lorem ipsum dolor sit amet, consectetur adipiscing elit. In euismod fringilla euismod. Sed urna eros, vehicula dictum nisi sit amet, commodo rhoncus lorem. Vivamus et magna vitae neque vestibulum consectetur ut nec augue. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Nullam massa erat, venenatis consequat tellus eu, dapibus rhoncus tortor. Nam vel mattis sapien. Etiam in ex ex. Suspendisse ut massa tristique, egestas justo at, fringilla tortor. Nam tempus, metus vitae vehicula aliquam, risus dolor lacinia mauris, sit amet blandit nisl est auctor quam. Vestibulum feugiat elementum porta. Ut ultrices condimentum libero, a vehicula felis volutpat eget. Quisque mollis, tortor id tristique aliquam, ipsum nibh tristique nulla, et lobortis dui odio nec est. Nullam sollicitudin nibh nec congue sodales. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Quisque in libero et sapien suscipit fermentum.', 'Duis augue est, luctus ut massa faucibus, euismod imperdiet felis. Duis nec tortor eget leo ultrices tincidunt. Mauris bibendum mi rhoncus rutrum vehicula. Proin rhoncus eleifend augue, vitae dictum augue. Vivamus rutrum malesuada mauris quis faucibus. Aliquam non eros odio. Ut sit amet egestas sapien. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Praesent dapibus congue imperdiet. Sed at commodo nisi. Etiam volutpat cursus maximus. Donec dignissim diam vel turpis ultricies volutpat.', 'Phasellus a fermentum lectus, sit amet placerat sapien. Sed gravida, purus sed commodo tincidunt, elit quam fermentum risus, at feugiat nisi arcu et metus. Maecenas tempus neque quis ornare sagittis. Pellentesque semper, magna nec elementum pellentesque, magna velit luctus mi, quis fermentum velit ipsum ut enim. Fusce non risus id leo tempor lobortis. Quisque auctor tristique magna, sed imperdiet mauris mattis nec. Maecenas pharetra tellus vitae justo rutrum, et semper mauris ullamcorper.', 'i got tired of copypasting lipsum', 'some more text', 'long text it couldve been', 'who knows what n means?', 'not me tbh', 'is this 9 yet or');      
CREATE CACHED TABLE "PUBLIC"."BINARY_TYPES"(
    "B1" BLOB,
    "B2" BINARY LARGE OBJECT,
    "B3" BINARY,
    "B4" BINARY(32),
    "B5" BYTEA
);              
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.BINARY_TYPES;             
CREATE CACHED TABLE "PUBLIC"."FLOAT_TYPES"(
    "F1" REAL,
    "F2" FLOAT(14),
    "F3" FLOAT4
);              
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.FLOAT_TYPES;              
INSERT INTO "PUBLIC"."FLOAT_TYPES" VALUES
(3.14159, 3.141595, 3.14159);        
CREATE CACHED TABLE "PUBLIC"."MISC_TYPES"(
    "M1" UUID
);    
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.MISC_TYPES;               
