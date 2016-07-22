-- 
-- general post processing
--

--
--Power
--

drop view if exists Power;
create view Power
    as SELECT sub.simid as SimID, tl.Time AS Time, TOTAL(sub.Power) AS Power, Prototype
    FROM timelist as tl LEFT JOIN (
        SELECT p.simid AS simid,p.Time AS Time, TOTAL(p.Value) AS Power, a.prototype as Prototype
        FROM timeseriespower AS p
        JOIN agents as a on a.agentid=p.agentid AND a.simid=p.simid
         WHERE sub.simid IS NOT NULL
        GROUP BY p.Time, a.prototype, p.simid
    ) AS sub ON tl.time=sub.time AND tl.simid=sub.simid

    GROUP BY sub.SimID, tl.Time, Prototype;

--
--Built
--
drop view if exists built_data;
create view built_data as
    SELECT a.simid as simid, tl.time AS time, COUNT(a.agentid) AS n, Prototype
    FROM agents AS a
    JOIN timelist AS tl ON tl.time=a.entertime AND tl.simid=a.simid
    GROUP BY a.simid, time, prototype;

drop view if exists Built;
create view Built as
    select keys.simid as simid, keys.time as time, keys.prototype as prototype, ifnull(n,0) as n
    from (
	    select *
	        from timelist as t
	        natural join (select simid, prototype from built_data group by simid, prototype)
	        ) as keys
	    left join built_data as data
	on keys.simid = data.simid and keys.time = data.time and keys.prototype = data.prototype;

drop view if exists BuiltPerYear;
create view BuiltPerYear as
    Select Built.SimID, info.InitialYear+time/12 as Year, sum(n) as n, Prototype
    from Built, Info
    where Built.SimID = Info.SimID
    group by Built.SimID, Year, Prototype;

--
--Deployed
--

drop table if exists deployed_data;
create table deployed_data as
	select * from (
	    select tl.SimID as SimID, tl.Time AS time, COUNT(a.Agentid) AS n, Prototype
	    from timelist AS tl
	    left join agents AS a ON a.entertime <= tl.time AND (a.exittime >= tl.time OR a.exittime IS NULL)
	             AND (tl.time < a.entertime + a.lifetime) AND a.simid=tl.simid
	    group by tl.SimID, tl.Time, Prototype)
	where prototype is not null;


drop view if exists Deployed;
create view Deployed as
 select keys.simid, keys.time, keys.prototype, ifnull(n,0) as n
    from (
	    select *
	        from timelist as t
	        natural join (select simid, prototype from deployed_data group by prototype)
	        ) as keys
	    left join deployed_data as data
	on keys.simid = data.simid and keys.time = data.time and keys.prototype = data.prototype;

drop view if exists DeployedPerYear;
create view DeployedPerYear as
   select keys.simid, InitialYear + keys.time/12 as year, keys.prototype, ifnull(n,0) as n
    from  (
	    select *
	        from (select * from timelist where time % 12 = 0)
	        natural join (select simid, prototype from deployed_data group by prototype)
	        ) as keys
	    left join deployed_data as data
	on keys.simid = data.simid and keys.time = data.time and keys.prototype = data.prototype,
	Info
	where keys.SimID = Info.SimID;


--
--Retired
--

drop view if exists RetiredPerYear;
create view RetiredPerYear as
    select b.SimID as SimId, b.Year as Year, max(0, total(d1.n- d.n + b.n)) as n, b.Prototype as
    Prototype
    from BuiltPerYear as b, DeployedPerYear as d, DeployedPerYear as d1
    where b.SimID = d.SimID and b.SimID = d1.SimID
      and b.prototype = d.prototype and b.prototype = d1.prototype
      and b.year = d.year and b.year = d1.year+1
     group by b.SimID, b.Year, b.Prototype;



