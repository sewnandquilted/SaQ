package processCASIOJournals;

//    Clean the transactions afterwards, for all lines which have been voided.
//        That is, date/time/line#/category all the same, amount = - amount

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JFileChooser;

public class ProcessCASIOJournals {
	
	static String ejLineTablename    = "EJLine";
	static String ejTxnTablename     = "EJTxn";
	static String ejFile001Tablename = "File001";
	static String ejFile002Tablename = "File002";
	static String ejFile005Tablename = "File005";
	static String ejFile020Tablename = "File020";


	// flags for indicating a table has been cleared
	static boolean JournalIsTruncated = false;
	private static String journalTxnDate;
	private static String journalTxnTime;
	private static int journalTxnID;

	private static int JournalTxnLineNumber;
	private static float JournalTxnLineQuantity;
	private static String JournalTxnLineCategory;
	private static float JournalTxnLineAmount;
	private static float JournalTxnLineDiscountPCT; // mutually exclusive.
													// Either % or $$ reduction
	private static float JournalTxnLineDiscount;

	// private static float JournalTxnPayLine;
	private static float JournalTxnPaymentTotal;
	private static boolean JournalTxnPaymentCHARGE;
	private static boolean JournalTxnPaymentCASH;
	private static boolean JournalTxnPaymentCOUPON;
	private static float JournalTxnPaymentDiscountPCT;
	private static float JournalTxnPaymentDiscount;
	private static float JournalTxnPaymentRounding;

	private static float LineAmountTotal;
	private static boolean TxnSubtotalled = false;
	private static int mismatchCount = 0;
	private static int lineCount = 0;
	private static int txnCount = 0;
	private static int traceCount = 0;
	private static String currentFilename;
	private static String line = " ";
	private static String abort = null;
	// private static String prevLine = " ";
	// private static String[] prevTokens;
	private static String[] tokens;
	private static boolean morelines;
	private static boolean txnTotalseen = false;
	private static String regexSpace = "[ $]+";
	private static String regexHyphen = "[-]";
	private static String regexPercent = "([0-9]{2})\\%";
	private static String regexInteger = "(^(0)*[0-9])";
	private static String regexDate = "([0-9]{2})-([0-9]{2})-([0-9]{4})";
	private static String regexDecimal = "^[\\$-]?(?=\\(.*\\)|[^()]*$)\\(?\\d{1,3}(,?\\d{3})?(\\.\\d\\d?)?\\)?$";
	private static String regexAlphabetic = "[a-zA-Z.]+";
	private static String regexAlphaSpace = "[a-zA-Z. ]+";
	private static String regexDEPT = "[DEPT\\d{3}]+";
	private static String regexTokenXZ = "^(X)+(X|Z)?[12]?";
	private static int UnrecognisedCount = 0;
	static Scanner input = new Scanner(System.in);
	static String userEntry;
	// private static boolean doTrace = false;
	static ArrayList<String> checked = new ArrayList<String>();

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://192.168.178.235:3306/SaQ?autoReconnect=true&useSSL=false";
	static final String USER = "geoff2";
	static final String PASS = "talk22me";

	// // JDBC driver name and database URL
	// static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	// static final String DB_URL =
	// "jdbc:mysql://localhost:3306/Test1?autoReconnect=true&useSSL=false";
	// static final String DB_URL = "jdbc:mysql://localhost/Test1";
	// jdbc:mysql://localhost:3306/Peoples?autoReconnect=true&useSSL=false

	static Connection conn;
	static Statement stmt;

	public interface JournalTxnInterface {
		/**
		 * Get the name of the person.
		 * 
		 * @return String representing person's name
		 */
		void putEJTxnPaymentTotal(float ejtotal);
		// ejTxnPaymentTotal = ejtotal;
	}

