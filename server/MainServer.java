/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 *
 * @author tmdr
 */
public class MainServer {

    /**
     * @param args the command line arguments
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException, RemoteException, MalformedURLException {

            // TODO code application logic here
            IServer server=new ServerImp();
                Naming.rebind("rmi://0.0.0.0:2000/serv", server);

    }
    
}
