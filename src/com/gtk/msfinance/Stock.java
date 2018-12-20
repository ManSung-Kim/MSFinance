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
				 *  FORMAT  
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
				
				// url parameters
				String[] urlParams = getUrlParamsWithTockens(strReportMainHTML);
				
				/*
				 * 정정신고가 있는 경우, 재무제표 템플릿이 있는경우, 없는경우, 있어도 이상한경우가 너무 많아서
				 * 정정신고가 발견되면 본문의 최초 사업보고서를 참조함. 코드간결성을위해서..
				 */
				if(urlParams == null || urlParams[0] == null 
						|| strReportMainHTML.contains("정정신고(보고")) { 
					//Prt.w(getName() + " URL NULL로 Return."+i);
					// 첨부에 재무제표가 없고 본문으로 이동해야 사업보고서가 있는 경우
					// find main doc, <optionvalue="rcpNo=20070330002144"title="사업보고서">
					String[] splited = strReportMainHTML.split("\"title=\"사업보고서");
					splited = splited[0].split("rcpNo=");
					rcpNo = splited[splited.length - 1];
					strReportMainHTML = HttpAction.sendGet(getReportMainJson(rcpNo)).replaceAll("\\s", "");
					urlParams = getUrlParamsWithTockens(strReportMainHTML);
					if(urlParams == null || urlParams[0] == null) {
						Prt.w(getName() + " URL NULL로 Return2."+i);
					}
						
				}
				
				// year
				String year = urlParams[0].substring(0, 4);		
				if(year == null) {
					Prt.w(getName() + " year NULL로 Return."+i);
				}
				
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
				
				// selling, general & administrative 판매관리비
				String strSGNA = getSellingGeneralAndAdministrativeExpensive(tblElements); // 판매관리비
				strSGNA = "";
//				if(strSGNA == null || strSGNA == "") { // TODO : wrap to retry
//					urlParams = getUrlParams(strReportMainHTML, "재무제표등");
//					if(urlParams == null)
//						continue;
//					//Prt.w(urlParams[0]+"");
//					year = urlParams[0].substring(0, 4);		
//					doc = getDocument(urlParams);
//					if(doc == null)
//						continue;
//					tblElements = getTableElements(doc, "table tbody tr td");					
//					if(tblElements == null)
//						continue;
//					strYearProfit = getYearProfit(tblElements);					
//					if(strYearProfit == null || strYearProfit == "")
//						continue;
//					strSGNA = getSellingGeneralAndAdministrativeExpensive(tblElements);
//					if(strSGNA == null || strSGNA == "") 
//						continue;
//				}
				
				// year report instance
				YearReport yearReport = new YearReport(year);
				yearReport.setYearProfit(strYearProfit);
				yearReport.setSGNA(strSGNA);
				
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
	
	private String[] getUrlParams(String html, String financitalStatementsKey) {
		String[] sp1 = html.split(financitalStatementsKey);
		if(sp1.length <= 1 )
			return null;
		
		String[] sp2 =sp1[1].split("}");
		String[] sp3 =sp2[0].split("viewDoc");
		String params = sp3[1].replaceAll("\\(","");
		params = params.replaceAll("\\)","");
		params = params.replace(" ", "");
		params = params.replace("'", "");
		params = params.replace(";", "");
		String[] urlParams =params.split(","); // 
		
		return urlParams;
	}
	
	private String[] getUrlParamsWithTockens(String strReportMainHTML) {
		String[] urlParams = null;
		
		int finStateTockenListLen = FinancialStatement.STR_FINANCIAL_STATEMENT_TOKEN_LIST.length;
		for(int finStateTockenIdx = 0; finStateTockenIdx < finStateTockenListLen; finStateTockenIdx++) {
			urlParams = getUrlParams(strReportMainHTML, 
					FinancialStatement.STR_FINANCIAL_STATEMENT_TOKEN_LIST[finStateTockenIdx]);
			if(urlParams != null)
				break;				
		}
		
		return urlParams;
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
	private String getYearProfit(Elements elements) { // 영업이익
		return getTableItem(STR_YEAR_PROFIT_KEY, elements);		
	}
	
	private final String STR_SGNA_KEY = "판매비와관리비";
	private String getSellingGeneralAndAdministrativeExpensive(Elements elements) { // 판관비 SG & A Expensive
		return getTableItem(STR_SGNA_KEY, elements);
	}
	
	private String getTableItem(String key, Elements elements) {
		String strItem = "";
		for(Element item : elements) {
			if(item.text().contains(key)) {
				Element subElements = item.nextElementSibling();
				if(subElements == null)
					break;
				strItem = subElements.text();
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
	
	public String getSNGA(int idx) {
		return arrYearReport.get(idx).getSGNA();
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
	private String mYear; // 연도
	private String mStrYearProfit; // 영업이익
	private String mStrSGNA; // 판관비
	
	private final String STR_AND = "&";
	private final String URL_PREFIX = "http://dart.fss.or.kr/report/viewer.do?";
	private final String STR_RCPNO = "rcpNo=";
	private final String STR_DCMNO = "dcmNo=";
	private final String STR_ELEID = "eleId=";
	private final String STR_OFFSET = "offset=";
	private final String STR_LENGTH = "length=";
	private final String STR_DTD = "dtd=";
	
	public YearReport(String year) {
		mYear = year;
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
	
	public void setSGNA(String strSGNA) {
		mStrSGNA = strSGNA;
	}
	
	public String getYear() {
		return mYear;
	}
	
	public String getYearProfit() {
		return mStrYearProfit;
	}
	
	public String getSGNA() {
		return mStrSGNA;
	}
}



