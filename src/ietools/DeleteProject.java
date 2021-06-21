package ietools;

import java.sql.*;

import utils.db.DBConnection;

public class DeleteProject
{
	private Connection conn;
	private String schema;
	
	public DeleteProject()
	{
	}
	
	public void setSchema(String schema)
	{
		this.schema = schema + ".";
	}
	
	public void init(String user, String password, String host, String dbName, String dbType)
	{
		try {
			conn = DBConnection.dbConnection(user, password, host, dbName, dbType);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try {
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void deleteProject(String projectName, String dbType)
	{
		try {
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery("select project_id from " + schema + "project where name = '" + projectName + "'");
			int projID = -1;
			if (rs.next()) {
				projID = rs.getInt(1);
			}
			
			//rs = stmt.executeQuery("select frame_instance_id from " + schema + "project_frame_instance where project_id = " + projID);
			stmt.execute("delete from " + schema + "frame_instance where frame_instance_id in "
				+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_document where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_data where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_data_history where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_data_history2 where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_document_history where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_element_repeat where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_lock where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_order where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_section_repeat where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "frame_instance_status where frame_instance_id in "
					+ "(select distinct a.frame_instance_id from project_frame_instance a where a.project_id = " + projID + ")");
			
			stmt.execute("delete from " + schema + "project_preload where project_id = " + projID);
			
			stmt.execute("delete from " + schema + "user_project where project_id = " + projID);
			
			stmt.execute("delete from " + schema + "crf_project where project_id = " + projID);
			
			stmt.execute("delete from project where project_id = " + projID);
			
			stmt.execute("delete from " + schema + "project_frame_instance where project_id = " + projID);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void deleteCRF(String crfName)
	{
		try {
			Statement stmt = conn.createStatement();
			
			int crfID = -1;
			int frameID = -1;
			ResultSet rs = stmt.executeQuery("select crf_id, frame_id from " + schema + "crf where crf_name = " + crfName); 
				
			if (rs.next()) {
				crfID = rs.getInt(1);
				frameID = rs.getInt(2);
			}
			
			StringBuilder strBlder = new StringBuilder();
			rs = stmt.executeQuery("select project_id from " + schema + "crf_project where crf_id = " + crfID);
			while (rs.next()) {
				strBlder.append(rs.getInt(1) + ", ");
			}
			
			if (strBlder.length() > 0) {
				System.out.println("CRF: " + crfName + " is associated with projects: " + strBlder.toString());
				return;
			}
			
			else {
				//delete CRF
				//stmt.execute("delete from " + schema + "crf_project where crf_id = " + crfID);
				//stmt.execute("delete from " + schema + "crf_element where crf_id = " + crfID);
				stmt.execute("delete from " + schema + "crf_section where crf_id = " + crfID);
				
				stmt.execute("delete from " + schema + "value where value_id in "
					+ "(select a.value_id from " + schema + "element_value a, crf_element b where b.crf_id = " + crfID 
					+ " and a.element_id = b.element_id)");
				
				stmt.execute("delete from " + schema + "element where element_id in "
					+ "(select a.element_id from " + schema + "crf_element a where a.crf_id = " + crfID + ")");
				
				stmt.execute("delete from " + schema + "element_value where element_id in "
						+ "(select a.element_id from " + schema + "crf_element a where a.crf_id = " + crfID + ")");
				
				stmt.execute("delete from " + schema + "crf_element where crf_id = " + crfID);
				
				stmt.execute("delete from " + schema + "crf where crf_id = " + crfID);
				
				
				//delete frame
				stmt.execute("delete from " + schema + "frame where frame_id = " + frameID);
				
				stmt.execute("delete from " + schema + "slot where slot_id in "
					+ "(select a.slot_id from " + schema + "frame_slot a where a.frame_id = " + frameID + ")");
	
				stmt.execute("delete from " + schema + "frame_slot where frame_id = " + frameID);
				//stmt.execute("delete from " + schema + "frame_section where frame_id = " + frameID);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		if (args.length != 8) {
			System.out.println("usage: user password host dbName dbType schema project/crf projName/crfName");
			System.exit(0);
		}
		
		DeleteProject delete = new DeleteProject();
		delete.setSchema(args[5]);
		delete.init(args[0], args[1], args[2], args[3], args[4]);
		if (args[6].equals("project"))
			delete.deleteProject(args[7], args[4]);
		else
			delete.deleteCRF(args[7]);
	}
}
