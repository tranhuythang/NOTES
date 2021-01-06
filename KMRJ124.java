package PKMR099;

import static db.common.ER;
import static db.common.SQL_SERVER;

import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.ibm.jzos.FileFactory;

import db.CommonConnection;

public class KMRP124 {
	public static void main(String[] args) {
		try {
			kmrp124Start();			
		}
		catch(Exception e) { 
			e.printStackTrace();
		}
	}	
	
//	private static CommonConnection connection;
	private static Connection connection;
	private static Statement EqmSch_statement; 
	private static ResultSet EqmSch_result;
	private static PreparedStatement Mmd_statement;
	private static ResultSet Mmd_result;
	private static PreparedStatement Ets_statement;
	private static ResultSet Ets_result;
	private static PreparedStatement Eqt_statement;
	private static ResultSet Eqt_result;
	
	private static BufferedWriter bw;
	
	private static int ws_eqm_cnt = 0;
	private static int ws_make_found = 0;
	private static int ws_make_not_found = 0;
	private static int ws_sch_cnt = 0;
	private static int ws_sch_not_found = 0; 
	private static int ws_ets_cnt = 0;       
	private static int ws_ets_not_found = 0; 
	private static int ws_eqt_cnt = 0;       
	private static int ws_eqt_not_found = 0; 
	private static int ws_outrecs = 0;
	
	private static String w_equip_fleet     = "";   
	private static String w_equip_model     = "";
	private static String w_equip_serial    = "";
	private static String w_equip_suffix    = "";
	private static String w_make            = "";
	private static String w_class           = "";
	private static String w_category        = "";
	private static String w_dte_in_svc      = "";
	private static String w_dte_out_svc     = "";
	private static String w_plant           = "";
	private static String w_yr_mfr          = "";
	private static String w_cost_orig       = "";
	private static String w_cost_econ       = "";
	private static String w_flg_theft_recov = "";
	private static String w_cde_owner       = "";
	private static String w_master_num      = "";
	private static String w_stat            = "";
	private static String w_usge            = "";
	private static String w_cond            = "";
	private static String w_flg_sold        = "";
	private static String w_flg_conv        = "";
	private static String w_dte_status      = "";
	private static String w_loc_trans_c     = "";
	private static String w_comp_num_curr   = "";
	private static String w_dept_num_curr   = "";
	private static String w_loc_trans_p     = "";
	private static String w_comp_num_prev   = "";
	private static String w_dept_num_prev   = "";
	private static String w_doc             = "";
	private static String w_dte_trans       = "";
	private static String w_cde_trans       = "";
	private static String w_dte_major_repr  = "";
	private static String w_dte_arrive      = "";
	private static String w_dte_lst_rental  = "";
	private static String w_dte_lst_oneway  = "";
	private static int w_mlge               = 0;
	private static String w_mkt_co          = "000";
	
	private static String w_model           = " ";
	private static String w_model_version   = " ";
	
//	private static String w_comp_num_curr; 
//	private static String w_dept_num_curr; 
//	private static String w_comp_num_prev; 
//	private static String w_dept_num_prev; 
//	private static String w_doc;           
//	private static String w_dte_trans;     
//	private static String w_cde_trans;     
//	private static String w_dte_major_repr;
//	private static String w_dte_arrive;    
//	private static String w_dte_lst_rental;
//	private static String w_dte_lst_oneway;
//	
//	private static String w_mlge;
//	
//	private static String w_make;
		
