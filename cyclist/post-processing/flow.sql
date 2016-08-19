drop table if exists Flow;

create table Flow as
	with qty as ( 
		select r.simid as SimID, r.ResourceID as ResourceID, total(r.quantity*c.massfrac) as q, count(*) as n
  		   from resources as r, compositions as c
  		where r.SimID = c.SimID and r.QualID = c.QualID
  		group by r.SimID, r.ResourceID
    )
  select tr.SimID as SimID, tr.time as Time, send.Prototype as Sender, recv.Prototype as Receiver, Commodity, total (qty.q) as Quantity
    from transactions as tr
      join Agents as send on tr.SenderID = send.AgentID and send.SimID = tr.SimID
      join Agents as recv on tr.ReceiverID = recv.AgentID and recv.SimID = tr.SimID
      join qty on tr.SimID = qty.SimID and tr.ResourceID = qty.ResourceID
    group by tr.SimID, tr.Time, commodity, sender, receiver;
    
 drop view if exists FlowPerYear;
 create view FlowPerYear as
    select f.SimID as SimID, InitialYear+time/12 as Year, Sender, Receiver, Commodity, total(Quantity)/1000 as Quantity
    from Flow as f, Info
    where f.SimID = Info.SimID
    group by f.SimID, Year, Sender, Receiver, Commodity;