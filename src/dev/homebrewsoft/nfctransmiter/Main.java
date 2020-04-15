package dev.homebrewsoft.nfctransmiter;

import java.util.HashMap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Main {
	
	static String url = "https://nfc.homebrewsoft.dev";
    static String db = "nfc";
    static String username = "admin";
    static String password = "admin";

    static String COMMERCE_ID = "1";
    static String POS_ID = "1";
    static String CLIENT_ID = "1";
    static int CONFIG_ID = 1;
    
    private static int uid;
    static int last_order_id = -1;
    static String last_url = "";
    
    public static void loadConfig() {
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream("config.properties");
			prop.load(in);
			url = prop.getProperty("url");
			db = prop.getProperty("db");
			username = prop.getProperty("username");
			password = prop.getProperty("password");
			COMMERCE_ID = prop.getProperty("COMMERCE_ID");
			POS_ID = prop.getProperty("POS_ID");
			CLIENT_ID = prop.getProperty("CLIENT_ID");
			CONFIG_ID = Integer.parseInt(prop.getProperty("CONFIG_ID"));
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	public static Object getLastPoSOrder(XmlRpcClient models) throws IndexOutOfBoundsException {
		try {
			return Arrays.asList((Object[]) models.execute("execute_kw", Arrays.asList(db, uid, password, "pos.order", "search_read",
			        Arrays.asList(Arrays.asList(Arrays.asList("session_id.config_id", "=", CONFIG_ID))), new HashMap() {
			            {
			                put("fields", Arrays.asList("id", "date_order", "company_id", "session_id", "account_move", "note",
			                        "amount_tax", "amount_total"));
			                put("limit", 1);
			                put("order", "date_order desc");
			            }
			        }))).get(0);
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
        return null;
    }
	

    public static Object getCompany(XmlRpcClient models, int company_id) throws IndexOutOfBoundsException {
        try {
            return Arrays.asList((Object[]) models.execute("execute_kw", Arrays.asList(db, uid, password, "res.company",
                    "search_read", Arrays.asList(Arrays.asList(Arrays.asList("id", "=", company_id))), new HashMap() {
                        {
                            put("fields", Arrays.asList("name", "street", "street2", "vat"));
                            put("limit", 1);
                        }
                    }))).get(0);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Object> getLines(XmlRpcClient models, int order_id) {
        try {
            return Arrays.asList((Object[]) models.execute("execute_kw",
                    Arrays.asList(db, uid, password, "pos.order.line", "search_read",
                            Arrays.asList(Arrays.asList(Arrays.asList("order_id", "=", order_id))), new HashMap() {
                                {
                                    put("fields", Arrays.asList("product_id", "qty", "price_unit", "discount",
                                            "price_subtotal"));
                                }
                            })));
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Object> getPayments(XmlRpcClient models, int order_id) {
        try {
            return Arrays.asList((Object[]) models.execute("execute_kw",
                    Arrays.asList(db, uid, password, "pos.payment", "search_read",
                            Arrays.asList(Arrays.asList(Arrays.asList("pos_order_id", "=", order_id))), new HashMap() {
                                {
                                    put("fields", Arrays.asList("currency_id", "amount"));
                                }
                            })));
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String toJSON(HashMap<String, Object> order, HashMap<String, Object> company, List<Object> lines, List<Object> payments) {
    	List<String> parsedLines = new ArrayList();
    	for (Object line : lines) {
    		HashMap<String, Object> hashLine = (HashMap<String, Object>) line;
    		parsedLines.add("{" + 
    				"\"name\": \"" +  ((Object[])hashLine.get("product_id"))[1] + "\"," +
    				"\"units\": " +  hashLine.get("qty") + "," +
    				"\"units_price\": " +  hashLine.get("price_unit") + "," +
    				"\"units_type\": " +  "\"unidades\"" + "," +
    				"\"discount\": " +  hashLine.get("discount") + "," +
    				"\"total_amount\": " +  hashLine.get("price_subtotal") + 
    				"}");
    	}
    	
    	double amount = 0.0;
    	double amount_return = 0.0;
    	for (Object payment : payments) {
    		HashMap<String, Object> hashPay = (HashMap<String, Object>) payment;
    		if ((double)hashPay.get("amount") > 0.0) {
    			amount += (double)hashPay.get("amount");
    		}
    		else {
    			amount_return += (double)hashPay.get("amount");
    		}
    	}
    	int invoice_id = 0;
    	try{
    		System.out.println((order.get("account_move")));
    		invoice_id = (int)(((Object[])order.get("account_move"))[0]);
    	}
    	catch (IndexOutOfBoundsException e) {
    		e.printStackTrace();
    	}
    	catch (ClassCastException e) {
    		e.printStackTrace();
    	}
    	return "{" + 
    		    "\"id\":" + order.get("id") + "," +
    		    "\"commerce\": {" +
    		        "\"name\":\"" + company.get("name") + "\"," +
    		        "\"address\":\"" + company.get("street") + "\"," +
    		        "\"fiscal_id\":\"" + company.get("vat") + "\"," +
    		        "\"commerce_id\":" + COMMERCE_ID + "," +
    		        "\"pos_id\":" + POS_ID + "," +
    		        "\"client_id\":" + CLIENT_ID +
    		    "}," +
    		    "\"date\": {" +
    		        "\"created\":\"" + order.get("date_order") + "\"" +
    		    "}," +
    		    "\"invoice_id\":" + invoice_id + "," +
    		    "\"products\":" + parsedLines.toString() + "," +
    		    "\"total\": {" +
    		        "\"currency\": \"EUR\"," +
    		        "\"tax\":" + order.get("amount_tax") + "," +
    		        "\"has_tax\":" + ((double)order.get("amount_tax") != 0.0) + "," +
    		        "\"subtotal\":" + ((double)order.get("amount_total") - (double)order.get("amount_tax")) + "," +
    		        "\"total\":" + order.get("amount_total") +
    		    "}," +
    		    "\"payment\": {" +
    		        "\"type\": \"cash\"," +
    		        "\"amount\":" + amount + "," +
    		        "\"amount_return\":" + amount_return +
    		    "}," +
    		    "\"message\":\"" + order.get("note") + "\"" +
    		"}";
    }
    
    public static String getTargetURL(String json) {
    	return json.split("\"")[3];
    }
    
	public static void main(String[] args) {
		loadConfig();
		// System.out.println(System.getProperty("os.name"));
		final XmlRpcClient client = new XmlRpcClient();
		final XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
	    try {
			common_config.setServerURL(new URL(String.format("%s/xmlrpc/common", url)));
			uid = (int) client.execute(common_config, "authenticate", Arrays.asList(db, username, password, Collections.emptyMap()));
		    XmlRpcClient models;
		    System.out.println("Loggeado");
			models = new XmlRpcClient() {
			    {
			        setConfig(new XmlRpcClientConfigImpl() {
			            {
			                setServerURL(new URL(String.format("%s/xmlrpc/object", url)));
			            }
			        });
			    }
			};
			HashMap<String, Object> lastOrder = (HashMap<String, Object>) getLastPoSOrder(models);
			int order_id = (int)(lastOrder.get("id"));
			if (last_order_id != order_id) {
				System.out.println("Generando nueva URL");
				int company_id = (int)((Object[])lastOrder.get("company_id"))[0];
				HashMap<String, Object> company = (HashMap<String, Object>) getCompany(models, company_id);
				List<Object> lines = getLines(models, order_id);
				List<Object> payments = getPayments(models, order_id);
				String jsonBody = toJSON(lastOrder, company, lines, payments);
				//System.out.println(jsonBody);
				APIQuery apiquery = new APIQuery("https://europe-west1-freetix-dev.cloudfunctions.net/api/invoices", jsonBody);
				String response = apiquery.getResponse();
				//System.out.println(response);
				last_url = getTargetURL(response);
			}
			
			System.out.println("URL lista: " + last_url);
			last_order_id = order_id;
			System.out.println(NFCController.sendURL(last_url));
	    }
	    catch (MalformedURLException e) {
			e.printStackTrace();
	    }
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
	    catch (IndexOutOfBoundsException e) {
	    	e.printStackTrace();
	    }
	}

}
