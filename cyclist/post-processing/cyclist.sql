-- post processing done in Cyclist

--
-- Fix Agent table
--

update Agents 
	set ExitTime = EnterTime+(select Duration from Info where Agents.SimId=Info.SimId ) 
	where ExitTime is null and Lifetime = -1;
	
update Agents 
	set ExitTime=(EnterTime+Lifetime) where ExitTime is null and  Lifetime != -1;
	

--
-- Facilities
--
	
create table if not exists Facilities (SimID BLOB,
	AgentId INTEGER,
	Spec TEXT,
	Prototype TEXT,
	InstitutionId INTEGER,
	RegionId INTEGER,
	EnterTime INTEGER,
	ExitTime INTEGER,
	Lifetime INTEGER);
	
replace into Facilities(SimID,AgentId,Spec,Prototype,InstitutionId,RegionId,EnterTime,ExitTime,Lifetime) 
	select f.SimId, f.AgentId, f.Spec, f.Prototype, i.AgentId, 
		cast(-1 as INTEGER), f.EnterTime, f.ExitTime, f.Lifetime from Agents as f, Agents as i 
	where f.Kind = 'Facility' and i.Kind = 'Inst' and f.ParentId = i.AgentId and f.SimId = i.SimId;
	
create index if not exists Facilities_idx on Facilities (SimId ASC, AgentId ASC);	

--
--Inventory
--

CREATE view if not exists QuantityInventory as 
	SELECT inv.SimId as SimId, tl.Time AS Time, SUM(inv.Quantity*cmp.MassFrac) AS Quantity, cmp.NucId AS NucId,ag.AgentID as AgentID, 
 	 	ag.Kind AS Kind, ag.Spec AS Spec, ag.Prototype AS Prototype 
 	FROM 
		Timelist AS tl 
		INNER JOIN Inventories AS inv ON inv.StartTime <= tl.Time AND inv.EndTime > tl.Time AND inv.SimId=tl.SimId
		INNER JOIN Agents AS ag ON ag.AgentId = inv.AgentId AND ag.SimId=tl.SimId
		INNER JOIN Compositions AS cmp ON cmp.QualId = inv.QualId AND cmp.SimId=tl.SimId
 	GROUP BY inv.SimId, tl.Time,ag.AgentId,cmp.NucId;
 	
 	
--
-- Transactions
--

CREATE table if not exists QuantityTransactedBase as 
	SELECT res.SimId as SimId, tr.Time AS Time,cmp.NucId AS NucId, ag.AgentID as AgentID, 
			cast(SUM(cmp.MassFrac * res.Quantity) AS REAL) AS Quantity 
	FROM 
		Resources AS res 
		INNER JOIN Transactions AS tr ON tr.ResourceId = res.ResourceId 
		INNER JOIN Agents AS ag ON ag.AgentId = tr.SenderID 
		INNER JOIN Compositions AS cmp ON cmp.QualId = res.QualId 
	WHERE 
		tr.SimId = res.SimId AND ag.SimId = tr.SimId and cmp.SimId=res.SimId 
	GROUP BY res.SimId, cmp.NucId, tr.Time, ag.AgentID
	ORDER BY tr.Time ASC;
							   
CREATE INDEX IF NOT EXISTS QuantityTransactedBase_idx ON QuantityTransactedBase (simid,agentid,time,nucid,quantity); 	

drop view if exists QuantityTransacted;
CREATE view if QuantityTransacted as
	SELECT base.SimID as SimID, Time, Quantity, NucId, base.AgentId as AgentId, Kind, Spec, Prototype
 	FROM
  		QuantityTransactedBase as base, Agents ag
 	WHERE
		base.SimID = ag.SimID
 		AND base.AgentId = ag.AgentId;
 		
 		
--
-- indicate cyclist post processing was done on this db
--
create table if not exists UpdatedIndication (flag INTEGER DEFAULT 1);
			 	