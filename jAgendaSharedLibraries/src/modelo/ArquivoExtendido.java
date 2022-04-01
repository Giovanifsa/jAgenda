/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.naming.SizeLimitExceededException;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Giovani
 */
public class ArquivoExtendido implements Serializable {
    public static final int TAMANHO_LIMITE_ARQUIVOS = 1048576 * 10; //10 MB
    public static final SizeLimitExceededException EXCEPTION_TAMANHO_EXCEDIDO = new SizeLimitExceededException("Tamanho limite excedido (10 MB).");
    
    public static FileNameExtensionFilter EXTENSOES_IMAGENS = new FileNameExtensionFilter("Arquivos de imagem", "png", "jpg", "bmp", "tiff", "jpeg");
    public static final String DIR_PADRAO_DOWNLOAD = obterDiretorioAtual() + "/arquivos_salvos/";
    
    private byte[] arquivo;
    private String nomeOriginal;
    private String extensao = "";
    
    public ArquivoExtendido(File f) throws IOException, SizeLimitExceededException {
        if (f.exists()) {
            if (Files.size(f.toPath()) > TAMANHO_LIMITE_ARQUIVOS) {
                throw EXCEPTION_TAMANHO_EXCEDIDO;
            }
            
            arquivo = Files.readAllBytes(f.toPath());
            
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                nomeOriginal = s.substring(0, i - 1);
                extensao = s.substring(i + 1);
            }
            
            else {
                nomeOriginal = s;
            }
        }
    }
    
    public ArquivoExtendido(byte[] f, String nomePontoExtensao) throws SizeLimitExceededException {
        if (f.length > TAMANHO_LIMITE_ARQUIVOS - 1) {
            throw EXCEPTION_TAMANHO_EXCEDIDO;
        }
        
        arquivo = f;
        
        int i = nomePontoExtensao.lastIndexOf('.');

        if (i > 0 &&  i < nomePontoExtensao.length() - 1) {
            extensao = nomePontoExtensao.substring(i + 1).toLowerCase();
            nomeOriginal = nomePontoExtensao.substring(0, i - 1).toLowerCase();
        }
        
        else {
            nomeOriginal = nomePontoExtensao;
        }
    }
    
    public ArquivoExtendido(byte[] f, String nome, String extensao) throws SizeLimitExceededException {
        this(f, (extensao != null ? nome + "." + extensao : nome));
    }
    
    public static String obterDiretorioAtual() {
        return Paths.get("").toAbsolutePath().toString();
    }
    
    public Path salvarArquivo(String dir, String nome, String extensao) throws IOException {
        File f = new File(dir + nome + (extensao != null ? "." + extensao : ""));
        
        f.createNewFile();
        
        try (FileOutputStream fout = new FileOutputStream(f)) {
            fout.write(arquivo);
        }
        
        return f.toPath();
    }
    
    public byte[] getArquivo() {
        return arquivo;
    }
    
    public String getNomeOriginal() {
        return nomeOriginal;
    }
    
    public String getExtensao() {
        return extensao;
    }
    
    public boolean isImagem() {
        if (extensao != null && (extensao.equals("bmp") || extensao.equals("jpg") || extensao.equals("jpeg") || extensao.equals("gif") || extensao.equals("tiff") || extensao.equals("tga") || extensao.equals("png"))) {
            return true;
        }
        
        return false;
    }
    
    public String SHA256Arquivo() {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            
            return new String(sha256.digest(arquivo));
        } catch (NoSuchAlgorithmException ex) {
            //Impossível existir este exception
        }
        
        return null;
    }
    
    public int getTamanhoArquivo() {
        return arquivo.length;
    }
}
