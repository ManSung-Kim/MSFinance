package com.gtk.msfinance;

import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Utils.Prt;

public class Stock {
	public String name;
	public String crp_cd; // 종목코드
	
	private String url;
	
	private ArrayList<YearReport> arrYearReport;
	
	public Stock(String name, String crp_cd) {
		this.name = name;
		this.crp_cd = crp_cd;
		
		arrYearReport = new ArrayList<YearReport>(); 
		
		makeUrl(crp_cd);		
	}
	
	public String getName() {
		return name;
	}
	
	private void makeUrl(String crp_cd) {
		url = "http://dart.fss.or.kr/api/search.json?auth=bda9679be5e210e6ec2eda39ea350bacade99cd8"
				+"&crp_cd="+crp_cd+"&start_dt=19990101&fin_rpt=Y&bsn_tp=A001&page_set=100&page_no=1";
	}
	
	public void updateYearReport(){
		String strYearReportJson = "";
		try {
			strYearReportJson = HttpAction.sendGet(url);
			
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(strYearReportJson);
			
			int reportCnt = Integer.parseInt(jsonObject.get("total_count") + "");
			
			JSONArray reportList = (JSONArray)jsonObject.get("list");
			int reportSize = reportList.size();
			for(int i = 0 ; i < reportSize; i++) {
				JSONObject report = (JSONObject) reportList.get(i);
				String rcpNo = report.get("rcp_no") + "";
				String strReportMainHTML = HttpAction.sendGet(getReportMainJson(rcpNo));
				
				//Thread.sleep(2000);
				
				/*
				 * 
treeNode2 = new Tree.TreeNode({text: "1. 요약재무정보",id: "12",cls: "text",listeners: {click: function() {
viewDoc(
'20180402000415', //rcpNo
'6044090', //dcmNo
'12', //eleId
'158316', // offset 
'14733', // length
'dart3.xsd' // dtd
);}}});
				 * 
				 */
				strReportMainHTML = strReportMainHTML.replaceAll("\\s", "");
				String[] sp1 = strReportMainHTML.split("재무에관한사항");
				if(sp1.length <= 1 )
					continue;
				String[] sp2 =sp1[1].split("}");
				String[] sp3 =sp2[0].split("viewDoc");
				String params = sp3[1].replaceAll("\\(","");
				params = params.replaceAll("\\)","");
				params = params.replace(" ", "");
				params = params.replace("'", "");
				params = params.replace(";", "");
				String[] urlParams =params.split(","); // 
				
				// year
				String year = urlParams[0].substring(0, 4);				
				
				// documents
				Document doc = getDocument(urlParams);
				
				if(doc == null)
					continue;
				
				// table elements
				Elements tblElements = getTableElements(doc, "table tbody tr td");
				
				if(tblElements == null)
					continue;
				
				// year profit
				String strYearProfit = getYearProfit(tblElements);
								
				if(strYearProfit == null || strYearProfit == "")
					continue;
				
				// year report instance
				YearReport yearReport = new YearReport(year, strYearProfit);
				
				// add year report item
				arrYearReport.add(yearReport);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			//strYearReportJson;
		}
	}
	
	private Elements getTableElements(Document doc, String form) {		
		return doc.select("table tbody tr td");
	}
	
	private final String STR_DART_FINANCIAL_STATEMENTS_URL_PREFIX = "http://dart.fss.or.kr/report/viewer.do?rcpNo=";
	private Document getDocument(String[] urlParams) {
		Document doc = null;
		
		String strUrl = STR_DART_FINANCIAL_STATEMENTS_URL_PREFIX + urlParams[0]
				+ "&dcmNo=" + urlParams[1]
						+ "&eleId=" + urlParams[2]
								+ "&offset=" + urlParams[3]
										+ "&length=" + urlParams[4] 
												+ "&dtd=" + urlParams[5];
		
		try {
			doc = Jsoup.connect(strUrl).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return doc;
	}
	
	private final String STR_YEAR_PROFIT_KEY = "영업이익";
	private String getYearProfit(Elements elements) {
		return getTableItem(STR_YEAR_PROFIT_KEY, elements);		
	}
	
	private String getTableItem(String key, Elements elements) {
		String strItem = "";
		for(Element item : elements) {
			if(item.text().contains(key)) {
				strItem = item.nextElementSibling().text();
				strItem = strItem.replace(",", "");
				//Prt.w(profitTd);
				break;
			}
		}
		return strItem;
	}
	
	private final String STR_REPORT_MAIN_PREFIX = "http://dart.fss.or.kr/dsaf001/main.do?rcpNo=";
	private String getReportMainJson(String rcpNo) {
		return STR_REPORT_MAIN_PREFIX + rcpNo;
	}
	
	public int getYearReportCnt() {
		return arrYearReport.size();
	}

	public String getYear(int idx) {
		return arrYearReport.get(idx).getYear();
	}
	
	public String getYearProfit(int idx) {
		return arrYearReport.get(idx).getYearProfit();
	}
}

class YearReport {
//	private String rcpNo;
//	private String dcmNo;
//	private String eleId;
//	private String offset;
//	private String length;
//	private String dtd;
	
	private String url;
	
	// Report
	private String mYear;
	private String mStrYearProfit;
	
	private final String STR_AND = "&";
	private final String URL_PREFIX = "http://dart.fss.or.kr/report/viewer.do?";
	private final String STR_RCPNO = "rcpNo=";
	private final String STR_DCMNO = "dcmNo=";
	private final String STR_ELEID = "eleId=";
	private final String STR_OFFSET = "offset=";
	private final String STR_LENGTH = "length=";
	private final String STR_DTD = "dtd=";
	
	public YearReport(String year, String yearProfit) {
		mYear = year;
		mStrYearProfit = yearProfit;
	}
	
//	public YearReport(String rcp, String dcm, String ele, String offset,
//			String length, String dtd) {
//		rcpNo = rcp;
//		dcmNo = dcm;
//		eleId = ele;
//		this.offset = offset;
//		this.length = length;
//		this.dtd = dtd;
//		
//		makeUrl(rcp, dcm, ele, offset, length, dtd);
//	}
	
//	private void makeUrl(String rcp, String dcm, String ele, String offset,
//			String length, String dtd) {
//		url = URL_PREFIX;
//		url += (STR_RCPNO + rcpNo);
//		url += (STR_AND + STR_DCMNO + dcmNo);
//		url += (STR_AND + STR_ELEID + eleId);
//		url += (STR_AND + STR_OFFSET + this.offset);
//		url += (STR_AND + STR_LENGTH + this.length);
//		url += (STR_AND + STR_DTD + this.dtd);		
//	}
	
	public void setYearProfit(String strYProfit) {
		mStrYearProfit = strYProfit;
	}
	
	public String getYear() {
		return mYear;
	}
	
	public String getYearProfit() {
		return mStrYearProfit;
	}
}



