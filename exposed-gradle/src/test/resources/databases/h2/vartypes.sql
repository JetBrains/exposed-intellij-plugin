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
DROP TABLE IF EXISTS DATETIME_TYPES;

DROP TABLE IF EXISTS INDEX_TABLE;
DROP INDEX IF EXISTS IDX1;


CREATE CACHED TABLE "PUBLIC"."INTEGER_TYPES"(
    "I1" INT NOT NULL,
    "I2" INTEGER NOT NULL,
    "I3" MEDIUMINT NOT NULL,
    "I4" INT4 NOT NULL,
    "I5" SIGNED NOT NULL
);            

INSERT INTO "PUBLIC"."INTEGER_TYPES" VALUES
(-2147483648, 2147483647, -2147483648, 2147483647, -2147483648);   

CREATE CACHED TABLE "PUBLIC"."BOOLEAN_TYPES"(
    "B1" BOOLEAN NOT NULL,
    "B2" BIT NOT NULL,
    "B3" BOOL NOT NULL
);

INSERT INTO "PUBLIC"."BOOLEAN_TYPES" VALUES
(TRUE, FALSE, TRUE);

CREATE CACHED TABLE "PUBLIC"."SMALL_INT_TYPES"(
    "T1" TINYINT NOT NULL,
    "S1" SMALLINT NOT NULL,
    "S2" INT2 NOT NULL,
    "S3" YEAR NOT NULL
);

INSERT INTO "PUBLIC"."SMALL_INT_TYPES" VALUES
(127, -32768, 32767, 2020);

CREATE CACHED TABLE "PUBLIC"."LONG_TYPES"(
    "L1" BIGINT NOT NULL,
    "L2" INT8 NOT NULL,
    "L3" IDENTITY NOT NULL
);

INSERT INTO "PUBLIC"."LONG_TYPES" VALUES
(-9223372036854775808, 9223372036854775807, 0);

CREATE CACHED TABLE "PUBLIC"."DECIMAL_TYPES"(
    "D1" DECIMAL(10) NOT NULL,
    "D2" DECIMAL(10, 5) NOT NULL,
    "D3" DEC(5) NOT NULL,
    "D4" DEC(15, 10) NOT NULL,
    "D5" NUMBER(3) NOT NULL,
    "D6" NUMBER(3, 2) NOT NULL,
    "D7" NUMERIC(4) NOT NULL,
    "D8" NUMERIC(4, 2) NOT NULL
);

INSERT INTO "PUBLIC"."DECIMAL_TYPES" VALUES
(1234567890, 12345.67891, 12345, 12345.6789012345, 123, 1.23, 1234, 12.30);

CREATE CACHED TABLE "PUBLIC"."DOUBLE_TYPES"(
    "D1" DOUBLE NOT NULL,
    "D2" DOUBLE PRECISION NOT NULL,
    "D3" FLOAT NOT NULL,
    "D4" FLOAT(27) NOT NULL,
    "D5" FLOAT8 NOT NULL
);

INSERT INTO "PUBLIC"."DOUBLE_TYPES" VALUES
(3.46, 3.141592653589793, 3.141592653589793, 3.141592653589793, 3.141592653589793);

CREATE CACHED TABLE "PUBLIC"."CHAR_TYPES"(
    "C1" CHAR NOT NULL,
    "C2" CHAR(5) NOT NULL,
    "C3" CHARACTER(5) NOT NULL,
    "C4" NCHAR(5) NOT NULL
);

INSERT INTO "PUBLIC"."CHAR_TYPES" VALUES
('text longer than five characters', 'five', 'chara', 'cters');
CREATE CACHED TABLE "PUBLIC"."VARCHAR_TYPES"(
    "C1" VARCHAR NOT NULL,
    "C2" VARCHAR(5) NOT NULL,
    "C3" CHARACTER VARYING(5) NOT NULL,
    "C4" LONGVARCHAR NOT NULL,
    "C5" VARCHAR2(5) NOT NULL,
    "C6" NVARCHAR NOT NULL,
    "C7" NVARCHAR2(5) NOT NULL,
    "C8" VARCHAR_CASESENSITIVE(5) NOT NULL,
    "C9" VARCHAR_IGNORECASE(5) NOT NULL
);

