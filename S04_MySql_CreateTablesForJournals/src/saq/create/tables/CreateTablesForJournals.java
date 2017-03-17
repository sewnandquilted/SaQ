package saq.create.tables;
//  Can not issue data manipulation statements with executeQuery().

//STEP 1. Import required packages
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class CreateTablesForJournals {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://192.168.178.235:3306/SaQ?autoReconnect=true&useSSL=false";
	static final String USER = "geoff2";
	static final String PASS = "talk22me";
	static String ejLineTablename    = "EJLine";
	static String ejTxnTablename     = "EJTxn";
	static String ejFile001Tablename = "File001";
	static String ejFile002Tablename = "File002";
	static String ejFile005Tablename = "File005";
	static String ejFile020Tablename = "File020";

	public static void main(String[] args) {

		Connection conn = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query

			dropCreateEJLine(conn);
			dropCreateEJTxn(conn);
			dropCreateFile001(conn);
			dropCreateFile002(conn);
			dropCreateFile005(conn);
			dropCreateFile020(conn);

			// STEP 6: Clean-up environment
			// System.out.println("Clean up environment...");
			conn.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		}
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
		} // end try
		System.out.println("Goodbye!");
	}// end main

	private static void dropCreateFile001(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		String sql = "drop table if exists " + ejFile001Tablename;

		int rs = stmt.executeUpdate(sql);
		System.out.println("Executed drop " + ejFile001Tablename + ", result= " + rs);

		sql = "CREATE TABLE " + ejFile001Tablename + " (" 
				+"f1MACHINE text, "
				+"f1REPORT text, "
				+"f1FILE text, "
				+"f1MODE text, "
				+"f1Z_COUNTER numeric(8),"
				+"f1DATE date,"
				+"f1TIME time,"
				+"GROSSCount numeric(10,2), GROSSAmount numeric(10,2), "
				+"NETCount numeric(10,2), NETAmount numeric(10,2), "
				+"CashInDrawCount numeric(10,2), CashInDrawAmount numeric(10,2), "
				+"CHIDCount numeric(10,2), CHIDAmount numeric(10,2), "
				+"CKIDCount numeric(10,2), CKIDAmount numeric(10,2), "
				+"CRID_1Count numeric(10,2), CRID_1Amount numeric(10,2), "
				+"CRID_2Count numeric(10,2), CRID_2Amount numeric(10,2), "
				+"CRID_3Count numeric(10,2), CRID_3Amount numeric(10,2), "
				+"CRID_4Count numeric(10,2), CRID_4Amount numeric(10,2), "
				+"CAID2Count numeric(10,2), CAID2Amount numeric(10,2), "
				+"CHID2Count numeric(10,2), CHID2Amount numeric(10,2), "
				+"CKID2Count numeric(10,2), CKID2Amount numeric(10,2), "
				+"CRID2_1Count numeric(10,2), CRID2_1Amount numeric(10,2), "
				+"CRID2_2Count numeric(10,2), CRID2_2Amount numeric(10,2), "
				+"CRID2_3Count numeric(10,2), CRID2_3Amount numeric(10,2), "
				+"CRID2_4Count numeric(10,2), CRID2_4Amount numeric(10,2), "
				+"RFCount numeric(10,2), RFAmount numeric(10,2), "
				+"CUSTCount numeric(10,2), CUSTAmount numeric(10,2), "
				+"AVRGCount numeric(10,2), AVRGAmount numeric(10,2), "
				+"C_1Count numeric(10,2), C_1Amount numeric(10,2), "
				+"C_2Count numeric(10,2), C_2Amount numeric(10,2), "
				+"CECA1Count numeric(10,2), CECA1Amount numeric(10,2), "
				+"CECK1Count numeric(10,2), CECK1Amount numeric(10,2), "
				+"CECA2Count numeric(10,2), CECA2Amount numeric(10,2), "
				+"CECK2Count numeric(10,2), CECK2Amount numeric(10,2), "
				+"DCCount numeric(10,2), DCAmount numeric(10,2), "
				+"COUPONCount numeric(10,2), COUPONAmount numeric(10,2), "
				+"RefundCount numeric(10,2), RefundAmount numeric(10,2), "
				+"ROUND1Count numeric(10,2), ROUND1Amount numeric(10,2), "
				+"ROUND2Count numeric(10,2), ROUND2Amount numeric(10,2), "
				+"CANCELCount numeric(10,2), CANCELAmount numeric(10,2), "
				+"DECLACount numeric(10,2), DECLAAmount numeric(10,2), "
				+"TAXABLE_AMTCount numeric(10,2), TAXABLE_AMTAmount numeric(10,2), "
				+"GST_IncludedCount numeric(10,2), GST_IncludedAmount numeric(10,2), "
				+"TA2Count numeric(10,2), TA2Amount numeric(10,2), "
				+"TX2Count numeric(10,2), TX2Amount numeric(10,2), "
				+"TA3Count numeric(10,2), TA3Amount numeric(10,2), "
				+"TX3Count numeric(10,2), TX3Amount numeric(10,2), "
				+"TA4Count numeric(10,2), TA4Amount numeric(10,2), "
				+"TX4Count numeric(10,2), TX4Amount numeric(10,2), "
				+"NON_TAXCount numeric(10,2), NON_TAXAmount numeric(10,2) "
				+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1";

		rs = stmt.executeUpdate(sql);
		System.out.println("Executed Create Table " + ejFile001Tablename + ", result= " + rs);

		// STEP 6: Clean-up environment
		// System.out.println("Clean up environment...");
		stmt.close();		
	}

	private static void dropCreateFile002(Connection conn) throws SQLException {
		System.out.println("Creating statement...");
		Statement stmt = conn.createStatement();
		String sql;
		sql = "drop table if exists " + ejFile002Tablename;
		int rs = stmt.executeUpdate(sql);

		System.out.println("Executed drop " + ejFile002Tablename + ", result= " + rs);

		sql = "CREATE TABLE " + ejFile002Tablename + " (" 
				+"f2MACHINE text, "
				+"f2REPORT text, "
				+"f2FILE text, "
				+"f2MODE text, "
				+"f2Z_COUNTER numeric(8),"
				+"f2DATE date,"
				+"f2TIME time,"
				+"CashCount numeric(10), CashAmount numeric(10,2), "
				+"SubtotalCount numeric(10), SubtotalAmount numeric(10,2), "
				+"ChargeCount numeric(10), ChargeAmount numeric(10,2), "
				+"ChequeCount numeric(10), ChequeAmount numeric(10,2), "
				+"HelpCount numeric(10), HelpAmount numeric(10,2), "
				+"TAX_PGMCount numeric(10), TAX_PGMAmount numeric(10,2), "
				+"PaidOutCount numeric(10), PaidOutAmount numeric(10,2), "
				+"ReceiveCashNoSaleCount numeric(10), ReceiveCashNoSaleAmount numeric(10,2), "
				+"ClerkSignOffCount numeric(10), ClerkSignOffAmount numeric(10,2), "
				+"ClerkNoCount numeric(10), ClerkNoAmount numeric(10,2), "
				+"ClerkShiftNoCount numeric(10), ClerkShiftNoAmount numeric(10,2), "
				+"DWNCount numeric(10), DWNAmount numeric(10,2), "
				+"DiscountPCTCount numeric(10), DiscountPCTAmount numeric(10,2), "
				+"PriceReductionCount numeric(10), PriceReductionAmount numeric(10,2), "
				+"PLUCount numeric(10), PLUAmount numeric(10,2), "
				+"PRCCount numeric(10), PRCAmount numeric(10,2), "
				+"ReceiptIssuedCount numeric(10), ReceiptIssuedAmount numeric(10,2), "
				+"CorrectionCount numeric(10), CorrectionAmount numeric(10,2), "
				+"RefundCount numeric(10), RefundAmount numeric(10,2), "
				+"OPENCount numeric(10), OPENAmount numeric(10,2), "
				+"RCTCount numeric(10), RCTAmount numeric(10,2), "
				+"ZeroCount numeric(10), ZeroAmount numeric(10,2), "
				+"DotCount numeric(10), DotAmount numeric(10,2), "
				+"VATCount numeric(10), VATAmount numeric(10,2), "
				+"NoSaleCount numeric(10), NoSaleAmount numeric(10,2), "
				+"CouponCount numeric(10), CouponAmount numeric(10,2), "
				+"UPCount numeric(10), UPAmount numeric(10,2), "
				+"CECount numeric(10), CEAmount numeric(10,2), "
				+"XCount numeric(10), XAmount numeric(10,2), "
				+"IncludeNonSaleReferenceCount numeric(10), IncludeNonSaleReferenceAmount numeric(10,2), "
				+"Hash_NSCount numeric(10), Hash_NSAmount numeric(10,2), "
				+"VOIDCount numeric(10), VOIDAmount numeric(10,2) "
				+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1";

		rs = stmt.executeUpdate(sql);
		System.out.println("Executed Create Table " + ejFile002Tablename + ", result= " + rs);
		stmt.close();

		
	}
	private static void dropCreateFile005(Connection conn) throws SQLException {
		System.out.println("Creating statement...");
		Statement stmt = conn.createStatement();
		String sql;
		sql = "drop table if exists " + ejFile005Tablename;
		int rs = stmt.executeUpdate(sql);

		System.out.println("Executed drop " + ejFile005Tablename + ", result= " + rs);

		sql = "CREATE TABLE " + ejFile005Tablename + " (" 
				+"f5MACHINE text, "
				+"f5REPORT text, "
				+"f5FILE text, "
				+"f5MODE text, "
				+"f5Z_COUNTER numeric(8),"
				+"f5DATE date,"
				+"f5TIME time,"
				+"FabricCount numeric(10), FabricAmount numeric(10,2), "
				+"WorkshopCount numeric(10), WorkshopAmount numeric(10,2), "
				+"SaleFabricCount numeric(10), SaleFabricAmount numeric(10,2), "
				+"GiftCertCount numeric(10), GiftCertAmount numeric(10,2), "
				+"Dept005Count numeric(10), Dept005Amount numeric(10,2), "
				+"NotionCount numeric(10), NotionAmount numeric(10,2), "
				+"BOMCount numeric(10), BOMAmount numeric(10,2), "
				+"ThreadsCount numeric(10), ThreadsAmount numeric(10,2), "
				+"Dept009Count numeric(10), Dept009Amount numeric(10,2), "
				+"DEPT010Count numeric(10), DEPT010Amount numeric(10,2), "
				+"BookCount numeric(10), BookAmount numeric(10,2), "
				+"PostageCount numeric(10), PostageAmount numeric(10,2), "
				+"KitsCount numeric(10), KitsAmount numeric(10,2), "
				+"DEPT014Count numeric(10), DEPT014Amount numeric(10,2), "
				+"DEPT015Count numeric(10), DEPT015Amount numeric(10,2), "
				+"ClassCount numeric(10), ClassAmount numeric(10,2), "
				+"LongArmCount numeric(10), LongArmAmount numeric(10,2), "
				+"PatternsCount numeric(10), PatternsAmount numeric(10,2), "
				+"DEPT019Count numeric(10), DEPT019Amount numeric(10,2), "
				+"PatternCount numeric(10), PatternAmount numeric(10,2), "
				+"JanomeCount numeric(10), JanomeAmount numeric(10,2), "
				+"BattingCount numeric(10), BattingAmount numeric(10,2), "
				+"MiscCount numeric(10), MiscAmount numeric(10,2), "
				+"DEPT024Count numeric(10), DEPT024Amount numeric(10,2), "
				+"Dept025Count numeric(10), Dept025Amount numeric(10,2), "
				+"DEPT026Count numeric(10), DEPT026Amount numeric(10,2), "
				+"DEPT027Count numeric(10), DEPT027Amount numeric(10,2), "
				+"DEPT028Count numeric(10), DEPT028Amount numeric(10,2), "
				+"DEPT029Count numeric(10), DEPT029Amount numeric(10,2), "
				+"DEPT030Count numeric(10), DEPT030Amount numeric(10,2), "
				+"DEPT031Count numeric(10), DEPT031Amount numeric(10,2), "
				+"DEPT032Count numeric(10), DEPT032Amount numeric(10,2), "
				+"DEPT033Count numeric(10), DEPT033Amount numeric(10,2), "
				+"DEPT034Count numeric(10), DEPT034Amount numeric(10,2), "
				+"DEPT035Count numeric(10), DEPT035Amount numeric(10,2), "
				+"DEPT036Count numeric(10), DEPT036Amount numeric(10,2), "
				+"DEPT037Count numeric(10), DEPT037Amount numeric(10,2), "
				+"DEPT038Count numeric(10), DEPT038Amount numeric(10,2), "
				+"DEPT039Count numeric(10), DEPT039Amount numeric(10,2), "
				+"DEPT040Count numeric(10), DEPT040Amount numeric(10,2), "
				+"DEPT041Count numeric(10), DEPT041Amount numeric(10,2), "
				+"DEPT042Count numeric(10), DEPT042Amount numeric(10,2), "
				+"DEPT043Count numeric(10), DEPT043Amount numeric(10,2), "
				+"DEPT044Count numeric(10), DEPT044Amount numeric(10,2), "
				+"DEPT045Count numeric(10), DEPT045Amount numeric(10,2), "
				+"DEPT046Count numeric(10), DEPT046Amount numeric(10,2), "
				+"DEPT047Count numeric(10), DEPT047Amount numeric(10,2), "
				+"DEPT048Count numeric(10), DEPT048Amount numeric(10,2), "
				+"DEPT049Count numeric(10), DEPT049Amount numeric(10,2), "
				+"DEPT050Count numeric(10), DEPT050Amount numeric(10,2), "
				+"DEPT051Count numeric(10), DEPT051Amount numeric(10,2), "
				+"DEPT052Count numeric(10), DEPT052Amount numeric(10,2), "
				+"DEPT053Count numeric(10), DEPT053Amount numeric(10,2), "
				+"DEPT054Count numeric(10), DEPT054Amount numeric(10,2), "
				+"DEPT055Count numeric(10), DEPT055Amount numeric(10,2), "
				+"DEPT056Count numeric(10), DEPT056Amount numeric(10,2), "
				+"DEPT057Count numeric(10), DEPT057Amount numeric(10,2), "
				+"DEPT058Count numeric(10), DEPT058Amount numeric(10,2), "
				+"DEPT059Count numeric(10), DEPT059Amount numeric(10,2), "
				+"DEPT060Count numeric(10), DEPT060Amount numeric(10,2), "
				+"DEPT061Count numeric(10), DEPT061Amount numeric(10,2), "
				+"DEPT062Count numeric(10), DEPT062Amount numeric(10,2), "
				+"DEPT063Count numeric(10), DEPT063Amount numeric(10,2), "
				+"DEPT064Count numeric(10), DEPT064Amount numeric(10,2), "
				+"DEPT065Count numeric(10), DEPT065Amount numeric(10,2), "
				+"DEPT066Count numeric(10), DEPT066Amount numeric(10,2), "
				+"DEPT067Count numeric(10), DEPT067Amount numeric(10,2), "
				+"DEPT068Count numeric(10), DEPT068Amount numeric(10,2), "
				+"DEPT069Count numeric(10), DEPT069Amount numeric(10,2), "
				+"DEPT070Count numeric(10), DEPT070Amount numeric(10,2), "
				+"DEPT071Count numeric(10), DEPT071Amount numeric(10,2), "
				+"DEPT072Count numeric(10), DEPT072Amount numeric(10,2), "
				+"DEPT073Count numeric(10), DEPT073Amount numeric(10,2), "
				+"DEPT074Count numeric(10), DEPT074Amount numeric(10,2), "
				+"DEPT075Count numeric(10), DEPT075Amount numeric(10,2), "
				+"DEPT076Count numeric(10), DEPT076Amount numeric(10,2), "
				+"DEPT077Count numeric(10), DEPT077Amount numeric(10,2), "
				+"DEPT078Count numeric(10), DEPT078Amount numeric(10,2), "
				+"DEPT079Count numeric(10), DEPT079Amount numeric(10,2), "
				+"DEPT080Count numeric(10), DEPT080Amount numeric(10,2), "
				+"DEPT081Count numeric(10), DEPT081Amount numeric(10,2), "
				+"DEPT082Count numeric(10), DEPT082Amount numeric(10,2), "
				+"DEPT083Count numeric(10), DEPT083Amount numeric(10,2), "
				+"DEPT084Count numeric(10), DEPT084Amount numeric(10,2), "
				+"DEPT085Count numeric(10), DEPT085Amount numeric(10,2), "
				+"DEPT086Count numeric(10), DEPT086Amount numeric(10,2), "
				+"DEPT087Count numeric(10), DEPT087Amount numeric(10,2), "
				+"DEPT088Count numeric(10), DEPT088Amount numeric(10,2), "
				+"DEPT089Count numeric(10), DEPT089Amount numeric(10,2), "
				+"DEPT090Count numeric(10), DEPT090Amount numeric(10,2), "
				+"DEPT091Count numeric(10), DEPT091Amount numeric(10,2), "
				+"DEPT092Count numeric(10), DEPT092Amount numeric(10,2), "
				+"DEPT093Count numeric(10), DEPT093Amount numeric(10,2), "
				+"DEPT094Count numeric(10), DEPT094Amount numeric(10,2), "
				+"DEPT095Count numeric(10), DEPT095Amount numeric(10,2), "
				+"DEPT096Count numeric(10), DEPT096Amount numeric(10,2), "
				+"DEPT097Count numeric(10), DEPT097Amount numeric(10,2), "
				+"DEPT098Count numeric(10), DEPT098Amount numeric(10,2), "
				+"DEPT099Count numeric(10), DEPT099Amount numeric(10,2), "
				+"DEPT100Count numeric(10), DEPT100Amount numeric(10,2), "
				+"DEPT101Count numeric(10), DEPT101Amount numeric(10,2), "
				+"DEPT102Count numeric(10), DEPT102Amount numeric(10,2), "
				+"DEPT103Count numeric(10), DEPT103Amount numeric(10,2), "
				+"DEPT104Count numeric(10), DEPT104Amount numeric(10,2), "
				+"DEPT105Count numeric(10), DEPT105Amount numeric(10,2), "
				+"DEPT106Count numeric(10), DEPT106Amount numeric(10,2), "
				+"DEPT107Count numeric(10), DEPT107Amount numeric(10,2), "
				+"DEPT108Count numeric(10), DEPT108Amount numeric(10,2), "
				+"DEPT109Count numeric(10), DEPT109Amount numeric(10,2), "
				+"DEPT110Count numeric(10), DEPT110Amount numeric(10,2), "
				+"DEPT111Count numeric(10), DEPT111Amount numeric(10,2), "
				+"DEPT112Count numeric(10), DEPT112Amount numeric(10,2), "
				+"DEPT113Count numeric(10), DEPT113Amount numeric(10,2), "
				+"DEPT114Count numeric(10), DEPT114Amount numeric(10,2), "
				+"DEPT115Count numeric(10), DEPT115Amount numeric(10,2), "
				+"DEPT116Count numeric(10), DEPT116Amount numeric(10,2), "
				+"DEPT117Count numeric(10), DEPT117Amount numeric(10,2), "
				+"DEPT118Count numeric(10), DEPT118Amount numeric(10,2), "
				+"DEPT119Count numeric(10), DEPT119Amount numeric(10,2), "
				+"DEPT120Count numeric(10), DEPT120Amount numeric(10,2), "
				+"DEPT121Count numeric(10), DEPT121Amount numeric(10,2), "
				+"DEPT122Count numeric(10), DEPT122Amount numeric(10,2), "
				+"DEPT123Count numeric(10), DEPT123Amount numeric(10,2), "
				+"DEPT124Count numeric(10), DEPT124Amount numeric(10,2), "
				+"DEPT125Count numeric(10), DEPT125Amount numeric(10,2), "
				+"DEPT126Count numeric(10), DEPT126Amount numeric(10,2), "
				+"DEPT127Count numeric(10), DEPT127Amount numeric(10,2), "
				+"DEPT128Count numeric(10), DEPT128Amount numeric(10,2), "
				+"DEPT129Count numeric(10), DEPT129Amount numeric(10,2), "
				+"DEPT130Count numeric(10), DEPT130Amount numeric(10,2), "
				+"DEPT131Count numeric(10), DEPT131Amount numeric(10,2), "
				+"DEPT132Count numeric(10), DEPT132Amount numeric(10,2), "
				+"DEPT133Count numeric(10), DEPT133Amount numeric(10,2), "
				+"DEPT134Count numeric(10), DEPT134Amount numeric(10,2), "
				+"DEPT135Count numeric(10), DEPT135Amount numeric(10,2), "
				+"DEPT136Count numeric(10), DEPT136Amount numeric(10,2), "
				+"DEPT137Count numeric(10), DEPT137Amount numeric(10,2), "
				+"DEPT138Count numeric(10), DEPT138Amount numeric(10,2), "
				+"DEPT139Count numeric(10), DEPT139Amount numeric(10,2), "
				+"DEPT140Count numeric(10), DEPT140Amount numeric(10,2), "
				+"DEPT141Count numeric(10), DEPT141Amount numeric(10,2), "
				+"DEPT142Count numeric(10), DEPT142Amount numeric(10,2), "
				+"DEPT143Count numeric(10), DEPT143Amount numeric(10,2), "
				+"DEPT144Count numeric(10), DEPT144Amount numeric(10,2), "
				+"DEPT145Count numeric(10), DEPT145Amount numeric(10,2), "
				+"DEPT146Count numeric(10), DEPT146Amount numeric(10,2), "
				+"DEPT147Count numeric(10), DEPT147Amount numeric(10,2), "
				+"DEPT148Count numeric(10), DEPT148Amount numeric(10,2), "
				+"DEPT149Count numeric(10), DEPT149Amount numeric(10,2), "
				+"DEPT150Count numeric(10), DEPT150Amount numeric(10,2), "
				+"DEPT151Count numeric(10), DEPT151Amount numeric(10,2), "
				+"DEPT152Count numeric(10), DEPT152Amount numeric(10,2), "
				+"DEPT153Count numeric(10), DEPT153Amount numeric(10,2), "
				+"DEPT154Count numeric(10), DEPT154Amount numeric(10,2), "
				+"DEPT155Count numeric(10), DEPT155Amount numeric(10,2), "
				+"DEPT156Count numeric(10), DEPT156Amount numeric(10,2), "
				+"DEPT157Count numeric(10), DEPT157Amount numeric(10,2), "
				+"DEPT158Count numeric(10), DEPT158Amount numeric(10,2), "
				+"DEPT159Count numeric(10), DEPT159Amount numeric(10,2), "
				+"DEPT160Count numeric(10), DEPT160Amount numeric(10,2), "
				+"DEPT161Count numeric(10), DEPT161Amount numeric(10,2), "
				+"DEPT162Count numeric(10), DEPT162Amount numeric(10,2), "
				+"DEPT163Count numeric(10), DEPT163Amount numeric(10,2), "
				+"DEPT164Count numeric(10), DEPT164Amount numeric(10,2), "
				+"DEPT165Count numeric(10), DEPT165Amount numeric(10,2), "
				+"DEPT166Count numeric(10), DEPT166Amount numeric(10,2), "
				+"DEPT167Count numeric(10), DEPT167Amount numeric(10,2), "
				+"DEPT168Count numeric(10), DEPT168Amount numeric(10,2), "
				+"DEPT169Count numeric(10), DEPT169Amount numeric(10,2), "
				+"DEPT170Count numeric(10), DEPT170Amount numeric(10,2), "
				+"DEPT171Count numeric(10), DEPT171Amount numeric(10,2), "
				+"DEPT172Count numeric(10), DEPT172Amount numeric(10,2), "
				+"DEPT173Count numeric(10), DEPT173Amount numeric(10,2), "
				+"DEPT174Count numeric(10), DEPT174Amount numeric(10,2), "
				+"DEPT175Count numeric(10), DEPT175Amount numeric(10,2), "
				+"DEPT176Count numeric(10), DEPT176Amount numeric(10,2), "
				+"DEPT177Count numeric(10), DEPT177Amount numeric(10,2), "
				+"DEPT178Count numeric(10), DEPT178Amount numeric(10,2), "
				+"DEPT179Count numeric(10), DEPT179Amount numeric(10,2), "
				+"DEPT180Count numeric(10), DEPT180Amount numeric(10,2), "
				+"DEPT181Count numeric(10), DEPT181Amount numeric(10,2), "
				+"DEPT182Count numeric(10), DEPT182Amount numeric(10,2), "
				+"DEPT183Count numeric(10), DEPT183Amount numeric(10,2), "
				+"DEPT184Count numeric(10), DEPT184Amount numeric(10,2), "
				+"DEPT185Count numeric(10), DEPT185Amount numeric(10,2), "
				+"DEPT186Count numeric(10), DEPT186Amount numeric(10,2), "
				+"DEPT187Count numeric(10), DEPT187Amount numeric(10,2), "
				+"DEPT188Count numeric(10), DEPT188Amount numeric(10,2), "
				+"DEPT189Count numeric(10), DEPT189Amount numeric(10,2), "
				+"DEPT190Count numeric(10), DEPT190Amount numeric(10,2), "
				+"DEPT191Count numeric(10), DEPT191Amount numeric(10,2), "
				+"DEPT192Count numeric(10), DEPT192Amount numeric(10,2), "
				+"DEPT193Count numeric(10), DEPT193Amount numeric(10,2), "
				+"DEPT194Count numeric(10), DEPT194Amount numeric(10,2), "
				+"DEPT195Count numeric(10), DEPT195Amount numeric(10,2), "
				+"DEPT196Count numeric(10), DEPT196Amount numeric(10,2), "
				+"DEPT197Count numeric(10), DEPT197Amount numeric(10,2), "
				+"DEPT198Count numeric(10), DEPT198Amount numeric(10,2), "
				+"DEPT199Count numeric(10), DEPT199Amount numeric(10,2), "
				+"DEPT200Count numeric(10), DEPT200Amount numeric(10,2) "
				+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1";
		rs = stmt.executeUpdate(sql);
		System.out.println("Executed Create Table " + ejFile002Tablename + ", result= " + rs);
		stmt.close();

		

		
	}

	private static void dropCreateFile020(Connection conn) throws SQLException {
		System.out.println("Creating statement...");
		Statement stmt = conn.createStatement();
		String sql;
		sql = "drop table if exists " + ejFile020Tablename;
		int rs = stmt.executeUpdate(sql);

		System.out.println("Executed drop " + ejFile020Tablename + ", result= " + rs);

		sql = "CREATE TABLE " + ejFile020Tablename + " (" 
					+"f20MODEL text, "
					+"f20MACHINE text, "
					+"f20FILE text, "
					+"f20MODE text, "
					+"f20Z_COUNTER numeric(8) not null primary key,"
					+"f20DATE date,"
					+"f20TIME time,"
					+"GrandTotal numeric(10,2), "
					+"PrevGrandTotal numeric(10,2) "
					+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1";
		rs = stmt.executeUpdate(sql);
		System.out.println("Executed Create Table " + ejFile020Tablename + ", result= " + rs);
		stmt.close();
	}

	private static void dropCreateEJTxn(Connection conn) throws SQLException {
		// TODO Auto-generated method stub
		Statement stmt = conn.createStatement();
		String sql = "drop table if exists " + ejFile002Tablename;

		int rs = stmt.executeUpdate(sql);
		System.out.println("Executed drop " + ejFile002Tablename + ", result= " + rs);
		if (rs>0) System.exit(0);

		sql = "CREATE TABLE " + ejFile002Tablename + " (" + " PaymentDateTime timestamp not null, " + " PaymentID numeric (10) not null, "
				+ " PaymentTotal  numeric (8,2), " + " PaymentIsCharge boolean, " + " PaymentIsCash  boolean, "
				+ " PaymentIsCoupon boolean, " + " PaymentIsUnknown  boolean, "
				+ " PaymentDiscountPCT  numeric (8,2), " + " PaymentDiscount  numeric (8,2), "
				+ " PaymentRounding  numeric (8,2), "
						+ "primary key (PaymentDateTime, PaymentID) "
							+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";

		rs = stmt.executeUpdate(sql);
		System.out.println("Executed Create Table " + ejTxnTablename + ", result= " + rs);
		if (rs>0) System.exit(0);
		// STEP 6: Clean-up environment
		// System.out.println("Clean up environment...");
		stmt.close();

	}

	private static void dropCreateEJLine(Connection conn) throws SQLException {
		// TODO Auto-generated method stub
		System.out.println("Creating statement...");
		Statement stmt = conn.createStatement();
		String sql = "drop table if exists " + ejLineTablename;
		int rs = stmt.executeUpdate(sql);
		System.out.println("Executed drop " + ejLineTablename + ", result= " + rs);

		sql = "CREATE TABLE " + ejLineTablename + " (" + "LineDateTime timestamp not null, " + "ID numeric not null , "
				+ "LineNumber numeric not null, " + "Quantity numeric(8,2), " + "Category text, "
				+ "Amount  numeric(8,2), " + "DiscountPCT  numeric(8,2), " + "Discount  numeric(8,2), "
				+ " primary key (LineDateTime, ID, LineNumber) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
		rs = stmt.executeUpdate(sql);
		System.out.println("Executed Create Table " + ejLineTablename + ", result= " + rs);
		stmt.close();
	}
}// end FirstExample