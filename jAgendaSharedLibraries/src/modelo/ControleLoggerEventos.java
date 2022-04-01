/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author giovani
 */
public class ControleLoggerEventos {
    public static final String LOGS_DIRETORIO_PADRAO = Helpers.obterDiretorioAtual() + "/logs/";
    
    private final String arquivoAtual;
    private final ArrayBlockingQueue<String> textoArquivo = new ArrayBlockingQueue<>(30000);
    
    public ControleLoggerEventos() {
        arquivoAtual = LOGS_DIRETORIO_PADRAO + "log_" + Helpers.obterDataHorario24H("dd_MM_yyyy_HH_mm_ss") + System.currentTimeMillis() + ".txt";
        
        File f = new File(LOGS_DIRETORIO_PADRAO);
        f.mkdirs();
        
        logarMensagem("Logging iniciado.", true);
    }
    
    public void logarErroFatal(String msg, boolean salvar) {
        textoArquivo.add("[ERRO FATAL - " + Helpers.obterDataHorario24H("HH:mm:ss") + "] " + msg);
        
        if (salvar) {
            salvarLog();
        }
    }
    
    public void logarErro(String msg, boolean salvar) {
        textoArquivo.add("[ERRO - " + Helpers.obterDataHorario24H("HH:mm:ss") + "] " + msg);
        
        if (salvar) {
            salvarLog();
        }
    }
    
    public void logarEvento(String msg, boolean salvar) {
        textoArquivo.add("[EVENTO - " + Helpers.obterDataHorario24H("HH:mm:ss") + "] " + msg);
        
        if (salvar) {
            salvarLog();
        }
    }
    
    public void logarAviso(String msg, boolean salvar) {
        textoArquivo.add("[AVISO - " + Helpers.obterDataHorario24H("HH:mm:ss") + "] " + msg);
        
        if (salvar) {
            salvarLog();
        }
    }
    
    public void logarMensagem(String msg, boolean salvar) {
        textoArquivo.add("[MENSAGEM - " + Helpers.obterDataHorario24H("HH:mm:ss") + "] " + msg);
        
        if (salvar) {
            salvarLog();
        }
    }
    
    public synchronized void salvarLog() {
        try (FileWriter fw = new FileWriter(arquivoAtual, true); 
            BufferedWriter bw = new BufferedWriter(fw); 
            PrintWriter out = new PrintWriter(bw)) {

            String str;

            while ((str = textoArquivo.poll()) != null) {
                out.println(str);
            }

        } catch (IOException ex) {
            logarErro("Não foi possível armazenar dados de log.", false);
        }
    }
}
