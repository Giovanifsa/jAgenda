/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.cliente;

import java.io.Serializable;

/**
 *
 * @author giovani
 */
public class Mensagem implements Serializable {
    private String mensagem, SHA256Arquivo = null;
    private Usuario remetente;
    private final Usuario destinatario;
    
    /**
     * Constrói um novo objeto Mensagem, que é utilizado para a troca de mensagens.
     * @param msg           Mensagem que será enviada;
     * @param destinatario  Índice de quem receberá a mensagem.
     * @param arquivo       Define se a mensagem identifica um arquivo.
     */
    public Mensagem(String msg, Usuario destinatario, boolean arquivo) {
        if (arquivo) {
            SHA256Arquivo = msg;
        }
        
        else {
            mensagem = msg;
        }
        
        this.destinatario = destinatario;
    }
    
    /**
     * Obtém a mensagem transmitida.
     * @return Mensagem transmitida.
     */
    public String obterMensagem() {
        return mensagem;
    }
    
    /**
     * Obtém o remetente da mensagem.
     * @return Índice do remetente da mensagem.
     */
    public Usuario obterRemetente() {
        return remetente;
    }
    
    public void setRemetente(Usuario rem) {
        remetente = rem;
    }
    
    /**
     * Obtém o destinatário da mensagem.
     * @return Índice do destinatário da mensagem.
     */
    public Usuario obterDestinatario() {
        return destinatario;
    }
    
    public boolean contemArquivo() {
        return (SHA256Arquivo != null);
    }
    
    public String getSHA256Arquivo() {
        return SHA256Arquivo;
    }
}
