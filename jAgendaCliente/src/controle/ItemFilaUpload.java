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
public class ItemFilaUpload {
    private final InterfaceHook<String> hookPosUpload;
    private final ArquivoExtendido arquivoUpload;
    
    public ItemFilaUpload(InterfaceHook<String> hookPosUpload, ArquivoExtendido arqUpload) {
        this.hookPosUpload = hookPosUpload;
        this.arquivoUpload = arqUpload;
    }
    
    public InterfaceHook<String> getHookPosUpload() {
        return hookPosUpload;
    }
    
    public ArquivoExtendido getArquivoAEnviar() {
        return arquivoUpload;
    }
}
