/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import modelo.ArquivoExtendido;
import modelo.InterfaceHook;

/**
 *
 * @author giovani
 */
public class ItemFilaDownload {
    private final InterfaceHook<ArquivoExtendido> hookPosDownload;
    private final String SHA256Arquivo;
    
    public ItemFilaDownload(String SHA256Arquivo, InterfaceHook<ArquivoExtendido> hookPosDownload) {
        this.SHA256Arquivo = SHA256Arquivo;
        this.hookPosDownload = hookPosDownload;
    }
    
    public InterfaceHook<ArquivoExtendido> getHookPosDoHook() {
        return hookPosDownload;
    }
    
    public String getSHA256Arquivo() {
        return SHA256Arquivo;
    }
}