	public static void kmrp124Start() throws Exception{
		ZoneId az = ZoneId.of("US/Arizona");
		ZonedDateTime azNow = ZonedDateTime.now();
		String todayDate = azNow.format(DateTimeFormatter.ofPattern("yyMMdd"));
		String tmpDate = azNow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String lstDate = azNow.minusDays(90).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		
		//connection = CommonConnection.buildConnection(SQL_SERVER, ER, "PROD");
		connection = DriverManager.getConnection("jdbc:sqlserver://opssqld01.uhaul.amerco.org:1433;DatabaseName=EquipmentRepositoryTEST", "vpoperations", "W7cv89q25x");
		
		String EqmSch_query =			
				" select * " +
				" from " +
				" 	(select distinct em.master_num as em_master_num, em.fleet as em_fleet, em.model as em_model, em.serial as em_serial, em.equip_suffix as em_equip_suffix, " + 
				"                    em.model_version as em_model_version, em.class as em_class, em.category as em_category, em.dte_in_svc as em_dte_in_svc, em.dte_out_svc as em_dte_out_svc, " + 
				"                    em.plant as em_plant, em.yr_mfr as em_yr_mfr, em.cost_orig as em_cost_orig, em.cost_econ as em_cost_econ, em.flg_theft_recov as em_flg_theft_recov, em.cde_owner as em_cde_owner, " + 
				"                    sch.master_num as sch_master_num, sch.stat, sch.usge, sch.cond, sch.flg_sold, sch.flg_conv, sch.dte_effect, sch.datetime_chng," +
				" 	                 row_number() over (partition by em.master_num order by sch.datetime_chng desc) as indexCol " +
				"	from EQUIP_MASTER em " +
				"	left join STAT_COND_HIST sch on em.master_num = sch.master_num) temptable" +
				" where indexCol = 1";
		EqmSch_statement = connection.createStatement();
		EqmSch_result = EqmSch_statement.executeQuery(EqmSch_query);
		
		String Mmd_query = "select top(1) * from MODEL_MFR_DATA where model = ? and model_version = ?";
		Mmd_statement = connection.prepareStatement(Mmd_query);		
		
		String Ets_query = "select top(1) * from EQP_TRANS_SUMRY where master_num = ?";
		Ets_statement = connection.prepareStatement(Ets_query);
		
		String Eqt_query = "select top(1) * from EQUIP_TRANS "
						+ "where master_num = ? and (convert(date, datetime_trans) = ? or convert(date, datetime_trans) = ?) "
						+ "      and cde_trans in ('LI', 'LO', 'OI', 'OL', 'OO', 'TI', 'TO') "
						+ "order by datetime_trans desc ";
		Eqt_statement = connection.prepareStatement(Eqt_query);
		
		String outputPath = "";
		if(System.getProperty("os.name").toLowerCase().equals("z/os"))			
			outputPath = "//DD:REPNVIN";							
		else			
			outputPath = "D:\\OneDrive - U-Haul International\\PKMR099_KMRP124\\output_" + azNow.format(DateTimeFormatter.ofPattern("MMdd_HHmm")) + ".txt";		
		
		bw = FileFactory.newBufferedWriter(outputPath);
		
//		String w_stat = "";
//		String w_usge = ""; 
//		String w_cond = ""; 
//		String w_flg_sold = "";
//		String w_flg_conv = "";
//		String w_dte_status = "";
//		
//		String w_equip_fleet     = " ";
//		String w_equip_model     = " ";
//		String w_equip_serial    = " ";
//		String w_equip_suffix 	  = " ";

//		String w_class           = " ";
//		String w_category        = " ";
//		String w_dte_in_svc      = " ";
//		String w_dte_out_svc     = " ";
//		String w_plant 		  = " ";
//		String w_yr_mfr 		  = " ";
//		String w_cost_orig 	  = " ";
//		String w_cost_econ 	  = " ";
//		String w_flg_theft_recov = " ";
//		String w_cde_owner 	  = " ";

		while(EqmSch_result.next()) {
			ws_eqm_cnt ++;
			//x10_reset();
			
			w_equip_fleet     = EqmSch_result.getString("em_fleet");        
			w_equip_model     = EqmSch_result.getString("em_model");        
			w_equip_serial    = EqmSch_result.getString("em_serial");      
			w_equip_suffix 	  = EqmSch_result.getString("em_equip_suffix");
			//w_model           = EqmSch_result.getString("em_model");
			w_model_version   = EqmSch_result.getString("em_model_version");    
			w_class           = EqmSch_result.getString("em_class");                    
			w_category        = EqmSch_result.getString("em_category");              
			w_dte_in_svc      = EqmSch_result.getString("em_dte_in_svc");         
			w_dte_out_svc     = EqmSch_result.getString("em_dte_out_svc");        
			w_plant 		  = EqmSch_result.getString("em_plant");                    
			w_yr_mfr 		  = EqmSch_result.getString("em_yr_mfr");                  
			w_cost_orig 	  = EqmSch_result.getString("em_cost_orig");            
			w_cost_econ 	  = EqmSch_result.getString("em_cost_econ");            
			w_flg_theft_recov = EqmSch_result.getString("em_flg_theft_recov");
			w_cde_owner 	  = EqmSch_result.getString("em_cde_owner");            
			w_master_num      = EqmSch_result.getString("em_master_num");
			
			//make sure this equipment is active
			w_stat = EqmSch_result.getString("stat");
			if(EqmSch_result.wasNull())
				ws_sch_not_found++;
			else {
				w_usge = EqmSch_result.getString("usge"); 
				w_cond = EqmSch_result.getString("cond");
				w_flg_sold = EqmSch_result.getString("flg_sold");
				w_flg_conv = EqmSch_result.getString("flg_conv");
				w_dte_status = EqmSch_result.getString("dte_effect").replace("-", "");
				
				if(!w_flg_sold.equals("TB") && !w_stat.equals("OO") && !w_stat.equals("I ") && !w_stat.equals("D ")){
					a05_get_make(w_model, w_model_version);
				    a11_read_ets(w_master_num);
				}					     
			}
		}
		if(ws_eqm_cnt == 0) {
			System.out.println("*********************************************");
			System.out.println("** no records on equipment master (eqm056) **");
			System.out.println("*********************************************");
			System.exit(-1);
		}
		
		System.out.println("** eqm056 records read:     " + ws_eqm_cnt);                    
		System.out.println("** mmd025 records read:     " + ws_make_found);                 
		System.out.println("** mmd025 records not read: " + ws_make_not_found);             
		System.out.println("** sch062 records read:     " + ws_sch_cnt);                    
		System.out.println("** sch062 records not read: " + ws_sch_not_found);
		System.out.println("** ets060 records read:     " + ws_ets_cnt);                    
		System.out.println("** ets060 records not read: " + ws_ets_not_found);              
		System.out.println("** eqt060 records read:     " + ws_eqt_cnt);                    
		System.out.println("** eqt060 records not read: " + ws_eqt_not_found);              
		System.out.println("** output records written:  " + ws_outrecs);
		
		bw.close();
		    
		EqmSch_result.close();
		EqmSch_statement.close();		
		Mmd_result.close();
		Mmd_statement.close();
		Ets_result.close();
		Ets_statement.close();		
		Eqt_result.close();   
		Eqt_statement.close();		       
		connection.close();	
	}
	
