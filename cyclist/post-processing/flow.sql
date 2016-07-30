drop table if exists Flow;
create table Flow as
  select tr.simid as simid, tr.time as time, send.prototype as sender, recv.prototype as receiver, commodity, total (qty.q) as quantity
    from transactions as tr
      join agents as send on tr.senderid = send.agentid and send.simid = tr.simid
      join agents as recv on tr.receiverid = recv.agentid and recv.simid = tr.simid
      join ( 
      	select r.simid as simid, r.resourceid as resourceid, total(r.quantity*c.massfrac) as q, count(*) as n
  		  from resources as r, compositions as c
  		 where r.simid = c.simid and r.qualid = c.qualid
  		 group by r.simid, r.resourceid
      ) as qty on tr.simid = qty.simid and tr.resourceid = qty.resourceid
    group by tr.simid, tr.time, commodity, sender, receiver;
    
 drop view if exists FlowPerYear;
 create view FlowPerYear as
    select f.SimID, InitialYear+time/12 as year, sender, receiver, commodity, total(Quantity)/1000 as Quantity
    from Flow as f, Info
    where f.SimID = Info.SimID
    group by f.SimID, Year, sender, Receiver, Commodity;