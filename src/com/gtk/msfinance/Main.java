package com.gtk.msfinance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
		int stocksSize = listStock.size();
		for(int i = 0; i < stocksSize; i++) {
			Stock stock = listStock.get(i);
			
			stock.updateYearReport();
			
			//Prt.w(stock.getName());
			int reportSize = stock.getYearReportCnt();
			String out = "";
			out += i + "/" +stocksSize + " ";
			out += stock.getName() + "] ";
			for(int j =0; j < reportSize; j++) {
//				String out = stock.getYear(j) + "년]    영업이익:" + stock.getYearProfit(j)+", "
//						+ " 판관비:"+stock.getSNGA(j);
//				Prt.w(out);
				out += stock.getYear(j) + "," + stock.getYearProfit(j) + "  ";
			}
			Prt.w(out);
		}
			
	}

}
