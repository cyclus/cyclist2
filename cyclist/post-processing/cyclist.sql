-- post processing currently done in Cyclist

--
-- Fix Agent table  (TODO: is it still needed?)
--

update Agents 
	set ExitTime = EnterTime+(select Duration from Info where Agents.SimID=Info.SimID ) 
	where ExitTime is null and Lifetime = -1;
	
update Agents 
	set ExitTime=(EnterTime+Lifetime) where ExitTime is null and  Lifetime != -1;
	

--
-- Facilities
--

drop table if exists Facilities;
create table Facilities (SimID BLOB,
	AgentID INTEGER,
	Spec TEXT,
	Prototype TEXT,
	InstitutionID INTEGER,
	RegionID INTEGER,
	EnterTime INTEGER,
	ExitTime INTEGER,
	Lifetime INTEGER);
	
Insert into Facilities(SimID,AgentID,Spec,Prototype,InstitutionID,RegionID,EnterTime,ExitTime,Lifetime) 
	select f.SimID, f.AgentID, f.Spec, f.Prototype, i.AgentID, 
		cast(-1 as INTEGER), f.EnterTime, f.ExitTime, f.Lifetime from Agents as f, Agents as i 
	where f.Kind = 'Facility' and i.Kind = 'Inst' and f.ParentID = i.AgentID and f.SimID = i.SimID;
	
create index if not exists Facilities_idx on Facilities (SimID ASC, AgentID ASC);	

--
-- Prototypes
--

drop table if exists prototypes;
create table Prototypes as
    select SimID, Prototype, AgentID, Kind, Spec, min(EnterTime) as EnterTime, max(ExitTime) as ExitTime
    from Agents
    group by SimID, Prototype;
    
--
--Inventory
--

-- drop table if exists QuantityInventory_base;
-- create table QuantityInventory_base as
--	select inv.simid as simid, inv.starttime as starttime, inv.endtime as endtime,  a.AgentID as AgentID, Prototype, Kind, Spec, NucID, total(inv.Quantity*c.MassFrac) AS Quantity
--	  from inventories as inv
--	 	   join compositions as c on c.qualid=inv.qualid AND c.simid=inv.simid
--		   join agents as a on a.agentid=inv.agentid AND a.simid=inv.simid
--	 group by inv.simid, starttime, endtime, NucID, a.AgentID;
	 
drop table if exists QuantityInventory;
create table QuantityInventory as
  with base as (
   select inv.simid as simid, inv.starttime as starttime, inv.endtime as endtime,  a.AgentID as AgentID, Prototype, Kind, Spec, NucID, total(inv.Quantity*c.MassFrac) AS Quantity
	  from inventories as inv
	 	   join compositions as c on c.qualid=inv.qualid AND c.simid=inv.simid
		   join agents as a on a.agentid=inv.agentid AND a.simid=inv.simid
	 group by inv.simid, starttime, endtime, NucID, a.AgentID
  )
  select tl.simid as simid, tl.time as time, AgentID, prototype, Kind, Spec, NucID, total(quantity)/1000 as quantity
    from timelist as tl
         join base as base on UNLIKELY(base.starttime <= tl.time) AND base.endtime > tl.time AND tl.simid=base.simid
    group by tl.simid, tl.time, AgentID, NucID;


create index QuantityInventory_Agent_idx ON QuantityInventory (SimID, Time, AgentID);
create index QuantityInventory_NucID_idx ON QuantityInventory (SimID, Time, NucID);
create index QuantityInventory_Agent_NucID_idx ON QuantityInventory (SimID, Time, AgentID, NucID);

drop table if exists QuantityInventoryPerYear;
create table QuantityInventoryPerYear as
	select qi.SimID, InitialYear+qi.Time/12 as Year, AgentID, Prototype, kind, Spec, NucID, total(quantity)/1000 as Quantity
	from QuantityInventory qi, Info
	where qi.SimID = Info.SimID
	group by qi.SimID, Year, AgentID;
	  
-- Previous (wrong) definition
--
--CREATE view if not exists QuantityInventory as 
--	SELECT inv.SimID as SimID, tl.Time AS Time, SUM(inv.Quantity*cmp.MassFrac) AS Quantity, cmp.NucID AS NucID,ag.AgentID as AgentID, 
-- 	 	ag.Kind AS Kind, ag.Spec AS Spec, ag.Prototype AS Prototype 
-- 	FROM 
--		Timelist AS tl 
--		INNER JOIN Inventories AS inv ON inv.StartTime <= tl.Time AND inv.EndTime > tl.Time AND inv.SimID=tl.SimID
--		INNER JOIN Agents AS ag ON ag.AgentID = inv.AgentID AND ag.SimID=tl.SimID
--		INNER JOIN Compositions AS cmp ON cmp.QualID = inv.QualID AND cmp.SimID=tl.SimID
-- 	GROUP BY inv.SimID, tl.Time,ag.AgentID,cmp.NucID;
 	
--
-- Transactions
--

drop table if exists QuantityTransacted;
CREATE table QuantityTransacted as 
	SELECT res.SimID as SimID, tr.Time AS Time, Commodity, cmp.NucID AS NucID, SenderID, ReceiverID,
			cast(TOTAL(cmp.MassFrac * res.Quantity) AS REAL) AS Quantity 
	FROM 
		Resources AS res 
		INNER JOIN Transactions AS tr ON tr.ResourceID = res.ResourceID 
		INNER JOIN Compositions AS cmp ON cmp.QualID = res.QualID 
	WHERE 
		tr.SimID = res.SimID and cmp.SimID = res.SimID 
	GROUP BY res.SimID, tr.Time, SenderID, ReceiverID, Commodity, cmp.NucID
	ORDER BY tr.Time ASC;
						   
CREATE INDEX IF NOT EXISTS QuantityTransacted_Sender_idx ON QuantityTransacted (SimID, time, SenderID); 	
CREATE INDEX IF NOT EXISTS QuantityTransacted_Receiver_idx ON QuantityTransacted (SimID, time, ReceiverID); 	
CREATE INDEX IF NOT EXISTS QuantityTransacted_NucID_idx ON QuantityTransacted (SimID, time, NucID); 	
CREATE INDEX IF NOT EXISTS QuantityTransacted_SenderReceieverNucID_idx ON QuantityTransacted (SimID, time, SenderID, ReceiverID, NucID); 	
 
drop table if exists QuantityTransactedPerYear;
CREATE table QuantityTransactedPerYear as
	SELECT qt.SimID as SimID, InitialYear + Time/12 as year, SenderID, ReceiverID, Commodity, NucID, total(Quantity)/1000 as Quantity
 	FROM
  		QuantityTransacted as qt, Info
 	WHERE
		qt.SimID = Info.SimID
 	group by qt.SimID, Year, SenderId, ReceiverID, NucID;
 	


    
--
-- Inform Cyclist not post process this db
--
    
create table if not exists UpdatedIndication (flag INTEGER DEFAULT 1);
			 	