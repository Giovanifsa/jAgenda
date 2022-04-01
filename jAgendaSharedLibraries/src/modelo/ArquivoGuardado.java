/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.io.Serializable;

/**
 *
 * @author Giovani
 */
public class ArquivoGuardado implements Serializable {
    private final String SHA256, nomeDoArquivo, extensaoDoArquivo, arquivoSalvo;
    
    public ArquivoGuardado(String sha256, String arquivoS, String nomeArquivo, String extensaoArquivo) {
        SHA256 = sha256;
        arquivoSalvo = arquivoS;
        nomeDoArquivo = nomeArquivo;
        extensaoDoArquivo = extensaoArquivo;
    }

    public String getSHA256() {
        return SHA256;
    }

    public String getNomeDoArquivo() {
        return nomeDoArquivo;
    }

    public String getExtensaoDoArquivo() {
        return extensaoDoArquivo;
    }
    
    public String getArquivoSalvo() {
        return arquivoSalvo;
    }
}
