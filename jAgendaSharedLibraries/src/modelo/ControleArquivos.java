/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.ext.DatabaseFileLockedException;
import com.db4o.ext.Db4oIOException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.naming.SizeLimitExceededException;
import modelo.exceptions.ArquivoInexistenteException;

/**
 *
 * @author Giovani
 */
public class ControleArquivos {
    public static final String BANCO_ARQUIVOS_DIRETORIO_PADRAO = Helpers.obterDiretorioAtual() + "/DB4O_Arquivos.bin";
    public static final String ARQUIVOS_DIRETORIO_PADRAO = Helpers.obterDiretorioAtual() + "/arquivos/";
    
    private final ObjectContainer bancoDB4O;
    private int contagem = 0;
    
    
    public ControleArquivos() throws DatabaseFileLockedException, Db4oIOException {
        bancoDB4O = Db4o.openFile(BANCO_ARQUIVOS_DIRETORIO_PADRAO);
        
        File f = new File(ARQUIVOS_DIRETORIO_PADRAO);
        f.mkdirs();
    }
    
    /**
     * Obtém uma lista de identificação de todos os arquivos guardados.
     * @return Lista de identificações de arquivos guardados.
     */
    public synchronized ArrayList<ArquivoGuardado> getTodosArquivos() {
        ObjectSet<ArquivoGuardado> lista = bancoDB4O.query(ArquivoGuardado.class);
        
        File f;
        
        ArrayList<ArquivoGuardado> listaArq = new ArrayList<>();
        
        for (ArquivoGuardado arquivo : lista) {
            f = new File(ARQUIVOS_DIRETORIO_PADRAO + arquivo.getArquivoSalvo());
            
            if (f.exists()) {
                listaArq.add(arquivo);
            }
            
            else {
                bancoDB4O.delete(arquivo);
            }
        }
        
        return listaArq;
    }
    
    /**
     * Guarda um arquivo no banco de arquivos. Caso já exista, ignora a gravação.
     * @param arq Arquivo à ser gravado.
     * @return Identificador do arquivo guardado;
     */
    public synchronized ArquivoGuardado guardarArquivo(ArquivoExtendido arq) {
        String sha256 = arq.SHA256Arquivo();
        ArquivoGuardado arquivo;
        
        try {
            arquivo = getArquivo(sha256);
            
            return arquivo;
        } catch (ArquivoInexistenteException ex) {
            String nomeArquivo = contagem + "-" + arq.getNomeOriginal() + System.currentTimeMillis();
            
            contagem++;
            
            File f = new File(ARQUIVOS_DIRETORIO_PADRAO + nomeArquivo + arq.getExtensao());
            
            if (f.exists()) {
                f.delete();
            }
            
            try {
                arq.salvarArquivo(ARQUIVOS_DIRETORIO_PADRAO, nomeArquivo, arq.getExtensao());
            } catch (IOException ex1) {
                //getControle().erroFatal(ex1);
            }
            
            arquivo = new ArquivoGuardado(sha256, nomeArquivo + "." + arq.getExtensao(), arq.getNomeOriginal(), arq.getExtensao());
            
            bancoDB4O.store(arquivo);
            bancoDB4O.commit();
            
            return arquivo;
        }
    }
    
    /**
     * Verifica se um arquivo existe à partir de seu SHA256.
     * @param sha256 SHA256 do arquivo à ser procurado.
     * @return Verdadeiro caso exista, falso caso contrário.
     */
    public synchronized boolean arquivoExiste(String sha256) {
        return getTodosArquivos().stream().anyMatch((arq) -> (arq.getSHA256().equals(sha256)));
    }
    
    public boolean arquivoExiste(ArquivoExtendido arq) {
        return arquivoExiste(arq.SHA256Arquivo());
    }
    
    /**
     * Obtém um identificador de arquivo do banco de dados.
     * @param sha256 SHA256 do identificador à ser procurado.
     * @return Identificação caso seja encontrado, null caso contrário.
     * @throws modelo.exceptions.ArquivoInexistenteException
     */
    public ArquivoGuardado getArquivo(String sha256) throws ArquivoInexistenteException {
        for (ArquivoGuardado arqS : getTodosArquivos()) {
            if (arqS.getSHA256().equals(sha256)) {
                return arqS;
            }
        }
        
        throw new ArquivoInexistenteException("Identificador de arquivo não encontrado.");
    }
    
    public void encerrarArquivos() {
        bancoDB4O.close();
    }
    
    @Override
    public void finalize() {
        bancoDB4O.close();
        
        try {
            super.finalize();
        } catch (Throwable ex) {}
    }
    
    /**
     * Carrega o arquivo na memória.
     * @param sha256 SHA256 do arquivo à ser carregado.
     * @return O arquivo caso seja encontrado, ou null caso não seja.
     * @throws modelo.exceptions.ArquivoInexistenteException
     */
    public ArquivoExtendido carregarArquivo(String sha256) throws ArquivoInexistenteException {
        ArquivoGuardado arquivo = getArquivo(sha256);
        
        try {
            ArquivoExtendido load = new ArquivoExtendido(new File(ARQUIVOS_DIRETORIO_PADRAO + arquivo.getArquivoSalvo()));
            ArquivoExtendido arq = new ArquivoExtendido(load.getArquivo(), arquivo.getNomeDoArquivo(), arquivo.getExtensaoDoArquivo());

            return arq;
        } catch (IOException | SizeLimitExceededException ex) {
            throw new InternalError("Um erro interno ocorreu, estamos analisando o problema.");
        }
    }
}
