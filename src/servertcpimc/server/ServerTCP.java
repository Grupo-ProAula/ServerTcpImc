package servertcpimc.server;

import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import servertcpimc.views.MainView;

public class ServerTCP extends Thread {
    private Boolean state;
    public static Map<String, ThreadClient> clientList;
    private Integer port = 9007;
    private ServerSocket service;
    private MainView view;

    public ServerTCP(Integer port, MainView view) {
        if(port != null && port != 0){
            this.port = port;
        }
        this.view = view;
        clientList = new HashMap<>();
    }
    
    @Override
    public void run() {
        super.run();
        startService();
    }
    
    public void startService() {
        try {
            service = new ServerSocket(port);
            state = true;
            view.getBtnStart().setText("DETENER");
            view.getBtnStart().setForeground(Color.RED);
            view.getLblState().setText("ONLINE");
            view.getLblState().setForeground(Color.GREEN);
            
            String msg = log() + "Servidor disponible en el Puerto " + port;
            System.out.println(msg);
            view.getTxtLog().append(msg + "\n");
            while (state) {
                Socket client = service.accept();
                String ip = client.getInetAddress().getHostAddress();
                msg = log() + "Cliente " + ip + " conectado";
                System.out.println(msg + "\n");
                view.getTxtLog().append(msg + "\n");
                ThreadClient atention = new ThreadClient(client, view);
                ServerTCP.clientList.put(ip, atention);
                atention.start();
            }
        } catch (IOException ex) {
            String msg = log() + "ERROR al abrir el puerto " + port;
            System.out.println(msg);
            view.getTxtLog().append(msg + "\n");
            view.getBtnStart().setText("INICIAR");
            view.getBtnStart().setForeground(Color.GREEN);
            view.getLblState().setText("OFFLINE");
            view.getLblState().setForeground(Color.RED);
        }
    }
    
    public void stopService() { 
        if (state) {
            state = false; 
            view.getBtnStart().setText("INICIAR"); 
            view.getBtnStart().setForeground(Color.GREEN);
            view.getLblState().setText("OFFLINE");
            view.getLblState().setForeground(Color.RED);
            ServerTCP.clientList.entrySet().stream().map(new Function<Map.Entry<String, ThreadClient>, String>() { 
                @Override 
                public String apply(Map.Entry<String, ThreadClient> element) {
                    String ip = element.getKey();
                    ThreadClient client = element.getValue();
                    String msg = log() + "Desconectando cliente " + ip;
                    System.out.println(msg);
                    view.getTxtLog().append(msg + "\n");
                    try {
                        client.getClient().close();
                        client = null;
                        ServerTCP.clientList.remove(element);
                        msg = log() + "Cliente desconectado" + ip;
                        System.out.println(msg);
                        view.getTxtLog().append(msg + "\n");
                    } catch (IOException ex) {
                        client = null;
                        ServerTCP.clientList.remove(element);
                        msg = log() + "Cliente desconectado" + ip;
                        System.out.println(msg);
                        view.getTxtLog().append(msg + "\n");
                    }
                    return ip;
                }
            }).forEachOrdered(ip -> {
                System.out.println("cliente " + ip + " Desconectado");
            });
            try {
                service.close();
            } catch (IOException ex) {
                System.out.println("ERROR: no se puede cerrar el Puerto " + port);
                String msg = log()+"ERROR: no se puede cerrar el Puerto " + port;
                System.out.println(msg);
                view.getTxtLog().append(msg + "\n");
            }
        }
    }    
    
    public String log() {
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        return f.format(new Date()) + " - ";
    }
}
