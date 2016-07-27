--
-- post processing tables and views for EG analysis
--

-- Per year views

drop view if exists PowerPerYear;
create view PowerPerYear as
    select Power.SimID, InitialYear+Time/12 as Year, Power/1000 as Power, Prototype
    from Power, Info
    where Time % 12 = 0 and Power.SimID = Info.SimID;

drop view if exists FlowPerYear;
create view FlowPerYear as
    select Flow.SimID, InitialYear+time/12 as Year, total(Quantity)/1000 as Quantity, Sender, Receiver, Commodity
    from Flow, Info
    where Flow.SimID = Info.SImID
    group by SimID, Year, Sender, Receiver, Commodity;



-- Inventory

drop view if exists InventoryQuantityPerYear;
create view InventoryQuantityPerYear as
    select SimID, InitalYear+Time/12 as Year, Quantity/1000 as Quantity, Prototype
    from InventoryQuantity, Info
    where Time % 12 = 0 and InventoryQuantity.SimID = Info.SimID;

drop view if exists InventoryDetailsPerYear;
create view InventoryDetailsPerYear as
    select SimID, InitialYear+Time/12 as Year, Quantity/1000 as Quantity, Prototype, NucID
    from InventoryDetails, Info
    where Time % 12 = 0 and InventoryDetails.SimID = Info.SimID;
    
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

insert into CapacityPerYear
    select b.SimID as SimId, b.Year as Year, 'Retired', 
    	   max(0, total(d1.Capacity - d.Capacity + b.Capacity)) as Capacity, 
    	   b.Prototype as Prototype
    from CapacityPerYear as b, CapacityPerYear as d, CapacityPerYear as d1
    where b.type = 'Built' and d.type = 'Deployed' and d1.type = 'Deployed'
      and b.SimID = d.SimID and b.SimID = d1.SimID
      and b.prototype = d.prototype and b.prototype = d1.prototype
      and b.year = d.year and b.year = d1.year+1
    group by b.SimID, b.Year, b.Prototype;


