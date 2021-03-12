package ietools;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import com.google.gson.Gson;

import utils.db.DBConnection;

public class LoadDocumentsCOVID
{
	private Connection conn;
	private Gson gson;
	
	public LoadDocumentsCOVID()
	{
		gson = new Gson();
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
			List<String> filterKeywords = new ArrayList<String>();
			filterKeywords = gson.fromJson(props.getProperty("filterKeywords"), filterKeywords.getClass());
			String insertQuery = props.getProperty("insertQuery");
			int numColumns = Integer.parseInt(props.getProperty("numColumns"));
			String startDateFlagStr = props.getProperty("startDateFlag");
			Boolean startDateFlag = false;
			if (startDateFlagStr != null) {
				startDateFlag = Boolean.parseBoolean(startDateFlagStr);
			}
			
			conn = DBConnection.dbConnection(user, password, host, dbName, dbType);
			conn.setAutoCommit(false);
			
			PreparedStatement pstmt = conn.prepareStatement(query);
			PreparedStatement pstmtInsert = conn.prepareStatement(insertQuery);
			
			BufferedReader reader = new BufferedReader(new FileReader(patientIDFile));
			
			String line = "";
			int batchTotal = 0;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				pstmt.setString(1, parts[0]);
				
				Date startDate = Date.valueOf(parts[1]);
				
				if (startDateFlag)
					pstmt.setDate(2, startDate);				
					
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
					
					boolean filter = false;
					
					if (flag) {
						for (String filterKeyword : filterKeywords) {
							if (text.indexOf(filterKeyword) >= 0) {
								filter = true;
								break;
							}
						}
						
						if (filter)
							continue;
						
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
			
			
			reader.close();
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
