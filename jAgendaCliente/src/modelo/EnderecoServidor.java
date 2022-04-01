/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

/**
 *
 * @author Giovani
 */
public class EnderecoServidor {
    private final String IP, nomeServidor;
    private final int PORTA;
    
    public EnderecoServidor(String IP, int porta, String nomeServidor) {
        this.IP = IP;
        PORTA = porta;
        this.nomeServidor = nomeServidor;
    }
    
    public String getIP() {
        return IP;
    }

    public int getPORTA() {
        return PORTA;
    }
    
    public String getNomeServidor() {
        return nomeServidor;
    }
}
