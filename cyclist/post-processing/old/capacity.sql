-- Capacity

drop table if exists reactor_power_factor;
create table reactor_power_factor (
    Prototype Text,
    Factor real
);

insert into reactor_power_factor values
('LWR_A', 1.0),
('LWR_B', 1.0),
('SFR_A', 0.4),
('SFR_B', 0.4);

drop table if exists CapacityPerYear;
create table CapacityPerYear (
    SimID blob,
    Year int,
    Type text,
    Capacity real,
    Prototype text
    );

insert into CapacityPerYear
    select SimID, Year, 'Built', n * Factor as Capacity, b.Prototype 
    from BuiltPerYear as b, reactor_power_factor as f
    where b.Prototype = f.Prototype;

insert into CapacityPerYear
    select SimID, Year, 'Deployed', n*Factor as Capacity, b.Prototype 
    from DeployedPerYear as b, reactor_power_factor as f
    where b.Prototype = f.Prototype;

--insert into CapacityPerYear
--    select b.SimID as SimId, b.Year as Year, 'Retired', 
--   	   max(0, total(d1.Capacity - d.Capacity + b.Capacity)) as Capacity, 
--    	   b.Prototype as Prototype
--    from CapacityPerYear as b, CapacityPerYear as d, CapacityPerYear as d1
--    where b.type = 'Built' and d.type = 'Deployed' and d1.type = 'Deployed'
--      and b.SimID = d.SimID and b.SimID = d1.SimID
--      and b.prototype = d.prototype and b.prototype = d1.prototype
--      and b.year = d.year and b.year = d1.year+1
--    group by b.SimID, b.Year, b.Prototype;
    
    
insert into CapacityPerYear
with years as (
	select t.simid, InitialYear + t.time/12 as year 
	from timelist as t, Info
	where t.simid = Info.simid and t.time%12 = 0
), 
keys as (
  select years.simid as simid, year, prototype
  from years
     left join (select distinct simid, prototype from CapacityPerYear) as k on years.simid = k.simid
),
nb as (
  select keys.simid as simid, keys.year as year, keys.prototype as prototype, ifnull(Capacity, 0) as Capacity
  from keys
  left outer join (select * from CapacityPerYear where type = 'Built') as b 
    on keys.simid = b.simid and keys.year = b.year and keys.prototype = b.prototype
),
 nd as (
  select keys.simid as simid, keys.year as year, keys.prototype as prototype, ifnull(Capacity, 0) as Capacity
  from keys
  left outer join (select * from CapacityPerYear where type = 'Deployed') as d 
    on keys.simid = d.simid and keys.year = d.year and keys.prototype = d.prototype
)
select nb.simid as simid, nb.year as year, 'Retired' as Type, max(0, ifnull(d1.Capacity,0) - ifnull(d.Capacity,0) + ifnull(nb.Capacity,0)) as Capacity, nb.prototype as prototype 
from nb
  inner join nd as d on (nb.simid = d.simid  and nb.prototype = d.prototype and  nb.year = d.year)
  inner join nd as d1 on (nb.simid = d1.simid  and nb.prototype = d1.prototype and  nb.year = d1.year+1);
  
  
 =====
 
drop table if exists CapacityPerYear;
create table CapacityPerYear (
    SimID blob,
    Year int,
    Type text,
    Capacity real,
    Prototype text
    );

insert into CapacityPerYear
    select SimID, Year, 'Built', n * Factor as Capacity, b.Prototype 
    from BuiltPerYear as b, reactor_power_factor as f
    where b.Prototype = f.Prototype;

insert into CapacityPerYear
    select SimID, Year, 'Deployed', n*Factor as Capacity, b.Prototype 
    from DeployedPerYear as b, reactor_power_factor as f
    where b.Prototype = f.Prototype;
    
    
insert into CapacityPerYear
with years as (
	select t.simid, InitialYear + t.time/12 as year 
	from timelist as t, Info
	where t.simid = Info.simid and t.time%12 = 0
), 
keys as (
  select years.simid as simid, year, prototype
  from years
     left join (select distinct simid, prototype from CapacityPerYear) as k on years.simid = k.simid
),
nb as (
  select keys.simid as simid, keys.year as year, keys.prototype as prototype, Capacity
  from keys
  left outer join (select * from CapacityPerYear where type = 'Built') as b 
    on keys.simid = b.simid and keys.year = b.year and keys.prototype = b.prototype
),
 nd as (
  select keys.simid as simid, keys.year as year, keys.prototype as prototype, Capacity
  from keys
  left outer join (select * from CapacityPerYear where type = 'Deployed') as d 
    on keys.simid = d.simid and keys.year = d.year and keys.prototype = d.prototype
)
select nb.simid as simid, nb.year as year, 'Retired' as Type, max(0, ifnull(d1.Capacity,0) - ifnull(d.Capacity,0) + ifnull(nb.Capacity,0)) as Capacity, nb.prototype as prototype 
from nb
  inner join nd as d on (nb.simid = d.simid  and nb.prototype = d.prototype and  nb.year = d.year)
  inner join nd as d1 on (nb.simid = d1.simid  and nb.prototype = d1.prototype and  nb.year = d1.year+1);