	// make sure this equipment is active
	public static void a05_get_make(String model, String model_version) throws Exception {
		Mmd_statement.setString(1, model);
		Mmd_statement.setString(2, model_version);
		Mmd_result = Mmd_statement.executeQuery();
		if(Mmd_result.next()) {
			w_make = Mmd_result.getString("make");
			ws_make_found++;
		}			
		else
			ws_make_not_found++;		                                    
	}
	
	// link the table
	public static void a11_read_ets(String master_num) throws Exception {
		Ets_statement.setString(1, master_num);
		Ets_result = Ets_statement.executeQuery();
		if(Ets_result.next()) {
			ws_ets_cnt++;
			w_loc_trans_c    = String.format("%6s", Ets_result.getString("loc_trans_c"));
			w_comp_num_curr  = w_loc_trans_c.substring(0,3); 
			w_dept_num_curr  = w_loc_trans_c.substring(3);
			w_loc_trans_p    = String.format("%6s", Ets_result.getString("loc_trans_p"));
			w_comp_num_prev  = w_loc_trans_p.substring(0,3); 
			w_dept_num_prev  = w_loc_trans_p.substring(3); 
			w_doc            = Ets_result.getString("doc_num");           
			w_dte_trans      = Ets_result.getString("dte_trans");     
			w_cde_trans      = Ets_result.getString("cde_trans");     
			w_dte_major_repr = Ets_result.getString("dte_major_repr");
			w_dte_arrive     = Ets_result.getString("dte_arrive");    
			w_dte_lst_rental = Ets_result.getString("dte_lst_rental");
			w_dte_lst_oneway = Ets_result.getString("dte_lst_oneway");
			
			if(EqmSch_result.getString("em_category").equals("TKL"))
				a20_read_eqt(w_master_num, w_dte_lst_rental, w_dte_arrive);
			else
				c10_download();
		}
		else
			ws_ets_not_found++;		
	}
	
