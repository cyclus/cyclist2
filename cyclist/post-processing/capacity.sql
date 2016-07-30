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

drop table if exists Capacity;
create table Capacity (
    SimID blob,
    time int,
    Type text,
    Capacity real,
    Prototype text
    );
     
--
-- Capacity
--
  
insert into Capacity
    select SimID, time, 'Built', n * Factor as Capacity, b.Prototype 
    from Built as b, reactor_power_factor as f
    where b.Prototype = f.Prototype;  
    
insert into Capacity
	select SimID, time, 'Deployed', n* Factor as Capacity, d.prototype 
	from deployed as d, reactor_power_factor as f
	where d.prototype = f.prototype;

insert into Capacity
with
keys as (
  select tl.simid as simid, time, prototype
  from timelist as tl
     left join (select distinct simid, prototype from Capacity) as k on tl.simid = k.simid
),
nb as (
  select keys.simid as simid, keys.time as time, keys.prototype as prototype, Capacity
  from keys
  left outer join (select * from Capacity where type = 'Built') as b 
    on keys.simid = b.simid and keys.time = b.time and keys.prototype = b.prototype
),
 nd as (
  select keys.simid as simid, keys.time as time, keys.prototype as prototype, Capacity
  from keys
  left outer join (select * from Capacity where type = 'Deployed') as d 
    on keys.simid = d.simid and keys.time = d.time and keys.prototype = d.prototype
)
select nb.simid as simid, nb.time as time, 'Retired' as Type, max(0, ifnull(d1.Capacity,0) - ifnull(d.Capacity,0) + ifnull(nb.Capacity,0)) as Capacity, nb.prototype as prototype 
from nb
  inner join nd as d on (nb.simid = d.simid  and nb.prototype = d.prototype and  nb.time = d.time)
  inner join nd as d1 on (nb.simid = d1.simid  and nb.prototype = d1.prototype and  nb.time = d1.time+1);


--
-- Capacity Per Year
--

drop table if exists CapacityPerYear;
create table CapacityPerYear (
    SimID blob,
    Year int,
    Type text,
    Capacity real,
    Prototype text
    );
    
insert into CapacityPerYear 
select c.simid as simid, InitialYear+c.time/12 as Year, Type, total(Capacity) as Capacity, Prototype
from Capacity as c, Info
where c.simid = info.simid and Type = 'Built'
group by c.simid, Year, Prototype;

insert into CapacityPerYear
select c.simid as simid, InitialYear+c.time/12 as Year, Type, Capacity, Prototype
from Capacity as c, Info
where c.simid = info.simid and Type = 'Deployed' and c.time%12 = 0;


Insert into CapacityPerYear
select c.simid as simid, InitialYear+c.time/12 as Year, Type, total(Capacity) as Capacity, Prototype
from Capacity as c, Info
where c.simid = info.simid and Type = 'Retired'
group by c.simid, Year, Prototype;