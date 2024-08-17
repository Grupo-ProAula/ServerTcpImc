package servertcpimc.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import servertcpimc.models.CalculateIMC;
import servertcpimc.views.MainView;

public class ThreadClient extends Thread{
    private Socket client;
    private String ip;
    private MainView view;

    public ThreadClient(Socket client, MainView view) {
        this.client = client;
        this.view = view;
        this.ip = client.getInetAddress().getHostAddress();
    }
    
    @Override
    public void run(){
        try{
            CalculateIMC.Imc imc = CalculateIMC();
            sendResponse(imc);
        }catch(Exception ex){
            System.out.println(log() + ex.getMessage());
            view.getTxtLog().append(log() + ex.getMessage() + "\n");
            try{
                client.close();
            }catch(IOException e){
                ServerTCP.clientList.remove(ip);
            }finally{
                ServerTCP.clientList.remove(ip);
            }
        }
    }
    
    public CalculateIMC.Imc CalculateIMC() throws Exception {
        DataInputStream input = null;
        try {
            input = new DataInputStream(client.getInputStream());
            String msg = "Esperando el PESO: ";
            System.out.println(log() + msg);
            view.getTxtLog().append(log()+msg + "\n" + "\n");
            float weight = input.readFloat();
            msg = "PESO: " + weight;
            System.out.println(log() + msg);
            view.getTxtLog().append(log() + msg + "\n");
            msg = "Esperando La Altura: ";
            System.out.println(log()+msg);
            view.getTxtLog().append(log() + msg + "\n");
            float height = input.readFloat();
            msg = "ALTURA: " + height;
            System.out.println(log()+msg);
            view.getTxtLog().append(log() + msg + "\n");
            CalculateIMC dataImc = new CalculateIMC(weight, height);
            System.out.println(log() + "IMC: " + dataImc.getImc().result);
            msg = "IMC: " + dataImc.getImc().result;
            System.out.println(log() + msg);
            view.getTxtLog().append(log() + msg + "\n");
            System.out.println(log() + "MENSAJE: " + dataImc.getImc().message);
            msg = "MENSAJE: " + dataImc.getImc().message;
            System.out.println(log() + msg);
            view.getTxtLog().append(log() + msg + "\n");
            return dataImc.getImc();
        } catch (IOException ex) {
            String msg = "Error al caputurar datos del cliente " + ip;
            System.out.println(log() + msg);
            view.getTxtLog().append(log() + msg + "\n");
            throw new Exception("Error al caputurar datos del cliente " + ip);
        }
    }
    
    public void sendResponse(CalculateIMC.Imc imc) {
        Thread threadRespose = new Thread() {
            @Override
            public void run() {
                DataOutputStream output = null;
                try {
                    output = new DataOutputStream(client.getOutputStream());
                    output.writeFloat(imc.result);
                    output.writeUTF(imc.message);
                    String msg = "IMC: " + imc.result;
                    System.out.println(log() + msg);
                    view.getTxtLog().append(log()+ msg + "\n");
                    msg = "MENSAJE: " + imc.message;
                    System.out.println(log() + msg);
                    view.getTxtLog().append(log() + msg + "\n");
                    output.flush();
                    sendResponse(CalculateIMC());
                } catch (IOException ex) {
                    String msg = "Error al enviar datos al cliente " + ip;
                    System.out.println(log() + msg);
                    view.getTxtLog().append(log() + msg + "\n");
                    ServerTCP.clientList.remove(ip);
                } catch (Exception ex) {
                    String msg = "Error al leer datos del cliente " + ip;
                    System.out.println(log() + msg);
                    view.getTxtLog().append(log() + msg + "\n");
                    try {
                        client.close();
                    } catch (IOException ex1) {
                        ServerTCP.clientList.remove(ip);
                    } finally {
                        ServerTCP.clientList.remove(ip);
                    }
                }
            }
        };
        threadRespose.start();
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }
        
    public String log() {
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        return ip + " -> " + f.format(new Date()) + " - ";
    }
    
}