	// check mileage truck
	public static void a20_read_eqt(String master_num, String dte_lst_rental, String dte_arrive) throws Exception {
		Eqt_statement.setString(1, master_num);
		Eqt_statement.setString(2, dte_lst_rental);
		Eqt_statement.setString(3, dte_arrive);
		Eqt_result = Eqt_statement.executeQuery();
		if(Eqt_result.next()) {
			ws_eqt_cnt++;
			w_mlge = Eqt_result.getInt("mlge");
		}
		else {
			ws_eqt_not_found++;
		}		
	}
	
	// produce rpt & download file
	private static final String format = "%4s%2s%4s%1s%3s%2s%3s%8s%8s%3s%4s%9s%9s%1s%3s%9s%-2s%-2s%2s%2s%1s%8s%3s%3s%3s%3s%8s%8s%2s%8s%8s%8s%8s%06d%3s";
	
	public static void c10_download() throws Exception{
		
		UnaryOperator<String> convertDate2Number = (date) -> date.replace("-", "").replace("19000101", "00000000");
		w_dte_in_svc     = convertDate2Number.apply(w_dte_in_svc);
		w_dte_out_svc    = convertDate2Number.apply(w_dte_out_svc);
		w_dte_trans		 = convertDate2Number.apply(w_dte_trans);
		w_dte_major_repr = convertDate2Number.apply(w_dte_major_repr);
		w_dte_arrive     = convertDate2Number.apply(w_dte_arrive);
		w_dte_lst_rental = convertDate2Number.apply(w_dte_lst_rental);
		w_dte_lst_oneway = convertDate2Number.apply(w_dte_lst_oneway);
		
		UnaryOperator<String> removeDecimalDot = (decimal) -> String.format("%9s", decimal.replace(".", "")).replace(" ", "0");
		w_cost_orig = removeDecimalDot.apply(w_cost_orig);
		w_cost_econ = removeDecimalDot.apply(w_cost_econ);
		
		bw.write(String.format(format, 
				w_equip_fleet,
				w_equip_model,    
				w_equip_serial,   
				w_equip_suffix,   
				w_make,           
				w_class,          
				w_category,       
				w_dte_in_svc,     
				w_dte_out_svc,    
				w_plant,          
				w_yr_mfr,         
				w_cost_orig,      
				w_cost_econ,      
				w_flg_theft_recov,
				w_cde_owner,      
				w_master_num,     
				w_stat,           
				w_usge,          
				w_cond,           
				w_flg_sold,       
				w_flg_conv,       
				w_dte_status,     
				w_comp_num_curr,  
				w_dept_num_curr,  
				w_comp_num_prev,  
				w_dept_num_prev,  
				w_doc,            
				w_dte_trans,      
				w_cde_trans,      
				w_dte_major_repr, 
				w_dte_arrive,     
				w_dte_lst_rental, 
				w_dte_lst_oneway, 
				w_mlge,           
				w_mkt_co) + "\n");
		ws_outrecs++;
	}
}s