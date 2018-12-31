package com.gtk.msfinance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import DocMgr.CsvMgr;
import Utils.Prt;

public class Main {

	static ArrayList<Stock> listStock;
		
	public static void main(String[] args) {
	
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
