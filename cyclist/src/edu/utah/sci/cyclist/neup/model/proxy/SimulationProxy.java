package edu.utah.sci.cyclist.neup.model.proxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import edu.utah.sci.cyclist.core.model.Simulation;
import edu.utah.sci.cyclist.neup.model.Facility;
import edu.utah.sci.cyclist.neup.model.Inventory;
import edu.utah.sci.cyclist.neup.model.Range;
import edu.utah.sci.cyclist.neup.model.Transaction;

public class SimulationProxy {
	static Logger log = Logger.getLogger(SimulationProxy.class);
	
	private Simulation _sim;
	private ObservableList<Facility> _facilities;
	
	public static final String FACILITIES_QUERY =
			"SELECT AgentId, Spec, Prototype, InstitutionId, RegionId FROM Facilities where SimId=?";
	
	public static final String COMMODITY_QUERY = 
			"SELECT distinct(Commodity) FROM Transactions where SimId=?";
	
//	public static final String TRANSACTIONS_QUERY =
//			 "SELECT  SenderId, ReceiverId, Commodity, NucId, Quantity*MassFrac as Amount, Units"
//			+ " FROM Transactions "
//			+ "      JOIN Facilities on (Transactions.SimId = Facilities.SimId and Transactions.%s = Facilities.AgentId) "
//			+ "      JOIN Resources on (Transactions.SimId = Resources.SimId and Transactions.ResourceId = Resources.ResourceId)"
//			+ "      JOIN Compositions on (Transactions.SimId = Compositions.SimId and Compositions.QualId = Resources.QualId)"
//			+ " WHERE" 
//			+ "     Transactions.SimId = ?"
//			+ " and Time >= ? and Time <= ? "
//			+"  and Facilities.%s = ?";
	
	public static final String TRANSACTIONS_QUERY =
			 "SELECT  SenderId, ReceiverId, Commodity, NucId, Quantity as Amount"
			+ " FROM QuantityTransactedPerYear qt"
			+ "      JOIN Facilities on (qt.SimId = Facilities.SimId and qt.%s = Facilities.AgentId) "
			+ " WHERE" 
			+ "     qt.SimId = ?"
			+ " and Time >= ? and Time <= ? "
			+"  and Facilities.%s = ?";
	
	public static final String TRANSACTIONS_PER_YEAR_QUERY =
			 "SELECT  SenderId, ReceiverId, Commodity, NucId, Quantity as Amount"
			+ " FROM QuantityTransactedPerYear qt"
			+ "      JOIN Facilities on (qt.SimId = Facilities.SimId and qt.%s = Facilities.AgentId) "
			+ " WHERE" 
			+ "     qt.SimId = ?"
			+ " and Year >= ? and Year <= ? "
			+"  and Facilities.%s = ?";
	
//	public static final String INVENTORY_QUERY = 
//			"SELECT tl.Time as time, cmp.NucId as nucId, SUM(inv.Quantity*cmp.MassFrac) as amount"
//			+ "	FROM "
//			+ "		TimeList AS tl"
//			+ "			INNER JOIN Inventories AS inv ON inv.StartTime <= tl.Time AND inv.EndTime > tl.Time "
//			+ "			INNER JOIN Agents AS ag ON ag.AgentId = inv.AgentId "
//			+ "			INNER JOIN Compositions AS cmp ON cmp.QualId = inv.QualId "
//			+ "	WHERE"
//			+ "		inv.SimId = cmp.SimId AND inv.SimId = ag.SimId"
//			+ "		AND inv.SimId = ?"
//			+ "		AND ag.%s = ?"
//			+ "     AND tl.Time > 0 "
//			+ "	GROUP BY tl.Time,cmp.NucId";
	
	public static final String INVENTORY_QUERY = 
			"SELECT Time, NucId, Quantity as amount"
			+ "	FROM "
			+ "		QuantityInventory as qi"
			+"      INNER JOIN Agents as a on qi.SimID = a.SimId and qi.AgentId = a.AgentId"
			+ "	WHERE"
			+ "		qi.SimId = ?"
			+ "		AND a.%s = ?"
			+ "     AND Time > 0 "
			+ "	GROUP BY Time,NucId";

	public static final String INVENTORY_PER_YEAR_QUERY = 
			"SELECT Year as Time, NucId, Quantity as amount"
			+ "	FROM "
			+ "		QuantityInventoryPerYear as qi"
			+"      INNER JOIN Agents as a on qi.SimID = a.SimId and qi.AgentId = a.AgentId"
			+ "	WHERE"
			+ "		qi.SimId = ?"
			+ "		AND a.%s = ?"
			+ "	GROUP BY Time, NucId";
			
