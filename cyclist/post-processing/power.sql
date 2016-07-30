-- 
-- general post processing
--

--
--Power

drop table if exists Power;
create table Power as
    select p.SimID as SimID, p.Time as Time, Prototype, total(p.value) as Power
	    from timeseriespower as p
	        natural join agents
	    group by p.SimID, p.Time, Prototype;
	
-- Per year

drop view if exists PowerPerYear;
create view PowerPerYear as
    select Power.SimID, InitialYear+Time/12 as Year, Power/1000 as Power, Prototype
    from Power, Info
    where Time % 12 = 0 and Power.SimID = Info.SimID;