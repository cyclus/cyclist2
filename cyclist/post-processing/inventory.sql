--
--InventoryDetails
--

drop table if exists InventoryDetails;

-- create table InventoryDetails as
--  SELECT a.simid as simid, tl.Time as time, total(inv.Quantity*c.MassFrac) AS Quantity, Prototype, NucID
--         FROM inventories as inv
--         JOIN timelist as tl ON UNLIKELY(inv.starttime <= tl.time) AND inv.endtime > tl.time AND tl.simid=inv.simid
--         JOIN agents as a on a.agentid=inv.agentid AND a.simid=inv.simid
--         JOIN compositions as c on c.qualid=inv.qualid AND c.simid=inv.simid
--         GROUP BY a.simid, tl.Time, prototype, NucID;

-- create table InventoryDetails as
--     SELECT sub.simid AS SimID, tl.Time AS Time, total(sub.Quantity) AS Quantity, Prototype, NucID
--     FROM timelist as tl
--     LEFT JOIN InventoryDetails_base as sub ON sub.time = tl.time and sub.SimID = tl.SimID
--     GROUP BY sub.simid, tl.Time, Prototype, NucID;

-- drop view if exists InventoryDetailsPerYear;
-- create view InventoryDetailsPerYear as
--     select f.SimID as SimID, InitialYear+Time/12 as year, Prototype, NucID, total(Quantity) as Quantity
--     from InventoryDetails, Info as f
--     where InventoryDetails.SimID = f.SimID and Time % 12 = 0;
    
    
    
drop table if exists InventoryDetails;
create table InventoryDetails as
select inv.simid as simid, inv.starttime as starttime, inv.endtime as endtime, total(inv.Quantity*c.MassFrac) AS Quantity, Prototype, NucID
from inventories as inv
JOIN compositions as c on c.qualid=inv.qualid AND c.simid=inv.simid
JOIN agents as a on a.agentid=inv.agentid AND a.simid=inv.simid
group by inv.simid, starttime, endtime, Prototype, NucID;


drop view if exists InventoryDetailsPerMonth;
create view InventoryDetailsPerMonth as
select tl.simid as simid, tl.time as time, prototype, nucid, total(quantity)/1000 as quantity
from timelist as tl
   join InventoryDetails as inv on UNLIKELY(inv.starttime <= tl.time) AND inv.endtime > tl.time AND tl.simid=inv.simid
   group by tl.simid, tl.time, prototype, nucid;
   
   
drop table if exists InventoryDetailsPerYear;
create table InventoryDetailsPerYear as
with tl as (
   select timelist.simid, time, initialyear+time/12 as year from timelist, info where time % 12 = 0 and timelist.simid = info.simid
)
select tl.simid as simid, year, prototype, nucid, total(quantity)/1000 as quantity
from tl
   join InventoryDetails as inv on UNLIKELY(inv.starttime <= tl.time) AND inv.endtime > tl.time AND tl.simid=inv.simid
   group by tl.simid, tl.time, prototype, nucid;