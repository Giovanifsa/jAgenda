/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import static controle.Controle.getControle;
import exceptions.ConexaoEncerrada;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Helpers;
import modelo.TimerSimples;
import modelo.cliente.Mensagem;
import modelo.cliente.Usuario;
import modelo.exceptions.LoginException;
import modelo.exceptions.SemUsuarioException;
import modelo.rede.Conexao;

/**
 *
 * @author Giovani
 */
public class ControleConexoes {
    //Rede
    private ServerSocket servidor = null;
    public final static int PORTA_PADRAO = 8484;
    private volatile boolean flag_interromper = false;
    private volatile boolean flag_terminar = false;
    
    //Verifica se a conexão ainda está ativa, caso não esteja, o envio do pedido de ping resultará em um erro, e chamará o terminarConexao internamente.
    private final TimerSimples timerKeepAlive = new TimerSimples(() -> {
        testeKeepAlive();
    }, 1000, true);
    
    //Limpa as conexões para diminuir o uso de memória.
    private final TimerSimples timerGC = new TimerSimples(() -> {
        limparRespostasAntigas();
    }, 30000, true);
    
    //Dados
    private volatile ArrayBlockingQueue<Conexao> listaConexoes = new ArrayBlockingQueue<Conexao>(Integer.SIZE);
    private volatile ArrayBlockingQueue<Conexao> listaConexoesLogadas = new ArrayBlockingQueue<Conexao>(Integer.SIZE);
    
