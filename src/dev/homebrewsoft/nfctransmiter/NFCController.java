package dev.homebrewsoft.nfctransmiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NFCController {
	public static void waitForDevice() {
		ProcessBuilder processGetURL = new ProcessBuilder("bash", "-c", "java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo");
        try {
            Process process = processGetURL.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("Esperando dispositivo");
            while ((line = reader.readLine()) != null) {
                if (line.equals("DEBUG: org.nfctools.spi.acs.InitiatorTerminalTagScanner - Waiting while card present")) {
                    break;
                }
            }
            System.out.println("Dispositivo encontrado");
            process.destroy();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static void sendURL(String url) {
		ProcessBuilder processSendURL = new ProcessBuilder("bash", "-c","java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo -url " + url + " -target");
        try {
            Process process = processSendURL.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("Enviando URL");
            while ((line = reader.readLine()) != null) {
                if (line.equals("INFO : org.nfctools.examples.snep.SnepAgentListenterImpl - SNEP succeeded")) {
                    break;
                }
            }
            process.destroy();
            System.out.println("URL enviada, espere 5 segundos para la siguiente transaccion");
            Thread.sleep(5000);
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
}
