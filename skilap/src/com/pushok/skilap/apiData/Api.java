package com.pushok.skilap.apiData;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import android.util.Log;

import com.pushok.skilap.exception.ApiException;
import com.pushok.skilap.exception.LoginException;
import com.pushok.skilap.exception.ScilapException;
import com.pushok.skilap.exception.TokenException;

public class Api {
	protected static class PresetHttpParams {
		public static BasicHttpParams getInstance() {
			BasicHttpParams obj = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(obj, 5000);
			HttpConnectionParams.setSoTimeout(obj, 10000);
			return obj;
		}
	}
	protected static String url;
	protected static String login;
	protected static String password;
	protected static String token = null;
	protected static String clientId = "";
	protected static DefaultHttpClient httpclient = new DefaultHttpClient(PresetHttpParams.getInstance());
    public static String getUrl() {
		return url;
	}
	public static void setUrl(String url) {
		Api.url = url;
//		url = "http://10.0.2.2/jsonrpc";
	}
	public static void setLogin(String login) {
		Api.login = login;
	}
	public static String getLogin() {
		return login;
	}
	public static void setPassword(String password) {
		Api.password = password;
	}
	public static String getPassword() {
		return password;
	}
	public static String getToken() {
		return token;
	}
	public static void setToken(String token) {
		Api.token = token;
	}
	public static void setClientId(String clientId) {
		if (clientId == null)
			Api.clientId = "";
		else
			Api.clientId = clientId;
	}
	public static String requestToken() throws ScilapException {
		RequestData request = new RequestData();
		request.id = 1;
		request.method = "core.getApiToken";
		request.params = new Object[] {"", clientId, ""};
		
		ResponseData response = request(request);
		if (response == null || response.error != null) {
			token = null;
			throw new TokenException("Wrong token recived");
		}
		
		return token = response.result.get(0).asText();
	}
	public static String requestLogin(String login, String password) throws ScilapException {
		Api.login = login;
		Api.password = password;
		requestToken();
		RequestData request = new RequestData();
		request.id = 1;
		request.method = "core.loginByPass";
		request.params = new Object[] {token, login, password};
		
		ResponseData response = request(request);
		if (response == null || response.error != null) {
			token = null;
			throw new LoginException("Can't login");
		}
		JsonNode sm = response.result.get(0);
		String name = sm.get("screenName").asText();
		if (name == null)
			name = sm.get("firstName").asText();
		if (name == null)
			name = sm.get("login").asText();
		return name;
	}
	public static void reLogin() throws ScilapException {
		requestLogin(login, password);
	}
	public static JsonNode requestAllAccounts() throws ScilapException {
		if (token == null) reLogin();
		RequestData request = new RequestData();
		request.id = 1;
		request.method = "cash.getAllAccounts";
		request.params = new Object[] {token};
		
		ResponseData response = request(request);
		if (response == null || response.error != null) {
			token = null;
			throw new ApiException();
		}
		
		return response.result.get(0);
	}
	public static JsonNode requestAccountInfo(Integer id, String[] params) throws ScilapException {
		if (token == null) reLogin();
		RequestData request = new RequestData();
		request.id = 1;
		request.method = "cash.getAccountInfo";
		request.params = new Object[] {token, id, params};
		
		ResponseData response = request(request);
		if (response == null || response.error != null) {
			token = null;
			throw new ApiException();
		}
		
		return response.result.get(0);
	}
	public static JsonNode requestAccountRegister(Integer id, Integer offset, Integer limit) throws ScilapException {
		if (token == null) reLogin();
		RequestData request = new RequestData();
		request.id = 1;
		request.method = "cash.getAccountRegister";
		request.params = new Object[] {token, id, offset, limit};
		
		ResponseData response = request(request);
		if (response == null || response.error != null) {
			token = null;
			throw new ApiException();
		}
		
		return response.result.get(0);
	}
	public static String saveTransaction(SaveSplitsObjectData transaction, String accId) throws ScilapException {
		if (token == null) reLogin();
		RequestData request = new RequestData();
		request.id = 1;
		request.method = "cash.saveTransaction";
		request.params = new Object[] {token, transaction, accId};
		
		ResponseData response = request(request);
		if (response == null) {
			token = null;
			throw new ApiException();
		}
		if (response.error != null) {
			throw new ApiException(response.error.get("message").asText());
		}
		
		return "Ok";
	}
	public static JsonNode requestForEach(String name, Object obj, String params) throws ScilapException {
		if (token == null) reLogin();
		RequestBatchData batch = new RequestBatchData();
		batch.method = name;
		batch.object = obj;
		batch.params = "[\"" + token + "\"," + params.substring(1);

		RequestData request = new RequestData();
		request.id = 1;
		request.method = "batch";
		request.params = new Object[] {token, batch};
		
		ResponseData response = request(request);
		if (response == null || response.error != null) {
			token = null;
			throw new ApiException();
		}
		
		return response.result;
	}
	public static JsonNode requestBatch(String batch) throws ScilapException {
		if (token == null) reLogin();
		batch = batch.replaceAll("__TOKEN__", token);
		RequestData request = new RequestData();
		request.id = 1;
		request.method = "batch.runBatch";
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			request.params = new Object[] {  mapper.readValue(batch, Object.class) };
		} catch (Exception e) {
			throw new ApiException();
		}

		ResponseData response = request(request);
		if (response == null) {
			token = null;
			throw new ApiException();
		} else
		if (response.error != null) {
			token = null;
			if ("InvalidToken".equals(response.error.get("subject")))
				throw new TokenException(response.error.get("subject").asText());
			throw new ApiException();
		}
		
		return response.result;
	}
	public static ResponseData request(RequestData o) throws ScilapException {
    	try {
    		if (url == null)
    			throw new ApiException("Url is null");
    		HttpPost httppost = new HttpPost("http://" + url + "/jsonrpc");
    		httppost.addHeader("Content-Type", "application/json; charset=UTF-8");
    		httppost.addHeader("Connection", "close");
    		httppost.addHeader("accept-language", "ru-RU");
    		
    		ObjectMapper mapper = new ObjectMapper();
    		String jsonst = mapper.writeValueAsString(o);
    		Log.d("request", jsonst);
    		
    		
    		httppost.setEntity(new StringEntity(jsonst, "UTF-8"));
    		HttpResponse response = httpclient.execute(httppost);
    		
    		StatusLine status = response.getStatusLine();
    		if (status.getStatusCode() != 200) {
    			throw new ApiException("Invalid response from server: " + status.toString());
    		}
    		
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			
			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}
			
			String dataAsString = new String(content.toByteArray());
    		Log.d("response", dataAsString);
			
			ResponseData obj = mapper.readValue(dataAsString, ResponseData.class);
			if (obj.error != null) {
				token = null;
				throw new TokenException(obj.error.findValue("message").asText());
			}
			return obj;
    	} catch (Exception e) {
    		Log.e("Request exception", (e.getMessage() != null)?e.getMessage():e.toString());
    		throw new ApiException((e.getMessage() != null)?e.getMessage():e.toString());
    	}
    }
}
