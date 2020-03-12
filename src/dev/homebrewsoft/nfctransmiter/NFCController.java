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
		ProcessBuilder processGetURL = new ProcessBuilder("cmd.exe", "/c", "java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo");
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
		ProcessBuilder processSendURL = new ProcessBuilder("cmd.exe", "/c","java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo -url " + url + " -target");
        boolean urlSuccessfullySent = false;
		try {
            Process process = processSendURL.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("Enviando URL");
            while ((line = reader.readLine()) != null) {
                if (line.equals("INFO : org.nfctools.examples.snep.SnepAgentListenterImpl - SNEP succeeded")) {
                    urlSuccessfullySent = true;
                	break;
                }
            }
            process.destroy();
            System.out.println(urlSuccessfullySent ? "URL enviada, espere 5 segundos para la siguiente transaccion" : "Error en transmisión, intentando de nuevo");
            Thread.sleep(5000);
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		return urlSuccessfullySent;
	}
}
