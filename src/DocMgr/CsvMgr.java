package DocMgr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.spec.PSSParameterSpec;
import java.util.ArrayList;

import com.gtk.msfinance.Stock;

public class CsvMgr {
	
	static public <T extends Object> ArrayList<T> getList(String path, int categoryCnt) {
		ArrayList<T> list = new ArrayList<T>(); 
		
		int passCnt = categoryCnt;
		
		File file = null;
		BufferedReader br = null;
		
		try {

			file = new File(path);		
			br = new BufferedReader(new FileReader(file));

			for(int i = 0; i< passCnt; i++)
				br.readLine();

			String line;			
			while((line = br.readLine()) != null) {
				String[] arr = line.split(",");
				while(arr[0].length() < 6)
				{
					arr[0] = "0" + arr[0];
				}
				
				list.add((T) new Stock(arr[1], arr[0]));
			}
			
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				br.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return list;
	}
}