	public SimulationProxy(Simulation sim) {
		_sim = sim;
	}
	
	public ObservableList<Facility> getFacilities() throws SQLException {
		if (_facilities == null) {
			_facilities = fetchFacilities();
		}
		return _facilities;
	}
	
	public ObservableList<Facility> fetchFacilities() throws SQLException {
		List<Facility> list = new ArrayList<>();
		
		try (Connection conn = _sim.getDataSource().getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement(FACILITIES_QUERY)) {
				stmt.setBytes(1, _sim.getSimulationId().getData());
				
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					Facility f = new Facility(rs.getInt("AgentId"), rs.getString("Spec"), rs.getString("Prototype"),
							rs.getInt("InstitutionId"), rs.getInt("RegionId"));
					list.add(f);
				}
			}	
		} catch (SQLException e) {
			log.error("fetchFacilities ",e);
		} finally {
			_sim.getDataSource().releaseConnection();
		}
		
		return FXCollections.observableList(list);
	}
	
	public ObservableList<String> getCommodities() throws SQLException {
		List<String> list = new ArrayList<String>();
		try (Connection conn = _sim.getDataSource().getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement(COMMODITY_QUERY)) {
				stmt.setBytes(1, _sim.getSimulationId().getData());

			
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					list.add(rs.getString("Commodity"));
				}
			}
		} catch (SQLException e) {
			log.error("getCommodities ",e);
		} finally {
			_sim.getDataSource().releaseConnection();
		}
		return FXCollections.observableList(list);
	}
	
	public ObservableList<Transaction> getTransactions(String type, String value, Range<Integer> timerange, boolean forward) throws SQLException {
		List<Transaction> list = new ArrayList<>();
		try (Connection conn = _sim.getDataSource().getConnection()) {
			String query = String.format(TRANSACTIONS_PER_YEAR_QUERY, forward? "SenderId" : "ReceiverId", type);
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setBytes(1, _sim.getSimulationId().getData());

				stmt.setInt(2, timerange.from);
				stmt.setInt(3, timerange.to);
				stmt.setString(4, value);
				
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					Transaction tr = new Transaction();
					tr.sender = rs.getInt("SenderId");
					tr.receiver = rs.getInt("ReceiverId");
					tr.commodity = rs.getString("Commodity");
					tr.nucid = rs.getInt("NucId");
					tr.amount = rs.getDouble("Amount");					
					list.add(tr);
				}
			}
		} catch (SQLException e) {
			log.error("getTransactions ",e);
		} finally {
			_sim.getDataSource().releaseConnection();
		}
		
		log.debug("retrieived "+list.size()+" transactions");
		return  FXCollections.observableList(list);
	}
	
	public ObservableList<Inventory> getInventory(String type, String value) throws SQLException {
		List<Inventory> list = new ArrayList<>();
		
		String query = String.format(INVENTORY_PER_YEAR_QUERY, type);
		long t0 = System.currentTimeMillis();
		try (Connection conn = _sim.getDataSource().getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setBytes(1, _sim.getSimulationId().getData());
				stmt.setString(2, value);				

				ResultSet rs = stmt.executeQuery();
				long t1 = System.currentTimeMillis();
				while (rs.next()) {
					Inventory i = new Inventory();
					i.time = rs.getInt(1);
					i.nucid = rs.getInt(2);
					i.amount = rs.getDouble(3);
					list.add(i);
				}
				long t2 = System.currentTimeMillis();
				
				log.debug("Inventory size:"+list.size()+"  timing: "+(t2-t0)/1000.0+"sec, execute:"+(t1-t0)/1000.0+"sec, "+(t2-t1)/(float)(list.size())+" ms/item");
			}
		} catch (SQLException e) {
			log.error("getInventory ",e);
		} finally {
			_sim.getDataSource().releaseConnection();
		}
		
		return FXCollections.observableArrayList(list);
	}
	
	public List<Inventory> getInventory2(String type, String value) throws SQLException {
		List<Inventory> list = new ArrayList<>();
		
		String query = String.format(INVENTORY_PER_YEAR_QUERY, type);
		log.debug("query: "+query);
		try (Connection conn = _sim.getDataSource().getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
//				stmt.setString(1, _sim.getSimulationId());
				stmt.setBytes(1, _sim.getSimulationId().getData());
				stmt.setString(2, value);				

				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					Inventory i = new Inventory();
					i.time = rs.getInt(1);
					i.nucid = rs.getInt(2);
					i.amount = rs.getDouble(3);
					list.add(i);
				}
			}
		} catch (SQLException e) {
			log.error("getInventory2 ",e);
		} finally {
			_sim.getDataSource().releaseConnection();
		}
		
		return list;
	}
}
