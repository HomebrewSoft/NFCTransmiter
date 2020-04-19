package dev.homebrewsoft.nfctransmiter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JTextArea;

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
	public static boolean sendURL(String url, JTextArea output) {
		ProcessBuilder processSendURL = null;
        if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
        	processSendURL = new ProcessBuilder("cmd.exe", "/c", "java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo -url " + url + " -target");
        }
        else {
        	processSendURL = new ProcessBuilder("bash", "-c", "java -cp lib/nfctools-examples.jar org.nfctools.examples.snep.SnepDemo -url " + url + " -target");
        }
		boolean urlSuccessfullySent = false;
		Process process = null;
		
		try {
            process = processSendURL.start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            String line;
			output.append("Preparando dispositivo para enviar URL.\n");
            boolean toggleDeviceReady = false;
            Thread timer = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(3000);
						output.append("No se encontró dispositivo NFC. Inténtelo de nuevo.\n");
	                	try {
							writer.write("\n");
							writer.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (InterruptedException e) {
						//e.printStackTrace();
					}
				}
			});
            timer.start();
            while (((line = reader.readLine()) != null)) {
            	System.out.println(line);
                if (line.indexOf("SNEP succeeded") > -1) {
                    urlSuccessfullySent = true;
                	writer.write("\n");
                	writer.flush();
                }
                else if(line.indexOf("connect() failed") > -1) {
                	output.append("Preparando lector de tarjetas. Por favor aleje el dispositivo e intente de nuevo.\n");
                	writer.write("\n");
                	writer.flush();
                }
                else if (line.indexOf("No supported card terminal found") > -1) {
                	output.append("No se ha encontrado lector de tarjetas válido\n");
                	writer.write("\n");
                	writer.flush();
                }
                else if ((line.indexOf("FF0000002DD48C0008001234564001FE0100000000000000000000000000FFFF01FE01000000000000000646666D01011000") != -1) && !toggleDeviceReady) {
                	output.append("Todo listo para el envío, acerce el dispisitivo a la terminal.\n");
                	timer.interrupt();
                	toggleDeviceReady = true;
                }
            }
			reader.close();
			writer.close();
        }
		catch (IOException e) {
            e.printStackTrace();
        }
		finally {
			process.destroy();
		}
		return urlSuccessfullySent;
	}
}
