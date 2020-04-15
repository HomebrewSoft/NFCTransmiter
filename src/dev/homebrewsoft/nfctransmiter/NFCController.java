package dev.homebrewsoft.nfctransmiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NFCController {
	/**
	 * 
	 * @return false if device not found, true otherwise
	 */
	public static boolean waitForDevice() {
		ProcessBuilder processGetURL = null;
		// check if system is windows
		if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
			processGetURL = new ProcessBuilder("cmd.exe", "/c", "java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo");
		}
		else {
			processGetURL = new ProcessBuilder("bash", "-c", "java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo");
		}
		boolean deviceFound = false;
		try {
            Process process = processGetURL.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("Esperando dispositivo");
            while ((line = reader.readLine()) != null) {
                if (line.equals("DEBUG: org.nfctools.spi.acs.InitiatorTerminalTagScanner - Waiting while card present")) {
                    deviceFound = true;
                	break;
                }
            }
            System.out.println(deviceFound ? "Dispositivo encontrado" : "Error en el proceso de búsqueda de dispositivo");
            process.destroy();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
		return deviceFound;
	}
	
	
	/**
	 * 
	 * @param url String to be sent in URL format to device
	 * @return true if URL successfully sent, false otherwise
	 */
	public static boolean sendURL(String url) {
		ProcessBuilder processSendURL = null;
        if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
        	processSendURL = new ProcessBuilder("cmd.exe", "/c","java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo -url " + url + " -target");
        }
        else {
        	processSendURL = new ProcessBuilder("bash", "-c","java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo -url " + url + " -target");
        }
		boolean urlSuccessfullySent = false;
		try {
            Process process = processSendURL.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("Enviando URL");
            while ((line = reader.readLine()) != null) {
            	System.out.println(line);
                if (line.equals("INFO : org.nfctools.examples.snep.SnepAgentListenterImpl - SNEP succeeded")) {
                    urlSuccessfullySent = true;
                	break;
                }
                else if(line.equals("javax.smartcardio.CardException: connect() failed") ) {
                	System.out.println("Preparando lector de tarjetas. Por favor aleje el dispositivo e intente de nuevo.");
                	break;
                }
                else if (line.indexOf("No supported card terminal found") > -1) {
                	System.out.println("No se ha encontrado lector de tarjetas válido");
                	break;
                }
            }
            process.destroy();
            if (urlSuccessfullySent) {
            	System.out.println("URL enviada");      	
            }
        }
		catch (IOException e) {
            e.printStackTrace();
        }
		return urlSuccessfullySent;
	}
}