INSERT INTO "PUBLIC"."VARCHAR_TYPES" VALUES
('text of arbitrary length', 'five ', 'chara', 'cters in this row', 'five ', 'or more', 'or 5 ', 'just ', CAST('five.' AS VARCHAR_IGNORECASE));
CREATE CACHED TABLE "PUBLIC"."TEXT_TYPES"(
    "T1" TEXT NOT NULL,
    "T2" CLOB NOT NULL,
    "T3" CHARACTER LARGE OBJECT NOT NULL,
    "T4" TINYTEXT NOT NULL,
    "T5" MEDIUMTEXT NOT NULL,
    "T6" LONGTEXT NOT NULL,
    "T7" NTEXT NOT NULL,
    "T8" NCLOB NOT NULL,
    "T9" CLOB(10240) NOT NULL
);

INSERT INTO "PUBLIC"."TEXT_TYPES" VALUES
('Lorem ipsum dolor sit amet, consectetur adipiscing elit. In euismod fringilla euismod. Sed urna eros, vehicula dictum nisi sit amet, commodo rhoncus lorem. Vivamus et magna vitae neque vestibulum consectetur ut nec augue. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Nullam massa erat, venenatis consequat tellus eu, dapibus rhoncus tortor. Nam vel mattis sapien. Etiam in ex ex. Suspendisse ut massa tristique, egestas justo at, fringilla tortor. Nam tempus, metus vitae vehicula aliquam, risus dolor lacinia mauris, sit amet blandit nisl est auctor quam. Vestibulum feugiat elementum porta. Ut ultrices condimentum libero, a vehicula felis volutpat eget. Quisque mollis, tortor id tristique aliquam, ipsum nibh tristique nulla, et lobortis dui odio nec est. Nullam sollicitudin nibh nec congue sodales. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Quisque in libero et sapien suscipit fermentum.', 'Duis augue est, luctus ut massa faucibus, euismod imperdiet felis. Duis nec tortor eget leo ultrices tincidunt. Mauris bibendum mi rhoncus rutrum vehicula. Proin rhoncus eleifend augue, vitae dictum augue. Vivamus rutrum malesuada mauris quis faucibus. Aliquam non eros odio. Ut sit amet egestas sapien. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Praesent dapibus congue imperdiet. Sed at commodo nisi. Etiam volutpat cursus maximus. Donec dignissim diam vel turpis ultricies volutpat.', 'Phasellus a fermentum lectus, sit amet placerat sapien. Sed gravida, purus sed commodo tincidunt, elit quam fermentum risus, at feugiat nisi arcu et metus. Maecenas tempus neque quis ornare sagittis. Pellentesque semper, magna nec elementum pellentesque, magna velit luctus mi, quis fermentum velit ipsum ut enim. Fusce non risus id leo tempor lobortis. Quisque auctor tristique magna, sed imperdiet mauris mattis nec. Maecenas pharetra tellus vitae justo rutrum, et semper mauris ullamcorper.', 'i got tired of copypasting lipsum', 'some more text', 'long text it couldve been', 'who knows what n means?', 'not me tbh', 'is this 9 yet or');
CREATE CACHED TABLE "PUBLIC"."BINARY_TYPES"(
    "B1" BLOB NOT NULL,
    "B2" BINARY LARGE OBJECT NOT NULL,
    "B3" BINARY NOT NULL,
    "B4" BINARY(32) NOT NULL,
    "B5" BYTEA NOT NULL
);

CREATE CACHED TABLE "PUBLIC"."FLOAT_TYPES"(
    "F1" REAL NOT NULL,
    "F2" FLOAT(14) NOT NULL,
    "F3" FLOAT4 NOT NULL
);

INSERT INTO "PUBLIC"."FLOAT_TYPES" VALUES
(3.14159, 3.141595, 3.14159);
CREATE CACHED TABLE "PUBLIC"."MISC_TYPES"(
    "M1" UUID NOT NULL
);

CREATE TABLE DATETIME_TYPES(
    D1 DATE NOT NULL,
    D2 TIMESTAMP NOT NULL,
    D3 DATETIME NOT NULL
);

CREATE TABLE INDEX_TABLE(
    I1 INTEGER NOT NULL,
    I2 INTEGER NOT NULL,
    I3 INTEGER NOT NULL
);

CREATE UNIQUE INDEX IDX1 ON INDEX_TABLE(I1, I3);

