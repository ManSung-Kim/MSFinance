package com.gtk.msfinance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import Utils.Prt;

public class Main {

	static ArrayList<Stock> listStock;
		
	public static void main(String[] args) {

		try {
			Prt.w(HttpAction.sendGet("http://dart.fss.or.kr/dsaf001/main.do?rcpNo=20150522000085"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		listStock = new ArrayList<Stock>();

		listStock.add(new Stock("썔바이오","049960"));
		listStock.add(new Stock("삼성전자","005930"));
		//..
		
		for(int i=0; i<listStock.size(); i++) {
			Stock stock = listStock.get(i);
			
			stock.updateYearReport();
			
			Prt.w(stock.getName());
			int reportSize = stock.getYearReportCnt();
			for(int j =0; j < reportSize; j++) {
				String out = stock.getYear(j) + "    " + stock.getYearProfit(j);
				Prt.w(out);
			}
		}
			
	}

}
