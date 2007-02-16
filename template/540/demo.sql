CREATE TABLE EMP(ID IDENTITY PRIMARY KEY, EMP_NO NUMERIC(4,0) NOT NULL ,EMP_NAME VARCHAR(20),MGR_ID NUMERIC(8,0),HIREDATE DATE,SAL NUMERIC(7,2),DEPT_ID NUMERIC(8,0), VERSION_NO NUMERIC(8));
CREATE TABLE DEPT(ID IDENTITY PRIMARY KEY, DEPT_NO NUMERIC(2,0) NOT NULL,DEPT_NAME VARCHAR(20),LOC VARCHAR(20), VERSION_NO NUMERIC(8,0));

INSERT INTO EMP VALUES(null,7369,'SMITH',13,'1980-12-17',800,2,0);
INSERT INTO EMP VALUES(null,7499,'ALLEN',6,'1981-02-20',1600,3,0);
INSERT INTO EMP VALUES(null,7521,'WARD',6,'1981-02-22',1250,3,0);
INSERT INTO EMP VALUES(null,7566,'JONES',9,'1981-04-02',2975,2,0);
INSERT INTO EMP VALUES(null,7654,'MARTIN',6,'1981-09-28',1250,3,0);
INSERT INTO EMP VALUES(null,7698,'BLAKE',9,'1981-05-01',2850,3,0);
INSERT INTO EMP VALUES(null,7782,'CLARK',9,'1981-06-09',2450,1,0);
INSERT INTO EMP VALUES(null,7788,'SCOTT',4,'1982-12-09',3000.0,2,0);
INSERT INTO EMP VALUES(null,7839,'KING',NULL,'1981-11-17',5000,1,0);
INSERT INTO EMP VALUES(null,7844,'TURNER',6,'1981-09-08',1500,3,0);
INSERT INTO EMP VALUES(null,7876,'ADAMS',8,'1983-01-12',1100,2,0);
INSERT INTO EMP VALUES(null,7900,'JAMES',6,'1981-12-03',950,3,0);
INSERT INTO EMP VALUES(null,7902,'FORD',4,'1981-12-03',3000,2,0);
INSERT INTO EMP VALUES(null,7934,'MILLER',7,'1982-01-23',1300,1,0);
INSERT INTO DEPT VALUES(null,10,'ACCOUNTING','NEW YORK',0);
INSERT INTO DEPT VALUES(null,20,'RESEARCH','DALLAS',0);
INSERT INTO DEPT VALUES(null,30,'SALES','CHICAGO',0);
INSERT INTO DEPT VALUES(null,40,'OPERATIONS','BOSTON',0);