    public void iniciarServico(int porta) throws IOException {
        if (servidor != null) {
            return;
        }
        
        servidor = new ServerSocket(porta);
        
        new Thread() {
            @Override
            public void run() {
                while (!flag_terminar) {
                    try {
                        sleep(5);
                    } catch (InterruptedException ex) {}
                    
                    Conexao clienteConectando;
                    
                    while (!flag_interromper) {
                        try {
                            //Aceita a conexão e já instancia o gerenciador de conexões
                            clienteConectando = new Conexao(servidor.accept(), (Exception obj) -> {
                                getControle().erroFatal(obj);
                            });
                            
                            listaConexoes.add(clienteConectando);
                            
                            getControle().getControleJanelas().escreverLinhaConsole("Um socket com IP \"" + clienteConectando.getControleConexao().obterIP() + "\" estabeleceu conexão.");
                            getControle().getControleJanelas().atualizarListaConexoes();
                        } catch (IOException ex) {
                            
                        } catch (ConexaoEncerrada ex) {
                            Logger.getLogger(Controle.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    flag_interromper = false;
                }
                
                flag_terminar = false;
                
            }
        }.start();
        
        //Inicia o keep alive e o limpador de memória
        timerKeepAlive.iniciar();
        timerGC.iniciar();
        
        getControle().getControleJanelas().escreverLinhaConsole("Serviço iniciado com sucesso na porta " + porta + ".");
        getControle().setOnline();
    }
    
    public void encerrarServico() {
        if (servicoEstaIniciado()) {
            flag_terminar = true;
            flag_interromper = true;

            //Envia uma mensagem para todos os clientes alertando que a conexão está sendo encerrada
            listaConexoes.forEach((cl) -> {
                cl.getControleConexao().terminarConexao("O serviço em nuvem está sendo desligado.", true);
            });
            
            try {
                servidor.close();
            } catch (IOException ex) {}

            servidor = null;

            getControle().getControleJanelas().escreverLinhaConsole("Serviço encerrado com sucesso.");
            getControle().setOffline();
        }
    }
    
    public void testeKeepAlive() {
        listaConexoes.forEach((con) -> {
            con.getControleConexao().renovarPing();
        });
    }
    
    public void limparRespostasAntigas() {
        listaConexoes.forEach((con) -> {
            con.getControleConexao().limparRespostasAntigas(60000);
        });
    }
    
    public boolean servicoEstaIniciado() {
        return (servidor != null);
    }
    
    public ArrayBlockingQueue<Conexao> obterConexoes() {
        return listaConexoes;
    }
    
    public ArrayBlockingQueue<Conexao> obterConexoesLogadas() {
        return listaConexoesLogadas;
    }
    
    public Usuario realizarLogin(String usuario, String senha) throws NoSuchAlgorithmException, LoginException {
        for (Usuario usr : getControle().getControlePersistencia().getBanco().getTodosUsuarios()) {
            if (usr.getUsuario().equals(usuario) && usr.getSenhaSHA256().equals(Helpers.SHA256(senha))) {
                for (Conexao cl : listaConexoes) {
                    try {
                        if (cl.usuarioEstaLogado() && cl.obterUsuario().getUsuario().equals(usuario)) {
                            cl.deslogarCliente("A sua conta foi acessada de outro lugar. Sua sessão neste dispositivo foi encerrada.");
                            break;
                        }
                    } catch (LoginException ex) {}
                }
                
                return usr;
            }
        }
        
        throw new LoginException("Conta não encontrada.");
    }
    
    public void enviarMensagem(Mensagem m) throws SemUsuarioException {
        if (!getControle().getControlePersistencia().getBanco().usuarioExiste(m.obterRemetente().getUsuario())) {
            throw new SemUsuarioException("O usuário \"" + m.obterRemetente() + "\" não existe.");
        }
        
        for (Conexao cl : obterConexoes()) {
            if (cl.usuarioEstaLogado()) {
                try {
                    if (cl.obterUsuario().getUsuario().equals(m.obterDestinatario().getUsuario())) {
                        cl.enviarMensagem(m);
                        
                        return;
                    }
                } catch (LoginException ex) {}
            }
        }
        
        //Se estiver offline, guardar mensagem para enviar
    }
    
    public synchronized void setClienteDesconectado(Conexao con) { 
        try {
            con.deslogarCliente("Você encerrou a conexão.");
        } catch (LoginException ex) {}
        
        listaConexoes.remove(con);
        
        getControle().getControleJanelas().atualizarListaConexoes();
    }
    
    public synchronized void setClienteLogado(Conexao ger, Usuario usr) {
        listaConexoesLogadas.add(ger);
        
        try {
            getControle().getControleJanelas().escreverLinhaConsole("[" + ger.getControleConexao().obterIP() + "] fez login como \"" + ger.obterUsuario().getUsuario() + "\".");
        } catch (LoginException ex) {}
        
        getControle().getControleJanelas().atualizarListaConexoes();
        
        for (Conexao con : obterConexoesLogadas()) {
            try {
                con.notificarLogin(usr.clonarSemDados());
            } catch (LoginException ex) {}
        }
    }
    
    public synchronized void setClienteDeslogado(Conexao ger, Usuario usr) {
        listaConexoesLogadas.remove(ger);
        
        getControle().getControleJanelas().escreverLinhaConsole("[" + ger.getControleConexao().obterIP() + "] fez logoff.");
        getControle().getControleJanelas().atualizarListaConexoes();
        
        for (Conexao con : obterConexoesLogadas()) {
            try {
                con.notificarLogout(usr.clonarSemDados());
            } catch (LoginException ex) {}
        }
    }
    
    public synchronized void matarConexao(Conexao con) {
        con.getControleConexao().terminarConexao("Sua conexão foi encerrada pelo servidor.", false);
        setClienteDesconectado(con);
    }
    
    public synchronized void deslogarConexao(Conexao con) {
        try {
            //Deslogar cliente
            con.deslogarCliente("Você foi forçado à deslogar.");
        } catch (LoginException ex) {}
    }
    
    public synchronized void encerrarLoginsUsuario(String usuario) {
        for (Conexao con : listaConexoesLogadas) {
            try {
                if (con.usuarioEstaLogado() && con.obterUsuario().getUsuario().equals(usuario)) {
                    deslogarConexao(con);
                }
            } catch (LoginException ex) {}
        }
    }
    
    public void notificarTodosLogout(Usuario usr) {
        
    }
    
    public void notificarTodosUsuarioEditado(Usuario usr) {
        
    }
}
