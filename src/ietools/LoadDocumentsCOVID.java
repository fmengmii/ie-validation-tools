package ietools;

import java.io.*;
import java.sql.*;
import java.util.*;

import utils.db.DBConnection;

public class LoadDocumentsCOVID
{
	private Connection conn;
	
	public LoadDocumentsCOVID()
	{
	}
	
	public void loadDocs(String user, String password, String config)
	{
		try {
			Properties props = new Properties();
			props.load(new FileReader(config));
			
			String host = props.getProperty("host");
			String dbName = props.getProperty("dbName");
			String dbType = props.getProperty("dbType");
			String patientIDFile = props.getProperty("patientIDFile");
			String query = props.getProperty("query");
			String textColName = props.getProperty("textColName");
			String keyword = props.getProperty("keyword").toLowerCase();
			String insertQuery = props.getProperty("insertQuery");
			int numColumns = Integer.parseInt(props.getProperty("numColumns"));
			
			conn = DBConnection.dbConnection(user, password, host, dbName, dbType);
			conn.setAutoCommit(false);
			
			PreparedStatement pstmt = conn.prepareStatement(query);
			PreparedStatement pstmtInsert = conn.prepareStatement(insertQuery);
			
			BufferedReader reader = new BufferedReader(new FileReader(patientIDFile));
			
			String line = "";
			int batchTotal = 0;
			while ((line = reader.readLine()) != null) {
				pstmt.setString(1, line);
				
				ResultSet rs = pstmt.executeQuery();
				
				System.out.println("PatientSID: " + line);
				
				boolean flag = false;
				while (rs.next()) {
					String text = rs.getString(textColName);
					if (text == null)
						continue;
					
					text = text.toLowerCase();
					if (text.indexOf(keyword) >= 0) {
						flag = true;
					}
					
					if (flag) {
						for (int i=1; i<=numColumns; i++)
							pstmtInsert.setObject(i, rs.getObject(i));
						
						pstmtInsert.addBatch();
						batchTotal++;
						
						if (batchTotal > 500) {
							pstmtInsert.executeBatch();
							conn.commit();
							batchTotal = 0;
						}
					}
				}
			}	
			
			pstmtInsert.executeBatch();
			conn.commit();
			
			pstmtInsert.close();
			pstmt.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		if (args.length != 3) {
			System.out.println("usage: user password config");
			System.exit(0);
		}
		
		LoadDocumentsCOVID load = new LoadDocumentsCOVID();
		load.loadDocs(args[0], args[1], args[2]);
	}
}
