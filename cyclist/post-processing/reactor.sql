drop view if exists Built;
create view Built as
    SELECT a.simid as simid, tl.time AS time, COUNT(a.agentid) AS n, Prototype
    FROM agents AS a
    JOIN timelist AS tl ON tl.time=a.entertime AND tl.simid=a.simid
    GROUP BY a.simid, time, prototype;
    
drop view if exists BuiltPerYear;
create view BuiltPerYear as
    Select Built.SimID, info.InitialYear+time/12 as Year, total(n) as n, Prototype
    from Built, Info
    where Built.SimID = Info.SimID
    group by Built.SimID, Year, Prototype;
    
 
drop table if exists Deployed;
create table Deployed as
	select * from (
	    select tl.SimID as SimID, tl.Time AS time, COUNT(a.Agentid) AS n, Prototype
	    from timelist AS tl
	    left join agents AS a 
	       on a.entertime <= tl.time AND (a.exittime >= tl.time OR a.exittime IS NULL)
	             AND (tl.time < a.entertime + a.lifetime) AND a.simid=tl.simid
	    group by tl.SimID, tl.Time, Prototype)
	where prototype is not null;
	

drop view if exists DeployedPerYear;
create view DeployedPerYear as
	select d.simid, InitialYear + d.time/12 as year, prototype, ifnull(n,0) as n
	from Deployed as d, Info
	where d.SimID = Info.SimID and d.time%12 = 0;