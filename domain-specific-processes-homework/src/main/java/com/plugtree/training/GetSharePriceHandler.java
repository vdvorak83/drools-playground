package com.plugtree.training;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class GetSharePriceHandler implements WorkItemHandler {

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		// invoke yahoo service
		String symbol = (String) workItem.getParameter("symbol");
		if (symbol == null) {
			symbol = "GOOG";
		}
		
		try {
			String value = doCall(buildURI(symbol));
			Map<String, Object> results = workItem.getResults();
			results.put("value", Double.valueOf(value).intValue());
			manager.completeWorkItem(workItem.getId(), results);
		} catch (Exception e) {
			manager.abortWorkItem(workItem.getId());
		}
		
	}

	private String responseToString(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		reader.readLine();
		String line = reader.readLine();
		return line.split(",")[4];
	}
	
	private String doCall(String uri) throws Exception {
		HttpClient httpClient = new HttpClient();
		HttpMethod getMethod = new GetMethod(uri);
		int response = httpClient.executeMethod(getMethod);
 		if (response != 200) {
			throw new Exception("HTTP problem, httpcode: " + response);
		}
		InputStream stream = getMethod.getResponseBodyAsStream();
		String responseText = responseToString(stream);
		return responseText;
	}
	
	private String buildURI(String symbol) {
		StringBuilder uri = new StringBuilder();
		uri.append("http://ichart.finance.yahoo.com/table.csv");
		uri.append("?s=").append(symbol);
		return uri.toString();
	}
	
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
	}

}