	public static String selectDirectory(String fDir) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				fDir = selectedFile.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fDir;
	}

	public static void processDirectory(String sDir) { // 1

		try {
			Files.find(Paths.get(sDir), 99999,
					(p, bfa) -> bfa.isRegularFile() && (p.getFileName().toString().matches(".*\\.TXT")))
					.forEach(item -> {
						System.out.println(item);
						Path FileName = item.getFileName();
						String RegisterFileType = FileName.toString().substring(0, 2);
						// String JournalDate = FileName.toString().substring(2,
						// 8);
						switch (RegisterFileType) {
						case "EJ": {
							// "EJ" are the Electronic Journal files (cash
							// register receipts)
							try {
								ProcessEJ(item.toString());
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						}
						default:
							break;
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void ProcessEJ(String fileIn) throws IOException {
		currentFilename = fileIn;
		FileInputStream fileStream = new FileInputStream(fileIn);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
		getNextLine(br);
		while (morelines) {
			// doTrace = false;
			if (tokens.length == 0) {
				processTokens0(fileIn, br);
			} else if (tokens.length == 1) {
				processTokens1(fileIn, br);
			} else if (tokens.length == 2) {
				processTokens2(fileIn, br);
			} else if (tokens.length == 3) {
				processTokens3(fileIn, br);
			} else if (tokens.length == 4) {
				processTokens4(fileIn, br);
			} else if (tokens.length == 5) {
				processTokens5(fileIn, br);
			} else if (tokens.length == 6) {
				processTokens6(fileIn, br);
			} else
				processUnrecognisedLine(fileIn, br);
		}
	}

	private static void processUnrecognisedLine(String fin, BufferedReader br) {
		if (tokens[0].matches(regexTokenXZ)) {
			// JournalTrace(fin, "Token XZ");
			getNextLine(br);
			if (tokens[1].matches(regexInteger)) {
				// JournalTrace(fin, "Token XZ Integer");
				getNextLine(br);
			} else {
			}
		} else {
			UnrecognisedCount++;
			System.out.println("Haven't identified a line, " + UnrecognisedCount + " times");
			System.out.println("We'll skip it for now");
			JournalTrace(fin, "Unrecognised");
			System.out.println("Aborting");
			// doTrace = true;
			JournalTrace(fin, "Unrecognised");
			System.out.println("# Tokens 6:" + tokens[6]);
			getNextLine(br);
		}
	}

	private static void processTokens6(String fin, BufferedReader br) {
		if ((tokens.length == 6) && (tokens[1].matches(regexDecimal)) && (tokens[5].matches(regexDecimal))) {
			// 6 tokens, with tokens 1&5 numbers (Category includes a
			// space) ++5
			if (!checked.contains("@22"))
				checked.add("@22");
			JournalTrace(fin, "@22");
			JournalTxnLineQuantity = Float.parseFloat(tokens[1]);
			JournalTxnLineCategory = tokens[2] + " " + tokens[3];
			JournalTxnLineAmount = Float.parseFloat(tokens[5].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@22", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else
			processUnrecognisedLine(fin, br);
	}

	private static void processTokens5(String fin, BufferedReader br) {
		if ((tokens.length == 5) && ((tokens[1].equals("REG")))) {
			// (tokens[0].equals("REG")) {
			// |REG 01-09-2016 03:24 PM| -- Case 1-again
			// | 000022|
			if (!checked.contains("REG")) {
				checked.add("REG");
			}
			JournalTrace(fin, "REG");
			TxnSubtotalled = false;
			txnTotalseen = false;
			journalTxnTime = tokens[3];
			tokens = tokens[2].split(regexHyphen);
			journalTxnDate = tokens[2] + "-" + tokens[1] + "-" + tokens[0];
			getNextLine(br);
			journalTxnID = Integer.parseInt(tokens[1]);
			// System.out.print("Txn +++: Date " + journalTxnDate + ", Time " +
			// journalTxnTime + ";");
			// System.out.println("TxnID " + journalTxnID + ";");
			getNextLine(br);
			JournalTxnLineNumber = 0;
		} else if ((tokens.length == 5) && (tokens[1].matches(regexDecimal)) && (tokens[4].matches(regexDecimal))) {
			// | 1 Fabric * $44.50| -- Case 7
			if (!checked.contains("@21")) {
				checked.add("@21");
			}
			JournalTrace(fin, "@21");
			JournalTxnLineQuantity = Float.parseFloat(tokens[1]);
			JournalTxnLineCategory = tokens[2];
			JournalTxnLineAmount = Float.parseFloat(tokens[4].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@21", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} // token count is 5
		else
			processUnrecognisedLine(fin, br);
	}

	private static void processTokens4(String fin, BufferedReader br) {
		if (tokens[0].equals("Data")) {
			// |data backup normal end|
			if (!checked.contains("Data"))
				checked.add("Data");
			JournalTrace(fin, "Data");
			getNextLine(br);
		} else if (tokens[0].equals("PGM")) {
			if (!checked.contains("PGM"))
				checked.add("PGM");
			JournalTrace(fin, "PGM");
			getNextLine(br);
			getNextLine(br);
		} else if (tokens[0].equals("X") && tokens[1].matches(regexDate)
				|| (tokens[0].equals("X") && tokens[1].equals("DEPT"))
				|| (tokens[0].equals("X") && tokens[1].equals("FIX"))
				|| (tokens[0].equals("X") && tokens[1].equals("TRANS"))
				|| (tokens[0].equals("X") && tokens[1].equals("FLASH"))) {
			if (!checked.contains("X date/DEPT/FIX/TRANS/FLASH"))
				checked.add("X date/DEPT/FIX/TRANS/FLASH");
			JournalTrace(fin, "X date/DEPT/FIX/TRANS/FLASH");
			getNextLine(br);
			getNextLine(br);
		} else if (tokens[1].equals("GST")) { // 4
			if (!checked.contains("GST")) {
				checked.add("GST");
			}
			JournalTrace(fin, "GST");
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[0].equals("REG"))) {
			// |REG 01-09-2016 03:24 PM| -- Case 1
			// | 000022|
			if (!checked.contains("REG #2")) {
				checked.add("REG #2");
			}
			JournalTrace(fin, "REG #2");
			txnTotalseen = false;
			if (tokens[3].equals("PM")) { // move time to 24-hour format
				int hh = Integer.parseInt(tokens[2].substring(0, 2));
				if (hh < 12) {
					hh = hh + 12;
				}
				StringBuilder sb = new StringBuilder();
				sb.append(hh);
				journalTxnTime = sb.toString() + tokens[2].substring(2, 5);
			} else {
				journalTxnTime = tokens[2];
			}
			tokens = tokens[1].split(regexHyphen);
			journalTxnDate = tokens[2] + "-" + tokens[1] + "-" + tokens[0];
			getNextLine(br);
			journalTxnID = Integer.parseInt(tokens[1]);
			// JournalTxnPayLine = 1;
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[1].equals("-")) && (tokens[3].matches(regexDecimal))) {
			// (same Quantity and Category as previous TxnLine)
			if (!checked.contains("@16"))
				checked.add("@16");
			JournalTrace(fin, "@16");
			JournalTxnLineDiscount = Float.parseFloat(tokens[3]);
			JournalTxnLineAmount = JournalTxnLineDiscount;
			JournalTxnLineCategory = "Discount";
			JournalTxnLineNumber++;
			JournalTxnPaymentTotal = JournalTxnPaymentTotal - JournalTxnPaymentDiscount;
			// JournalTxnLineDiscount =
			// Float.parseFloat(tokens[3].replaceAll(",", ""));
			// JournalTxnLineNumber++;
			insertEJLine("@16", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			// perhaps delete the row without the discount, and re-insert
			// with the discount
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[2].matches(regexAlphabetic)) && (tokens[3].matches(regexDecimal))) {
			if (!checked.contains("@17"))
				checked.add("@17");
			JournalTrace(fin, "@17");
			JournalTxnLineCategory = tokens[2];
			JournalTxnLineAmount = Float.parseFloat(tokens[3].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@17", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[1].equals("%-")) && (tokens[3].matches(regexDecimal))) {
//			if (!checked.contains("@17b"))
//				checked.add("@17b");
			JournalTrace(fin, "@17b");
			if (TxnSubtotalled) {
				JournalTxnPaymentDiscountPCT = Float.parseFloat(tokens[1].replace("%", ""));
				getNextLine(br);
				if ((tokens.length == 4) && (tokens[3].matches(regexDecimal)))
					JournalTxnPaymentDiscount = Float.parseFloat(tokens[3]);
				else
					JournalTxnPaymentDiscount = Float.parseFloat(tokens[2]);
				LineAmountTotal=LineAmountTotal+JournalTxnPaymentDiscount;
				getNextLine(br);
			} else {
				JournalTxnLineDiscountPCT = Float.parseFloat(tokens[1].replace("%", ""));
				getNextLine(br);
				if ((tokens.length == 4) && (tokens[3].matches(regexDecimal)))
					JournalTxnLineDiscount = Float.parseFloat(tokens[3]);
				else
					JournalTxnLineDiscount = Float.parseFloat(tokens[2]);
				JournalTxnLineAmount = JournalTxnLineDiscount;
				JournalTxnLineNumber++;
				insertEJLine("@17b", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			}
//			JournalTxnLineDiscount = Float.parseFloat(tokens[3]);
//			JournalTxnLineAmount = JournalTxnLineDiscount;
//			JournalTxnLineCategory = "Discount";
//			JournalTxnLineNumber++;
//			JournalTxnPaymentTotal = JournalTxnPaymentTotal - JournalTxnPaymentDiscount;

			// JournalTxnLineAmount = 0;
			// JournalTxnLineDiscount =
			// Float.parseFloat(tokens[3].replaceAll(",", ""));
			// JournalTxnLineQuantity = 0;
			// JournalTxnLineNumber++;
			insertEJLine("@17b", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[1].equals("COUPON")) && (tokens[3].matches(regexDecimal))) {
			if (!checked.contains("@17a"))
				checked.add("@17a");
			JournalTrace(fin, "@17a");
			JournalTxnLineQuantity = 1;
			JournalTxnLineCategory = tokens[1];
			JournalTxnLineAmount = Float.parseFloat(tokens[3].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@17a", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else if ((tokens.length == 4) && !(tokens[2].equals("-")) && (tokens[3].matches(regexDecimal))) {
			if (!checked.contains("@17c"))
				checked.add("@17c");
			JournalTrace(fin, "@17c");
			JournalTxnLineQuantity = Float.parseFloat(tokens[0]);
			JournalTxnLineCategory = tokens[1];
			JournalTxnLineAmount = Float.parseFloat(tokens[3].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@17a", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[0].matches(regexDecimal)) && (tokens[3].matches(regexDecimal))) {
			// 5 tokens, with tokens 1&4 numbers ++2
			if (!checked.contains("@18"))
				checked.add("@18");
			JournalTrace(fin, "@18");
			JournalTxnLineQuantity = Float.parseFloat(tokens[0]);
			JournalTxnLineCategory = tokens[1];
			JournalTxnLineAmount = Float.parseFloat(tokens[3].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@18", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[2].matches(regexAlphabetic))
				&& (tokens[3].matches(regexAlphabetic))) {
			// current line is 1 Sale Fabric
			if (!checked.contains("@19"))
				checked.add("@19");
			JournalTrace(fin, "@19");
			JournalTxnLineQuantity = Float.parseFloat(tokens[1]);
			JournalTxnLineCategory = tokens[2] + " " + tokens[3];
			getNextLine(br);
			JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@19", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[0].equals("_PSD"))) {
			if (!checked.contains("_PSD"))
				checked.add("_PSD");
			JournalTrace(fin, "_PSD");
			getNextLine(br);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[0].equals("_P02"))) {
			if (!checked.contains("_P02"))
				checked.add("_P02");
			JournalTrace(fin, "_P02");
			getNextLine(br);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[0].equals("_P01"))) {
			if (!checked.contains("_P01"))
				checked.add("_P01");
			JournalTrace(fin, "_P01");
			getNextLine(br);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[0].equals("_R_F"))) {
			if (!checked.contains("_R_F"))
				checked.add("_R_F");
			JournalTrace(fin, "_R_F");
			getNextLine(br);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[0].equals("OFF"))) {
			if (!checked.contains("OFF"))
				checked.add("OFF");
			JournalTrace(fin, "OFF");
			getNextLine(br);
			getNextLine(br);
		} else if ((tokens.length == 4) && (tokens[1].matches(regexDecimal)) && (tokens[3].matches(regexDecimal))) {
			// 5 tokens, with tokens 1&4 numbers ++3
			if (!checked.contains("@20"))
				checked.add("@20");
			JournalTrace(fin, "@20");
			JournalTxnLineQuantity = Float.parseFloat(tokens[1]);
			JournalTxnLineCategory = tokens[2];
			JournalTxnLineAmount = Float.parseFloat(tokens[3].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@20", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else if (tokens.length == 4 && tokens[1].equals("TL")) {
			if (!checked.contains("TL"))
				checked.add("TL");
			JournalTrace(fin, "TL");
			if (!txnTotalseen) {
				txnTotalseen = true;
				JournalTxnPaymentTotal = Float.parseFloat(tokens[3].replace("_", "").replace(",", ""));
			}
			getNextLine(br);
		} else if (tokens[0].equals("Z") && tokens[1].matches(regexDate)) {
			if (!checked.contains("z (at end)"))
				checked.add("z (at end)");
			JournalTrace(fin, "z (at end)");
			for (int i = 1; i < 31; i++) {
				getNextLine(br);
			}
			morelines = false;
		} // token count is 4
		else
			processUnrecognisedLine(fin, br);
	}

	private static void processTokens3(String fin, BufferedReader br) {
		// if (tokens.length == 3 && tokens[1].equals("TL")) {
		// if (!txnTotalseen) {
		// txnTotalseen = true;
		// JournalTxnPaymentTotal = Float.parseFloat(tokens[3].replace("_",
		// "").replace(",", ""));
		// }
		// getNextLine(br);
		// } else
		if (tokens[0].equals("X") && tokens[1].equals("DAILY")) {
			if (!checked.contains("DAILY"))
				checked.add("DAILY");
			JournalTrace(fin, "DAILY");
			getNextLine(br);
		} else if (tokens[1].equals("CHARGE") || tokens[1].equals("CASH")) {
			if (!checked.contains("CHARGE/CASH"))
				checked.add("CHARGE/CASH");
			JournalTrace(fin, "CHARGE/CASH");
			if (tokens[1].equals("CASH")) {
				JournalTxnPaymentCASH = true;
			} else if (tokens[1].equals("CHARGE")) {
				JournalTxnPaymentCHARGE = true;
			}
			getNextLine(br);
			while (tokens[1].equals("TL"))
			// if (tokens[1].equals("TL")) // more payments to come
			{
				getNextLine(br);
				if (tokens[1].equals("CASH")) {
					JournalTxnPaymentCASH = true;
				} else if (tokens[1].equals("CHARGE")) {
					JournalTxnPaymentCHARGE = true;
				}
				getNextLine(br);
			}
			try {
				insertEJTxn(journalTxnDate, journalTxnTime, journalTxnID,
						// JournalTxnPayLine,
						JournalTxnPaymentTotal, JournalTxnPaymentCHARGE, JournalTxnPaymentCASH, JournalTxnPaymentCOUPON,
						JournalTxnPaymentDiscountPCT, JournalTxnPaymentDiscount, JournalTxnPaymentRounding);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			txnTotalseen = false;
		} else if (tokens[1].equals("COUPON")) { // 3
			if (!checked.contains("COUPON"))
				checked.add("COUPON");
			JournalTrace(fin, "COUPON");
			JournalTxnPaymentCOUPON = true;
			LineAmountTotal = LineAmountTotal + Float.parseFloat(tokens[2]);
			// System.out.println("COUPON: LineAmountTotal="+LineAmountTotal);
			getNextLine(br);
		} else if ((tokens[1].equals("VOID"))) {
			if (!checked.contains("VOID"))
				checked.add("VOID");
			JournalTrace(fin, "VOID");
			getNextLine(br);
		} else if (tokens[0].equals("Z") && tokens[1].equals("DEPT")) {
			if (!checked.contains("Z DEPT"))
				checked.add("Z DEPT");
			JournalTrace(fin, "Z DEPT");
			getNextLine(br);
			getNextLine(br);
		} else if (tokens[1].equals("CG")) {
			// change = tokens[2];
			// txnTotal = txnTotal + "-" + change;
			if (!checked.contains("CG"))
				checked.add("CG");
			JournalTrace(fin, "CG");
			getNextLine(br);
		} else if (tokens[0].equals("NET")) {
			if (!checked.contains("NET"))
				checked.add("NET");
			JournalTrace(fin, "NET");
			getNextLine(br);
			getNextLine(br);
		} else if (tokens[1].equals("RF")) {
			if (!checked.contains("RF"))
				checked.add("RF");
			JournalTrace(fin, "RF");
			// getNextLine(br);
			getNextLine(br);
		} else if (tokens[0].equals("Z")) {
			if (!checked.contains("Z alone"))
				checked.add("Z alone");
			JournalTrace(fin, "Z alone");
			getNextLine(br);
		} else if (tokens[0].equals("GROSS")) {
			if (!checked.contains("GROSS"))
				checked.add("GROSS");
			JournalTrace(fin, "GROSS");
			getNextLine(br);
			getNextLine(br);
		} else if (tokens[0].equals("Z") && tokens[1].equals("FIX")) {
			if (!checked.contains("Z FIX"))
				checked.add("Z FIX");
			JournalTrace(fin, "Z FIX");
			getNextLine(br);
			getNextLine(br);
		} else if (tokens[1].equals("NS")) {
			if (!checked.contains("NS"))
				checked.add("NS");
			JournalTrace(fin, "NS");
			getNextLine(br);
		} else if (tokens[1].equals("SUBTOTAL")) { // 3
			if (!checked.contains("SUBTOTAL"))
				checked.add("SUBTOTAL");
			JournalTrace(fin, "SUBTOTAL");
			TxnSubtotalled = true;
			getNextLine(br);
		} else if (tokens[1].equals("ROUND")) { // 3
			if (!checked.contains("ROUND"))
				checked.add("ROUND");
			JournalTrace(fin, "ROUND");
			JournalTxnPaymentRounding = Float.parseFloat(tokens[2]);
			getNextLine(br);
		} else if ((tokens.length == 3) && (tokens[1].equals("CORR"))) {
			if (!checked.contains("CORR"))
				checked.add("CORR");
			JournalTrace(fin, "CORR");
			JournalTxnLineQuantity = 0;
			// JournalTxnLineCategory = tokens[1];
			JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@03b", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else if ((tokens.length == 3) && (tokens[0].equals("-"))) {
			// reduced price item
			if (!checked.contains("@11"))
				checked.add("@11");
			JournalTrace(fin, "- (reduce price)@11");
			JournalTxnLineDiscount = Float.parseFloat(tokens[2]);
			insertEJLine("@11", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			JournalTxnLineDiscount = 0;
			getNextLine(br);
		} else if ((tokens.length == 3) && (tokens[1].matches(regexDecimal) && !(tokens[2].matches(regexDecimal)))) {
			System.out.println(
					"check whether this is processing properly - there are if statements here that aren't matched");
			if (!checked.contains("@12"))
				checked.add("@12");
			JournalTrace(fin, "@12");
			JournalTxnLineQuantity = Float.parseFloat(tokens[1]);
			JournalTxnLineCategory = tokens[2];
			getNextLine(br);
			if ((tokens.length == 3) && (tokens[2].matches(regexDecimal))) {
				JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
				JournalTxnLineNumber++;
				insertEJLine("@12", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			}
		} else if ((tokens.length == 3) && (tokens[0].matches(regexInteger) && !(tokens[2].matches(regexDecimal)))) {
			if (!checked.contains("@14A"))
				checked.add("14A");
			JournalTrace(fin, "@14a");
			JournalTxnLineQuantity = 1;
			JournalTxnLineCategory = tokens[1];
			JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@14a", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else if ((tokens.length == 3) && (tokens[0].matches(regexInteger)
				&& (tokens[1].matches(regexAlphabetic) && (tokens[2].matches(regexDecimal))))) {
			// |28 Postage $126.00|
			if (!checked.contains("@14"))
				checked.add("@14");
			JournalTrace(fin, "@14");
			JournalTxnLineCategory = tokens[1] + " " + tokens[2];
			getNextLine(br);
			if ((tokens.length == 3) && (tokens[0].matches(regexInteger)) && (tokens[2].matches(regexDecimal))) {
				JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
				JournalTxnLineNumber++;
				insertEJLine("@14", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
				System.out.println("unmatched if statement here");
				abort = tokens[9];
			} else {
				System.out.println("unmatched else on @14");
				System.exit(14);
			}
			JournalTxnLineNumber++;
		} else if ((tokens[1].equals("Postage"))) {
			// |28 Postage $126.00|
			if (!checked.contains("@14b"))
				checked.add("@14b");
			JournalTrace(fin, "@14b");
			JournalTxnLineCategory = tokens[1];
			JournalTxnLineQuantity = Float.parseFloat(tokens[0]);
			JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@14", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
			JournalTxnLineNumber++;
		} else if ((tokens[1].equals("-"))) {
			// | - -1.18|
			if (!checked.contains("@14c"))
				checked.add("@14c");
			JournalTrace(fin, "@14c");
			JournalTxnLineQuantity = 1;
			JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
			JournalTxnLineDiscount = Float.parseFloat(tokens[2].replaceAll(",", ""));
			JournalTxnLineNumber++;
			insertEJLine("@14", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
			JournalTxnLineNumber++;
		} else if (tokens.length == 3 && tokens[1].equals("TL")) {
			if (!checked.contains("@10a"))
				checked.add("@10a");
			JournalTrace(fin, "@10a");
			JournalTxnPaymentTotal = Float.parseFloat(tokens[2].replaceAll("_", ""));
			// TxnSubtotalled = false; // needs to be reset, since we are past
			// the end of the lines for this txn
			getNextLine(br);
		} else if (tokens[1].equals("CANCEL")) {
			if (!checked.contains("CANCEL"))
				checked.add("CANCEL");
			JournalTrace(fin, "CANCEL");
			resetTxnTotals();
			getNextLine(br);
		} else
			processUnrecognisedLine(fin, br);
	}

	private static void processTokens2(String fin, BufferedReader br) {
		if (tokens[0].equals("OFF") && tokens[1].matches(regexDate)
				|| (tokens[0].equals("X") && tokens[1].equals("DEPT"))
				|| (tokens[0].equals("X") && tokens[1].equals("FIX"))
				|| (tokens[0].equals("X") && tokens[1].equals("TRANS"))
				|| (tokens[0].equals("X") && tokens[1].equals("FLASH"))
				|| (tokens[0].equals("X") && tokens[1].equals("MONTHLY"))
				|| (tokens[0].equals("X") && tokens[1].equals("GROUP"))
				|| (tokens[0].equals("X") && tokens[1].equals("EJ"))
				|| (tokens[0].equals("X") && tokens[1].equals("PLU"))
				|| (tokens[0].equals("X") && tokens[1].equals("HOURLY"))
				|| (tokens[0].equals("XZ") && tokens[1].matches(regexDate))
				|| (tokens[0].matches(regexTokenXZ) && tokens[1].equals("DEPT"))
				|| (tokens[0].matches(regexTokenXZ) && tokens[1].equals("FIX"))
				|| (tokens[0].matches(regexTokenXZ) && tokens[1].equals("TRANS"))) {
			if (!checked.contains("OFF, X DEPT, X FIX, X TRANS, X FLASH"))
				checked.add("OFF, X DEPT, X FIX, X TRANS, X FLASH");
			JournalTrace(fin, "OFF, X DEPT, X FIX, X TRANS, X FLASH");
			getNextLine(br);
			getNextLine(br);
		} else if (tokens[0].equals("X") && tokens[1].equals("INDIVIDUAL")
				|| (tokens[0].matches(regexTokenXZ) && tokens[1].equals("PERIODIC"))) {
			if (!checked.contains("X DEPT INDIVIDUAL, XX1, XX2"))
				checked.add("X DEPT INDIVIDUAL, XX1, XX2");
			JournalTrace(fin, "X DEPT INDIVIDUAL, XX1, XX2");
			getNextLine(br);
		} else if (tokens[0].equals("Cash In Draw") || tokens[0].equals("CHID") || tokens[0].equals("CKID")
				|| tokens[0].equals("CRID(1)") || tokens[0].equals("CRID(2)") || tokens[0].equals("CRID(3)")
				|| tokens[0].equals("CRID(4)") || tokens[0].equals("CUST") || tokens[0].equals("ROUND")
				|| tokens[0].equals("TAXABLE") || tokens[0].equals("GST") || tokens[0].equals("GT")) {
			if (!checked.contains("CHID, etc"))
				checked.add("CHID, etc");
			JournalTrace(fin, "CHID, etc");
			getNextLine(br);
		} else if ((tokens.length == 2) && (tokens[1].matches(regexInteger))) {
			// | 000001|
			if (!checked.contains("@05"))
				checked.add("@05");
			JournalTrace(fin, "@05");
			abort = tokens[9];
			// this should be taken care of in "REG" processing
			JournalTxnLineQuantity = Float.parseFloat(tokens[1].replaceAll(",", ""));
			getNextLine(br);
			if ((tokens.length == 2) && !(tokens[1].matches(regexDecimal))) {
				// we have a two-token Category, which looks like
				// 0.25
				// Sale Fabric
				// * $5.13
				JournalTxnLineCategory = tokens[1] + "&" + tokens[2];
				getNextLine(br);
				if ((tokens.length == 2) && (tokens[1].matches(regexDecimal))) {
					JournalTxnLineAmount = Float.parseFloat(tokens[1].replaceAll(",", ""));
					JournalTxnLineNumber++;
					insertEJLine("@05", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
							JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount,
							JournalTxnLineDiscountPCT, JournalTxnLineDiscount);
					getNextLine(br);
				}
				System.out.println("unmatched IF statement @05");
				abort = tokens[9];
			}
		} else if ((tokens.length == 2) && (tokens[1].matches(regexDecimal))) {
			// | 0.3 | -- case 3
			// | Fabric * $6.30| (several possibilities)
			if (!checked.contains("@05ab"))
				checked.add("@05ab");
			JournalTrace(fin, "@05ab");
			JournalTxnLineQuantity = Float.parseFloat(tokens[1].replaceAll(",", ""));
			getNextLine(br);
			if ((tokens.length == 3) && (tokens[1].matches(regexAlphabetic)) && (tokens[2].matches(regexAlphabetic))) {
				// we have a two-token Category, which looks like
				// | Sale Fabric |
				// System.out.println("@05ab first if statement");
				JournalTxnLineCategory = tokens[1] + " " + tokens[2];
				getNextLine(br);
				if ((tokens.length == 3) && (tokens[2].matches(regexDecimal))) {
					// | * $5.13 |
					// System.out.println("@05ab second if statement");
					JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
					JournalTxnLineNumber++;
					insertEJLine("@05a", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
							JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount,
							JournalTxnLineDiscountPCT, JournalTxnLineDiscount);
					getNextLine(br);
				} else {
					System.out.println("@05ab else of second if statement");
					System.out.println("exiting from @05ab");
					System.exit(5);
				}
			} else if ((tokens.length == 4) && (tokens[1].matches(regexAlphabetic)) && (tokens[2].equals("*"))
					&& (tokens[3].matches(regexDecimal))) {
				// | 0.25 |
				// | Fabric * $6.30|
				// System.out.println("@05ab else of first if statement");
				JournalTxnLineCategory = tokens[1];
				JournalTxnLineAmount = Float.parseFloat(tokens[3].replaceAll(",", ""));
				JournalTxnLineNumber++;
				insertEJLine("@05ab", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			}
			// else {
			// JournalTrace(fin, "@05ac");
			//// System.out.println("exiting from @05ac");
			//// System.exit(5);
			// }
		} else if ((tokens.length == 2) && (tokens[1].matches(regexPercent))) {
			// | 20% |
			// | %- -11.20 |
			// System.out.println(" Discount");
			if (!checked.contains("@06"))
				checked.add("@06");
			JournalTrace(fin, "@06");
			if (TxnSubtotalled) {
				JournalTxnPaymentDiscountPCT = Float.parseFloat(tokens[1].replace("%", ""));
				getNextLine(br);
				if ((tokens.length == 4) && (tokens[3].matches(regexDecimal)))
					JournalTxnPaymentDiscount = Float.parseFloat(tokens[3]);
				else
					JournalTxnPaymentDiscount = Float.parseFloat(tokens[2]);
				LineAmountTotal=LineAmountTotal+JournalTxnPaymentDiscount;
				getNextLine(br);
			} else {
				JournalTxnLineDiscountPCT = Float.parseFloat(tokens[1].replace("%", ""));
				getNextLine(br);
				if ((tokens.length == 4) && (tokens[3].matches(regexDecimal)))
					JournalTxnLineDiscount = Float.parseFloat(tokens[3]);
				else
					JournalTxnLineDiscount = Float.parseFloat(tokens[2]);
				JournalTxnLineAmount = JournalTxnLineDiscount;
				JournalTxnLineNumber++;
				insertEJLine("@06", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			}
		} else if ((tokens.length == 2) && tokens[1].equals("TL")) {
			getNextLine(br);
			if (!checked.contains("@06a"))
				checked.add("@06a");
			JournalTrace(fin, "@06a");
			JournalTxnPaymentTotal = Float.parseFloat(tokens[2].replace("_", "").replace(",", ""));
			// TxnSubtotalled = false;
			getNextLine(br);
		} else if ((tokens.length == 2) && tokens[0].matches(regexDecimal) && tokens[1].matches(regexAlphabetic)) {
			// |100 Fabric | -- case 3
			// | * $6.30|
			if (!checked.contains("@07"))
				checked.add("@07");
			JournalTrace(fin, "@07");
			JournalTxnLineQuantity = Float.parseFloat(tokens[0].replaceAll(",", ""));
			JournalTxnLineCategory = tokens[1];
			getNextLine(br);
			if ((tokens.length == 2) && (tokens[1].matches(regexDecimal))) {
				// we have a two-token Category, which looks like
				// 100
				// Fabric
				// * $5.13
				JournalTxnLineAmount = Float.parseFloat(tokens[1].replaceAll(",", ""));
				JournalTxnLineNumber++;
				insertEJLine("@07", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			} else if (((tokens.length == 2) && (tokens[0].matches(regexDecimal))
					&& (tokens[1].matches(regexAlphabetic)))) {
				if (!checked.contains("@07a"))
					checked.add("@07a");
				JournalTrace(fin, "@07a");
				JournalTxnLineQuantity = Float.parseFloat(tokens[0].replaceAll(",", ""));
				JournalTxnLineCategory = tokens[1];
				getNextLine(br);
				JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
				JournalTxnLineNumber++;
				insertEJLine("@07a", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			} else if (((tokens.length == 3) && (tokens[1].equals("*") && (tokens[2].matches(regexDecimal))))) {
				if (!checked.contains("@07b"))
					checked.add("@07b");
				JournalTrace(fin, "@07b");
				JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
				JournalTxnLineNumber++;
				insertEJLine("@07b", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			}
			// System.out.println("unmatched IF statement @07");
			// String abort = tokens[9];
		} else if ((tokens.length == 2) && (tokens[1].matches(regexAlphabetic))) {
			// | 100 |
			// | Fabric | -- case 3
			// | * $6.30|
			if (!checked.contains("@08"))
				checked.add("@08");
			JournalTrace(fin, "@08");
			JournalTxnLineQuantity = Float.parseFloat(tokens[1]);
			getNextLine(br);
			if ((tokens.length == 2) && !(tokens[1].matches(regexDecimal))) {
				// we have a two-token Category, which looks like
				// 0.25
				// Sale Fabric
				// * $5.13
				JournalTxnLineCategory = tokens[1] + "&" + tokens[2];
				getNextLine(br);
				if ((tokens.length == 2) && (tokens[1].matches(regexDecimal))) {
					JournalTxnLineAmount = Float.parseFloat(tokens[1].replaceAll(",", ""));
					JournalTxnLineNumber++;
					insertEJLine("@08", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
							JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount,
							JournalTxnLineDiscountPCT, JournalTxnLineDiscount);
					getNextLine(br);
				} else {
					if (!checked.contains("@08a"))
						checked.add("@08a");
					JournalTrace(fin, "@08a");
					System.out.println("unmatched IF statement @05");
					abort = tokens[9];
				}

			} else {
				System.out.println("unmatched IF statement @05");
				abort = tokens[9];
				if (!checked.contains("@08b"))
					checked.add("@08b");
				JournalTrace(fin, "@08b");
			}
		} else if ((tokens.length == 2) && !(tokens[1].matches(regexDecimal))) {
			// | 100 Fabric | -- case 3
			// | * $6.30|
			if (!checked.contains("@09"))
				checked.add("@09");
			JournalTrace(fin, "@09");
			JournalTxnLineQuantity = Float.parseFloat(tokens[1]);
			getNextLine(br);
			if ((tokens.length == 2) && !(tokens[1].matches(regexDecimal))) {
				// we have a two-token Category, which looks like
				// 0.25
				// Sale Fabric
				// * $5.13
				JournalTxnLineCategory = tokens[1] + "&" + tokens[2];
				getNextLine(br);
				if ((tokens.length == 2) && (tokens[1].matches(regexDecimal))) {
					JournalTxnLineAmount = Float.parseFloat(tokens[1].replaceAll(",", ""));
					JournalTxnLineNumber++;
					insertEJLine("@09", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
							JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount,
							JournalTxnLineDiscountPCT, JournalTxnLineDiscount);
					getNextLine(br);
				}
				System.out.println("unmatched IF statement @09-1");
				abort = tokens[9];
			}
			System.out.println("unmatched IF statement @09-2");
			abort = tokens[9];
		} else if ((tokens.length == 2) && (tokens[1].matches(regexDecimal))) {
			// | 0.25 |
			// | Sale Fabric |
			if (!checked.contains("@10"))
				checked.add("@10");
			JournalTrace(fin, "@10");
			JournalTxnLineQuantity = Float.parseFloat(tokens[1]);
			getNextLine(br);
			if ((tokens.length == 2) && (tokens[1].matches(regexDecimal))) {
				JournalTxnLineCategory = tokens[1];
				JournalTxnLineAmount = Float.parseFloat(tokens[3].replaceAll(",", ""));
				JournalTxnLineNumber++;
				insertEJLine("@10", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			}
			System.out.println("unmatched IF statement @10");
			abort = tokens[9];
		} else if (tokens.length == 2 && tokens[1].equals("TL")) {
			if (!checked.contains("@10a"))
				checked.add("@10a");
			JournalTrace(fin, "@10a");
			JournalTxnPaymentTotal = Float.parseFloat(tokens[1]);
			// TxnSubtotalled = false; // needs to be reset, since we are past
			// the end of the lines for this txn
			getNextLine(br);
		}
		// token count is 2
		else

			processUnrecognisedLine(fin, br);
	}

	private static void processTokens1(String fin, BufferedReader br) {
		if ((tokens[0].equals("------------------------"))) {
			if (!checked.contains("@---"))
				checked.add("@---");
			JournalTrace(fin, "@---");
			getNextLine(br);
		} else if ((tokens.length == 1) && (tokens[0].matches(regexDecimal))) {
			// |13.3 | -- case 2
			// | Fabric * $212.80|
			if (!checked.contains("@04"))
				checked.add("@04");
			JournalTrace(fin, "@04");
			JournalTxnLineQuantity = Float.parseFloat(tokens[0]);
			getNextLine(br);

			if ((tokens.length == 2) && (tokens[1].matches(regexAlphabetic))) {
				JournalTxnLineCategory = tokens[1];
				getNextLine(br);
				if ((tokens.length == 3) && (tokens[2].matches(regexDecimal))) {

					JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
					JournalTxnLineNumber++;
					insertEJLine("@04", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
							JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount,
							JournalTxnLineDiscountPCT, JournalTxnLineDiscount);
					getNextLine(br);
				}
			} else if ((tokens.length == 4) && (tokens[3].matches(regexDecimal))) {
				JournalTxnLineCategory = tokens[1];
				JournalTxnLineAmount = Float.parseFloat(tokens[3].replaceAll(",", ""));
				JournalTxnLineNumber++;
				insertEJLine("@04", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			} else if ((tokens.length == 3) && (tokens[1].matches(regexAlphabetic))
					&& (tokens[2].matches(regexDecimal))) {
				JournalTxnLineCategory = tokens[1];
				JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
				JournalTxnLineNumber++;
				insertEJLine("@04", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
						JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
						JournalTxnLineDiscount);
				getNextLine(br);
			}
		} else if ((tokens.length == 1) && (tokens[0].matches(regexDecimal))) {
			// |13.3 | -- case 2
			// | Fabric * $212.80|
			if (!checked.contains("@01"))
				checked.add("@01");
			JournalTrace(fin, "@01");
			JournalTxnLineQuantity = Float.parseFloat(tokens[0]);
			getNextLine(br);
			if ((tokens.length == 2) && !(tokens[1].matches(regexDecimal))) {
				// we have a large quantity, pushing the category onto the
				// next line
				// 100
				// Fabric
				// * $5.13
				JournalTxnLineCategory = tokens[1];
				getNextLine(br);
				if ((tokens.length == 2) && (tokens[1].matches(regexDecimal))) {
					JournalTxnLineAmount = Float.parseFloat(tokens[1].replaceAll(",", ""));
					JournalTxnLineNumber++;
					insertEJLine("@01", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
							JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount,
							JournalTxnLineDiscountPCT, JournalTxnLineDiscount);
					getNextLine(br);
				} else {
					JournalTxnLineAmount = Float.parseFloat(tokens[2].replaceAll(",", ""));
					JournalTxnLineNumber++;
					insertEJLine("@01", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
							JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount,
							JournalTxnLineDiscountPCT, JournalTxnLineDiscount);
					getNextLine(br);
					if (!checked.contains("@01a"))
						checked.add("@01a");
					JournalTrace(fin, "@01a");
				}
			} else if ((tokens.length == 4) && (tokens[3].matches(regexDecimal))) {
				if (!checked.contains("@22"))
					checked.add("@22");
				JournalTrace(fin, "@22");
				JournalTxnLineCategory = tokens[1];
				JournalTxnLineAmount = Float.parseFloat(tokens[3].replaceAll(",", ""));
				getNextLine(br);
			}
			if (!checked.contains("@02"))
				checked.add("@02");
			JournalTrace(fin, "@02");
			JournalTxnLineNumber++;
			insertEJLine("@02", journalTxnDate, journalTxnTime, journalTxnID, JournalTxnLineNumber,
					JournalTxnLineQuantity, JournalTxnLineCategory, JournalTxnLineAmount, JournalTxnLineDiscountPCT,
					JournalTxnLineDiscount);
			getNextLine(br);
		} else
			processUnrecognisedLine(fin, br);
	}

	private static void processTokens0(String fin, BufferedReader br) {
		if (!checked.contains("@00 tokens"))
			checked.add("@00 tokens");
		JournalTrace(fin, "@00 tokens");
		getNextLine(br);

	}

	private static void insertEJTxn(String journalTxnDate2, String journalTxnTime2, int journalTxnID2,
			// float journalTxnPayLine2,
			float journalTxnPaymentTotal2, boolean JournalTxnPaymentCHARGE2, boolean JournalTxnPaymentCASH2,
			boolean JournalTxnPaymentCOUPON2, float journalTxnPaymentDiscountPCT2, float journalTxnPaymentDiscount2,
			float JournalTxnPaymentRounding2) throws SQLException {
		String mismatch = "";
		float delta = (LineAmountTotal - journalTxnPaymentTotal2);

		// LineAmountTotal = 7464.6, but journalTxnPaymentTotal2 = 8850.6,
		// Mismatch #1, delta -1385.9995
		boolean ignore1 = (journalTxnDate2 == "2016-09-29" && journalTxnTime2 == "17:19");

		if ((Math.abs(delta) > 0.01) || ignore1) {
			mismatchCount++;
			mismatch = " ** ";
			System.out.println("LineAmountTotal = " + LineAmountTotal + ", but journalTxnPaymentTotal2 = "
					+ journalTxnPaymentTotal2 + ", Mismatch #" + mismatchCount + ", delta " + delta);
			System.out.println("file :" + currentFilename + ", journalTxnID2 = " + journalTxnID2);
			System.out.println("Review the debug information");
			JournalTrace(currentFilename, "Mismatch");
			userEntry = input.nextLine();
		}
		System.out.println(mismatch + journalTxnDate2 + " " + journalTxnTime2 + " " + journalTxnID2 + " "
		// + journalTxnPayLine2 + " "
				+ journalTxnPaymentTotal2 + " " + JournalTxnPaymentCHARGE2 + " " + JournalTxnPaymentCASH2 + " "
				+ JournalTxnPaymentCOUPON2 + " " + journalTxnPaymentDiscountPCT2 + " " + journalTxnPaymentDiscount2);

		resetTxnTotals();
		txnCount++;
		System.out.println("Creating statement...");
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String sql;
		sql = "insert into " + ejTxnTablename + "(" + " PaymentDateTime, " + " PaymentID, " + " PaymentTotal, "
				+ " PaymentIsCHARGE, " + " PaymentIsCASH, " + " PaymentIsCOUPON, " + " PaymentIsUNKNOWN, "
				+ " PaymentDiscountPCT, " + " PaymentDiscount, " + " PaymentRounding " + ") VALUES (" + "'"
				+ journalTxnDate2 + " " + journalTxnTime2 + ":00', " + journalTxnID2 + "," + journalTxnPaymentTotal2
				+ "," + JournalTxnPaymentCHARGE2 + "," + JournalTxnPaymentCASH2 + "," + JournalTxnPaymentCOUPON2 + ","
				+ false + "," + journalTxnPaymentDiscountPCT2 + "," + journalTxnPaymentDiscount2 + ","
				+ JournalTxnPaymentRounding2 + ");";
		int result = stmt.executeUpdate(sql);
		if (result > 1) {
			System.out.println("Executed insert into " + ejTxnTablename + ", result= " + result);
		}
		stmt.close();
	}

	private static void resetTxnTotals() {
		JournalTxnPaymentTotal = 0;
		JournalTxnPaymentCHARGE = false;
		JournalTxnPaymentCASH = false;
		JournalTxnPaymentCOUPON = false;
		JournalTxnPaymentDiscountPCT = 0;

		JournalTxnPaymentDiscount = 0;

		JournalTxnLineNumber = 0;

		LineAmountTotal = 0;
		// System.out.println("resetTxnTotal: LineAmountTotal=" +
		// LineAmountTotal);
	}

	private static void insertEJLine(String calledFrom, String journalTxnDate2, String journalTxnTime2,
			int journalTxnID2, int journalTxnLineNumber2, float journalTxnLineQuantity2, String journalTxnLineCategory2,
			float journalTxnLineAmount2, float journalTxnLineDiscountPCT2, float journalTxnLineDiscount2) {

		LineAmountTotal = LineAmountTotal + journalTxnLineAmount2;

		if (journalTxnLineQuantity2 == 0) {
			System.out.println(" zero line quantity: 0000000000000000000000" + "|, called from" + calledFrom);
			System.out.println("|" + line + "|");
		}

		if (journalTxnLineCategory2.matches(regexDEPT)) {
			System.out.println("Category is a DEPT |" + journalTxnLineCategory2 + "|, called from" + calledFrom);
			System.out.println("|" + line + "|");
		}
		if (!((journalTxnLineCategory2.matches(regexAlphaSpace) || (journalTxnLineCategory2.matches(regexDEPT))))) {
			System.out
					.println("Category is not Alphabetic |" + journalTxnLineCategory2 + "|, called from" + calledFrom);
			System.out.println("|" + line + "|");
			JournalTrace(currentFilename, "non-alpha Category");
			userEntry = input.nextLine();
		}
		System.out.println(journalTxnDate2 + " " + journalTxnTime2 + " " + journalTxnID2 + " " + journalTxnLineNumber2
				+ " " + journalTxnLineQuantity2 + " " + journalTxnLineCategory2 + " " + journalTxnLineAmount2 + " "
				+ journalTxnLineDiscountPCT2 + " " + journalTxnLineDiscount2 + " called from " + calledFrom);
		lineCount++;
		// STEP 4: Execute a query
		System.out.println("Creating statement...");
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String sql;
		sql = "insert into " + ejTxnTablename + "(" + "LineDateTime, " + "ID, " + "LineNumber, " + "Quantity, "
				+ "Category, " + "Amount, " + "DiscountPCT, " + "Discount" + ") VALUES (" + "'" + journalTxnDate2 + " "
				+ journalTxnTime2 + ":00', " + journalTxnID2 + "," + journalTxnLineNumber2 + ", "
				+ journalTxnLineQuantity2 + ", '" + journalTxnLineCategory2 + "', " + journalTxnLineAmount2 + ", "
				+ journalTxnLineDiscountPCT2 + ", " + journalTxnLineDiscount2 + " " + ");";

		int result = 0;
		try {
			result = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			printSQLException(e);
			e.printStackTrace();
		}
		JournalTxnLineDiscountPCT = 0;
		JournalTxnLineDiscount = 0;
		if (result > 1) {
			System.out.println("Executed insert into " + ejTxnTablename + ", result= " + result);
		}
		try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void JournalTrace(String fin, String traceLocation2) {
		traceCount++;
		if (!checked.contains(traceLocation2)) {
			System.out.println("+++++ " + traceLocation2 + " ++++ file is " + fin);
			// System.out.println("trace location is " + traceLocation2);
			// System.out.println("previous line was |" + prevLine +
			// "|==================");
			// prevTokens = prevLine.split(regexSpace);
			// for (int i = 0; i < prevTokens.length; i++)
			// System.out.println(i + ":'" + prevTokens[i] + "'");
			// System.out.println("# prevTokens:'" + prevTokens.length + "'");
			System.out.println("|" + line + "|-------------------");
			for (int i = 0; i < tokens.length; i++)
				System.out.print(i + ":'" + tokens[i] + "'");
			System.out.println("# Tokens:'" + tokens.length + "'");
			// System.out.println("Review the debug information");
			// userEntry = input.nextLine();
		}
	}

	private static void getNextLine(BufferedReader br) {
		// prevLine = line;
		try {
			morelines = ((line = br.readLine()) != null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tokens = line.split(regexSpace);
		// if (tokens[0].matches(regexTokenXZ)) {
		// try {
		// morelines = ((line = br.readLine()) != null);
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		// tokens = line.split(regexSpace);
		// if (tokens[1].matches(regexInteger)) {
		// try {
		// morelines = ((line = br.readLine()) != null);
		// } catch (IOException e2) {
		// e2.printStackTrace();
		// }
		// tokens = line.split(regexSpace);
		// }
		// }
	}
	
	
	public static void printSQLException(SQLException ex) {

	    for (Throwable e : ex) {
	        if (e instanceof SQLException) {
//	            if (ignoreSQLException(
//	                ((SQLException)e).
//	                getSQLState()) == false) {

	                e.printStackTrace(System.err);
	                System.err.println("SQLState: " +
	                    ((SQLException)e).getSQLState());

	                System.err.println("Error Code: " +
	                    ((SQLException)e).getErrorCode());

	                System.err.println("Message: " + e.getMessage());

	                Throwable t = ex.getCause();
	                while(t != null) {
	                    System.out.println("Cause: " + t);
	                    t = t.getCause();
	                }
	            }
	        }
//	    }
	}

	public static void main(String[] args) {
		String Fdir;
		// This application processes all the EJ*.TXT files generated by
		// a CASIO cash register (SE-S400). They files are stored in a directory
		// and its sub-directories, extracting the data into a MySQL database.
		//

		// openMySQL();

		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
//			Fdir = "/Users/geoffn/Desktop/11 Test CSV";
			// Fdir = "/Users/geoffn/Desktop/CASIO26122016";
			 Fdir = "/Users/geoffn/Desktop/CASIO08012017";
			// Fdir = selectDirectory(Fdir);

			System.out.println("Hello (process directory now)!");
			processDirectory(Fdir);
			System.out.println("Count of Txns is " + txnCount);
			System.out.println("Count of lines is " + lineCount);
			System.out.println("Count of mismatched Txns is " + mismatchCount);
			System.out.println("Count of unidentified lines is " + UnrecognisedCount);
			System.out.println("Trace Count is " + traceCount);
			System.out.println("Goodbye (process directory now)!");

			conn.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		System.out.println("Goodbye!");

		// System.exit(1);
	}
}
