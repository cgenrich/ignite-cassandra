CREATE KEYSPACE ks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} AND durable_writes = true;

CREATE TABLE if not exists ks.tbl (
first uuid,
second uuid,
third int,
fourth int,
fifth text,
sixth blob,
seventh blob,
eighth bigint,
PRIMARY KEY (first, second)
) WITH CLUSTERING ORDER BY (second ASC);