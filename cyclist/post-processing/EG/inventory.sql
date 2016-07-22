--
-- Time consuming tables
---

--
--InventoryQuantity
--No need for this as details is now fast enough
--

--drop table if exists InventoryQuantity_base;
--create table InventoryQuantity_base as
--    SELECT a.simid as simid, tl.Time as time, total(inv.Quantity) AS Quantity, Prototype
--        FROM inventories as inv
--        JOIN timelist as tl ON UNLIKELY(inv.starttime <= tl.time) AND inv.endtime > tl.time AND tl.simid=inv.simid
--        JOIN agents as a on a.agentid=inv.agentid AND a.simid=inv.simid
--        GROUP BY a.simid, tl.Time, Prototype;
--
--
--drop table if exists InventoryQuantity;
--create table InventoryQuantity as
--    SELECT sub.simid AS SimID, tl.Time AS Time, total(sub.Quantity) AS Quantity, Prototype
--    FROM timelist as tl
--    LEFT JOIN InventoryQuantity_base AS sub ON sub.time=tl.time
--    GROUP BY sub.SimID, tl.Time, Prototype;
--
--drop view if exists InventoryQuantity;
--create view InventoryQuantity as
--    select keys.SimID, as SimID, keys.Time as Time, keys.Prototype as Prototype, ifnull(Quantity,0) as Quantity
--    from
--        (select *
--            from timelist as t
--            natural join
--                (select SimID, Prototype from InventoryDetails_base group by SimID, Prototype)
--        ) as keys
--        left join InventoryDetails_base as data
--        on keys.SimID = data.SimID and keys.Time = data.Time
--            and keys.Prototype = data.Prototype;
--
--drop view if exists InventoryQuantityPerYear;
--create view InventoryQuantityPerYear as
--    select f.SimID as SimID, InitialYear+Time/12 as year, Prototype, total(Quantity) as Quantity
--    from InventoryQuantity, Info as f
--    where InventoryQuantity.SimID = f.SimID
--    group by f.SimID, Year, Prototype;


--
--InventoryDetails
--

drop table if exists InventoryDetails_base;
drop table if exists InventoryDetails;

create table InventoryDetails_base as
 SELECT a.simid as simid, tl.Time as time, total(inv.Quantity*c.MassFrac) AS Quantity, Prototype, NucID
        FROM inventories as inv
        JOIN timelist as tl ON UNLIKELY(inv.starttime <= tl.time) AND inv.endtime > tl.time AND tl.simid=inv.simid
        JOIN agents as a on a.agentid=inv.agentid AND a.simid=inv.simid
        JOIN compositions as c on c.qualid=inv.qualid AND c.simid=inv.simid
        GROUP BY a.simid, tl.Time, prototype, NucID;

create table InventoryDetails as
    SELECT sub.simid AS SimID, tl.Time AS Time, total(sub.Quantity) AS Quantity, Prototype, NucID
    FROM timelist as tl
    LEFT JOIN InventoryDetails_base as sub ON sub.time = tl.time and sub.SimID = tl.SimID
    GROUP BY sub.simid, tl.Time, Prototype, NucID;

drop view if exists InventoryDetailsPerYear;
create view InventoryDetailsPerYear as
    select f.SimID as SimID, InitialYear+Time/12 as year, Prototype, NucID, total(Quantity) as Quantity
    from InventoryDetails, Info as f
    where InventoryDetails.SimID = f.SimID and Time % 12 = 0
    group by f.SimID, Year, Prototype;


--
-- Flow
--
--

create table qty_data as
  select r.simid as simid, r.resourceid as resourceid, total(r.quantity*c.massfrac) as q, count(*) as n
  from resources as r, compositions as c
  where r.simid = c.simid and r.qualid = c.qualid
  group by r.simid, r.resourceid;


create table trans_data as
  select tr.simid as simid, tr.time as time, send.prototype as sender, recv.prototype as receiver, commodity, total
  (qty.q) as quantity
    from transactions as tr
      join agents as send on tr.senderid = send.agentid and send.simid = tr.simid
      join agents as recv on tr.receiverid = recv.agentid and recv.simid = tr.simid
      join qty_data as qty on tr.simid = qty.simid and tr.resourceid = qty.resourceid
    group by tr.simid, tr.time, commodity, sender, receiver;


drop view if exists Flow;
create view Flow as
    select keys.simid as simid, keys.time as time,
            keys.sender as sender, keys.receiver as receiver, keys.commodity as commodity,
            ifnull(quantity, 0) as quantity
    from
        (select *
            from timelist as t
            natural join
                (select simid, sender, receiver, commodity from trans_data group by simid, sender, receiver, commodity)
        ) as keys
        left join trans_data as data
        on keys.simid = data.simid and keys.time = data.time
		     and keys.sender = data.sender and keys.receiver;


 drop view if exists FlowPerYear;
 create view FlowPerYear as
    select f.SimID, InitialYear+time/12 as year, sender, receiver, commodity, total(Quantity)/1000 as Quantity
    from Flow as f, Info
    where f.SimID = Info.SimID
    group by f.SimID, Year, sender, Receiver, Commodity;