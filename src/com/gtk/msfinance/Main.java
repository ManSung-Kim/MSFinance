package com.gtk.msfinance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.gtk.msfinance.docmgr.CsvMgr;
import com.gtk.msfinance.math.Polynomials;
import com.gtk.msfinance.util.Prt;



public class Main {
			
	public static void main(String[] args) {
	
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {            
            @Override
            public void run() {
                CsvMgr.closeAll();
            }
        }));
		
		String strMenu = "1. record from all stocks\n"
				+"2. analysis1\n"
				+ "select menu : ";
		System.out.print(strMenu);
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		switch(Integer.parseInt(input)) {
		case 1:
			recFromAllStocks();
			break;
		case 2:
			analysis1();
			break;
		default:
			break;	
		}
		
		
	}

	private static void analysis1() { // title not defined yet 
		Polynomials.test(3);
	}
	
	private static void recFromAllStocks() {
		ArrayList<Stock> listStock;
		
		listStock = CsvMgr.getList("csv\\data_stocks.csv", 1);
//		for(int i=0; i<listStock.size(); i++) 
//			Prt.w(i + " " + listStock.get(i).getName() + " " + listStock.get(i).crp_cd);
		
//		listStock = new ArrayList<Stock>();
//
//		listStock.add(new Stock("썔바이오","049960"));
//		listStock.add(new Stock("삼성전자","005930"));
		//..
//		listStock = new ArrayList<Stock>();
//		listStock.add(new Stock("계양전기","012200"));
		
		String strWriteFilePath = "csv\\data" + System.currentTimeMillis() + ".csv";
		CsvMgr.initStaticBufferedWriter(strWriteFilePath);
		
		//List<String> listFinancialStatement = new ArrayList<String>();
		
		String strYearTag = "";
		for(int i = 2018; i >= 1970; i--)
			strYearTag += "," + i;
		//listFinancialStatement.add(strYearTag);
		CsvMgr.writeFileNoneStop(strYearTag);
		int stocksSize = listStock.size();
		for(int i = 0; i < stocksSize; i++) {
			Stock stock = listStock.get(i);
			String strStockCsv = stock.getName();
			
			stock.updateYearReport();
			
			//Prt.w(stock.getName());
			int reportSize = stock.getYearReportCnt();
			String out = "";
			out += i + "/" +stocksSize + " ";
			out += stock.getName() + "] ";
			
			String strYear = "";
			String strYearProfit = "";
			for(int j = 0; j < reportSize; j++) {
				strYear = stock.getYear(j);
				strYearProfit = stock.getYearProfit(j);
				
				out += strYear + "," + strYearProfit + "  ";
				strStockCsv += "," + strYearProfit;
			}
			Prt.w(out);
			//listFinancialStatement.add(strStockCsv);
			CsvMgr.writeFileNoneStop(strStockCsv);
		}
		//CsvMgr.writeFile("csv\\data.csv", listFinancialStatement);
		CsvMgr.closeAll();
	}
	
}
