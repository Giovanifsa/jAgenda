/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import com.db4o.ext.DatabaseFileLockedException;
import exceptions.ConexaoEncerrada;
import exceptions.RespostaTimeoutException;
import java.io.IOException;
import javax.swing.JOptionPane;
import modelo.ControleArquivos;
import modelo.ControleLoggerEventos;
import modelo.EnderecoServidor;
import modelo.cliente.Usuario;
import modelo.exceptions.LoginException;
import modelo.exceptions.OfflineException;
import visao.janelas.JanelaServidores;

/**
 *
 * @author giovani
 */
public class Controle {
    //Main
    public static void main(String[] args) {
        new Controle();
    }
    
    //Habilita a seleção de servidores
    public final boolean SELECIONAR_SERVIDORES = true;
    
    //Dados de conexão e servidores;
    public static final EnderecoServidor SERVIDOR_PADRAO = new EnderecoServidor("nescauzitos.tplinkdns.com", 8484, "Nescau");
    
    public static final EnderecoServidor[] SERVIDORES_DISPONIVEIS = {
        new EnderecoServidor("127.0.0.1", 8484, "LOCALHOST"),
        SERVIDOR_PADRAO
    };
    
    //Versão de compilação para verificação de versão.
    public static final int VER_COMP = 2;
    public static final String VER_STRING = "jAgendaBeta_" + VER_COMP;
    
    //Singletone
    private static Controle controleIniciado = null;
    
    //Dados do cliente
    private volatile Usuario usuarioLogado = null;
    
    //Controladoras de informações
    private ControleLoggerEventos contLogger;
    private ControleConexoes contConexoes;
    private ControleJanelas contJanelas;
    private ControleArquivos contArquivos;
    
    //Fechamento do programa
    private Thread threadShutdown = new Thread() {
        @Override
        public void run() {
            encerrarPrograma();
        }
    };
    
    private volatile boolean encerrando = false;
    
    public Controle() {
        controleIniciado = this;
        
        contLogger = new ControleLoggerEventos();
        
        try {
            contArquivos = new ControleArquivos();
            
            contConexoes = new ControleConexoes();
        
            try {
                if (SELECIONAR_SERVIDORES) {
                    EnderecoServidor selecionado = new JanelaServidores().selecionarServidor(SERVIDORES_DISPONIVEIS);
                    
                    if (selecionado == null) {
                        encerrarPrograma();
                    }
                    
                    else {
                        contConexoes.iniciarConexao(selecionado);
                    }
                }
                
                else {
                    contConexoes.iniciarConexao(SERVIDOR_PADRAO);
                }
                
                contJanelas = new ControleJanelas();
                
                contJanelas.mostrarTelaLogin();
                
                Runtime.getRuntime().addShutdownHook(threadShutdown);
            } catch (IOException | ConexaoEncerrada | RespostaTimeoutException ex) {
                contLogger.logarErroFatal("Erro recebido durante a inicialização de conexão com o servidor: " + ex.toString(), true);
                
                JOptionPane.showMessageDialog(null, "Sem conexão com o servidor.");
                
                encerrarPrograma();
            }
            
        } catch (DatabaseFileLockedException ex) {
            contLogger.logarErroFatal("O banco de dados local não pôde ser aberto. Já estava aberto em outra instância?", true);
            
            JOptionPane.showMessageDialog(null, "Uma instância do jAgenda já está em execução.");
            
            encerrarPrograma();
        }
    }
    
    public void encerrarPrograma() {
        encerrarPrograma(null);
    }
    
    public void encerrarPrograma(String msg) {
        if (!encerrando) {
            encerrando = true;
            
            if (contConexoes != null) {
            contConexoes.encerrarConexao(false, msg);
            }

            if (contArquivos != null) {
                contArquivos.encerrarArquivos();
            }

            if (contLogger != null) {
                contLogger.salvarLog();
            }     

            System.exit(0);
        }
    }
    
    public static Controle getControle() {
        if (controleIniciado != null) {
            return controleIniciado;
        }
        
        controleIniciado = new Controle();
        return controleIniciado;
    }
    
    public ControleConexoes getControleConexoes() {
        return contConexoes;
    }
    
    public ControleJanelas getControleJanelas() {
        return contJanelas;
    }
    
    public ControleArquivos getControleArquivos() {
        return contArquivos;
    }
    
    public ControleLoggerEventos getControleLogger() {
        return contLogger;
    }
    
    public void setOnline() {
        
    }
    
    public void setOffline() {
        
    }

    /**
     * Obtém os dados do usuário
     * @return      Usuário caso esteja logado.
     * @throws LoginException 
     */
    public Usuario obterUsuario() throws LoginException {
        if (!usuarioEstaLogado()) {
            contLogger.logarErro("Tentativa de obtenção dos dados do usuário sem login. Alguma tela não foi fechada?", true);
            
            throw new LoginException("O usuário não está logado.");
        }
        
        return usuarioLogado;
    }
    
    /**
     * Verifica se o usuário está logado
     * @return      Verdadeiro caso esteja, falso caso contrário.
     */
    public boolean usuarioEstaLogado() {
        return (usuarioLogado != null);
    }
    
    public void logout(String msg) {
        if (usuarioEstaLogado()) {
            usuarioLogado = null;
            
            contJanelas.logoffExecutado(msg);
        }
    }
    
    public synchronized void realizarLogin(String usuario, String senha) throws OfflineException, RespostaTimeoutException, LoginException, ConexaoEncerrada {
        if (!contConexoes.estaOnline()) {
            throw new OfflineException("Não há conexão com o servidor.");
        }
        
        if (usuarioEstaLogado()) {
            contLogger.logarErro("Tentativa de login enquanto o usuário já estava logado. Existe alguma tela de login aberta?", true);
            
            throw new LoginException("Você já está logado!");
        }
        
        usuarioLogado = contConexoes.realizarLogin(usuario, senha);
        
        contLogger.logarAviso("Login realizado com sucesso: " + usuarioLogado.getUsuario(), true);
        
        contJanelas.loginRealizado(usuarioLogado);
    }
}
