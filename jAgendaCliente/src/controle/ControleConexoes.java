/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import static controle.Controle.getControle;
import exceptions.ConexaoEncerrada;
import exceptions.RespostaTimeoutException;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import javax.swing.JOptionPane;
import modelo.ArquivoExtendido;
import modelo.EnderecoServidor;
import modelo.IDsRede;
import modelo.TimerSimples;
import modelo.cliente.Mensagem;
import modelo.cliente.Usuario;
import modelo.exceptions.OfflineException;
import modelo.exceptions.ArquivoInexistenteException;
import modelo.exceptions.LoginException;
import modelo.exceptions.NovaContaException;
import modelo.exceptions.SemUsuarioException;
import modelo.exceptions.VersaoDiferenteException;
import rede.ControladorSocket;
import rede.PacoteRede;
import rede.SocketHook;

/**
 *
 * @author Giovani
 */
public class ControleConexoes {
    //Rede
    private volatile boolean ONLINE = false;

    //Controladoras de conexões
    private ControladorSocket conexaoControladora = null;
    private ControladorSocket conexaoDownloads = null;
    private ControladorSocket conexaoUploads = null;

    //Filas de dados thread-safe
    private volatile ArrayBlockingQueue<ItemFilaDownload> itensParaBaixar = new ArrayBlockingQueue<ItemFilaDownload>(1000);
    private volatile ArrayBlockingQueue<ItemFilaUpload> itensParaEnviar = new ArrayBlockingQueue<ItemFilaUpload>(1000);

    //Flags de gerenciamento de threads
    private volatile boolean encerrandoConexao = false;
    private volatile boolean threadUploadOn = false;
    private volatile boolean threadDownloadOn = false;
    private volatile boolean finalizarThreads = false;
    private volatile boolean iniciado = false;
    
    //Verifica a conexão de 1 em 1 segundo
    private final TimerSimples timerKeepAlive = new TimerSimples(() -> {
        testarKeepAlive();
    }, 1000, true);
    
    //Limpa pacotes antigos não obtidos pelo programa das controladoras de conexão
    private final TimerSimples timerGC = new TimerSimples(() -> {
        limparRespostas();
    }, 30000, true);
    
    /**
     * Tenta fechar uma conexão com o servidor.
     * @param endServidor
     * @throws ConexaoEncerrada Caso a conexão tenha sido encerrada durante a inicialização.
     * @throws RespostaTimeoutException Caso o tempo de transmissão de dados máximo seja alcançado.
     * @throws IOException Caso acontece um erro de transmissão de dados.
     */
    public synchronized void iniciarConexao(EnderecoServidor endServidor) throws ConexaoEncerrada, RespostaTimeoutException, IOException {
        if (!iniciado) {
            iniciado = true;
            getControle().getControleLogger().logarEvento("Estabelecendo conexões sockets com o IP " + endServidor.getIP() + ":" + endServidor.getPORTA(), true);

            try {
                conexaoControladora = new ControladorSocket(new Socket(endServidor.getIP(), endServidor.getPORTA()), new SocketHook() {
                    @Override
                    public void pedidoRecebido(PacoteRede pr) {
                        ControleConexoes.this.pedidoRecebido(pr);
                    }

                    @Override
                    public void erroFatal(Exception excptn) {
                        encerrarConexao(true, null);
                    }

                    @Override
                    public void conexaoTerminada(String msg) {
                        encerrarConexao(true, msg);
                        ControleConexoes.this.conexaoTerminada(msg);
                    }
                }, false, true);

                getControle().getControleLogger().logarEvento("Conexão controle estabelecida...", true);

                conexaoDownloads = new ControladorSocket(new Socket(endServidor.getIP(), endServidor.getPORTA()), new SocketHook() {
                    @Override
                    public void pedidoRecebido(PacoteRede pedido) {
                        //Impossível receber um pedido numa conexão de download
                    }

                    @Override
                    public void erroFatal(Exception ex) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void conexaoTerminada(String msg) {
                        encerrarConexao(true, null);
                        ControleConexoes.this.conexaoTerminada(msg);
                    }
                }, false, true);

                getControle().getControleLogger().logarEvento("Conexão de downloads estabelecida...", true);

                conexaoUploads = new ControladorSocket(new Socket(endServidor.getIP(), endServidor.getPORTA()), new SocketHook() {
                    @Override
                    public void pedidoRecebido(PacoteRede pedido) {
                        //Impossível receber um pedido numa conexão de upload
                    }

                    @Override
                    public void erroFatal(Exception ex) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void conexaoTerminada(String msg) {
                        encerrarConexao(true, null);
                        ControleConexoes.this.conexaoTerminada(msg);
                    }
                }, false, true);

                getControle().getControleLogger().logarEvento("Conexão de uploads estabelecida...", true);
                getControle().getControleLogger().logarEvento("Todas as conexões foram estabelecidas!", true);
            } catch (IOException | ConexaoEncerrada ex) {
                encerrarConexao(true, null);

                throw ex;
            }

            try {    
                Object obj1 = conexaoControladora.aguardarResposta(conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_VERIFICAR_VERSAO, Controle.VER_COMP)));
                conexaoDownloads.enviarPedido(new PacoteRede(IDsRede.ID_VERIFICAR_VERSAO, Controle.VER_COMP));
                conexaoUploads.enviarPedido(new PacoteRede(IDsRede.ID_VERIFICAR_VERSAO, Controle.VER_COMP));

                if (obj1 instanceof VersaoDiferenteException) {
                    //Atualizar cliente
                }

                conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_TIPOCONEXAO_CONTROLE));
                conexaoDownloads.enviarPedido(new PacoteRede(IDsRede.ID_TIPOCONEXAO_DOWNLOAD));
                conexaoUploads.enviarPedido(new PacoteRede(IDsRede.ID_TIPOCONEXAO_UPLOAD));

                new Thread("jAgenda - Download Thread") {
                    @Override
                    public void run() {
                        threadDownloadOn = true;

                        ItemFilaDownload itemFila;

                        while (!finalizarThreads) {
                            if (!itensParaBaixar.isEmpty()) {
                                itemFila = itensParaBaixar.peek();

                                try {
                                    //Verifica se o arquivo já existe baixado
                                    ArquivoExtendido ext = getControle().getControleArquivos().carregarArquivo(itemFila.getSHA256Arquivo());
                                    itemFila.getHookPosDoHook().run(ext);
                                } 

                                catch (ArquivoInexistenteException ex) {
                                    try {
                                        //Caso não exista, baixa-o
                                        Object obj = conexaoDownloads.aguardarResposta(conexaoDownloads.enviarPedido(new PacoteRede(IDsRede.ID_BAIXAR_ARQUIVO, itemFila.getSHA256Arquivo())), 6000000).getDado();

                                        if (obj instanceof ArquivoExtendido) {
                                            //Grava-o em disco caso seja encontrado no servidor e baixado com sucesso
                                            getControle().getControleArquivos().guardarArquivo((ArquivoExtendido) obj);

                                            itemFila.getHookPosDoHook().run((ArquivoExtendido) obj);
                                        } 

                                        else {
                                            itemFila.getHookPosDoHook().run(null);
                                        }
                                    } catch (ConexaoEncerrada ex1) {
                                        encerrarConexao(true, "Conexão perdida com o servidor.");
                                    } catch (RespostaTimeoutException ex1) {
                                    }
                                }

                                itensParaBaixar.poll();
                            } 

                            else {
                                try {
                                    sleep(5);
                                } 

                                catch (InterruptedException ex) {
                                }
                            }
                        }

                        threadDownloadOn = false;
                    }
                }.start();

                new Thread("jAgenda - Upload Thread") {
                    @Override
                    public void run() {
                        threadUploadOn = true;

                        ItemFilaUpload itemFila;

                        while (!finalizarThreads) {
                            if (!itensParaEnviar.isEmpty()) {
                                itemFila = itensParaEnviar.peek();

                                try {
                                    //Verifica se o arquivo já existe no servidor
                                    Object obj = conexaoUploads.aguardarResposta(conexaoUploads.enviarPedido(new PacoteRede(IDsRede.ID_VERIFICAR_EXISTENCIA_ARQUIVO, itemFila.getArquivoAEnviar().SHA256Arquivo()))).getDado();

                                    if (obj instanceof Boolean) {
                                        //Caso já exista, simplesmente ignora o envio, usando o arquivo já enviado.
                                        if ((boolean) obj) {
                                            itemFila.getHookPosUpload().run(itemFila.getArquivoAEnviar().SHA256Arquivo());
                                        } 

                                        else {
                                            //Caso não exista, envia o arquivo
                                            obj = conexaoUploads.aguardarResposta(conexaoUploads.enviarPedido(new PacoteRede(IDsRede.ID_ENVIAR_ARQUIVO, itemFila.getArquivoAEnviar())), 6000000).getDado();

                                            if (obj instanceof String) {
                                                itemFila.getHookPosUpload().run((String) obj);
                                            } 

                                            else {
                                                itemFila.getHookPosUpload().run(null);
                                            }
                                        }
                                    }

                                } catch (ConexaoEncerrada ex) {
                                    encerrarConexao(true, "Conexão perdida com o servidor.");
                                } catch (RespostaTimeoutException ex) {

                                }

                                itensParaEnviar.poll();
                            } else {
                                try {
                                    sleep(5);
                                } catch (InterruptedException ex) {}
                            }
                        }

                        threadUploadOn = false;
                    }
                }.start();

                timerKeepAlive.iniciar();
                timerGC.iniciar();

                ONLINE = true;

            } catch (ConexaoEncerrada | RespostaTimeoutException ex) {
                encerrarConexao(true, null);

                throw ex;
            }
        }
    }

    public synchronized void encerrarConexao(boolean mostrarMensagem, String msg) {
        if (!encerrandoConexao) {
            encerrandoConexao = true;
            finalizarThreads = true;
            
            timerKeepAlive.parar();
            timerGC.parar();

            if (conexaoControladora != null && !conexaoControladora.conexaoEstaEncerrada()) {
                conexaoControladora.terminarConexao("Conexão encerrada pelo cliente.", false);
                conexaoControladora = null;
            }

            if (conexaoDownloads != null && !conexaoDownloads.conexaoEstaEncerrada()) {
                conexaoDownloads.terminarConexao("Conexão encerrada pelo cliente.", false);
                conexaoDownloads = null;
            }

            if (conexaoUploads != null && !conexaoUploads.conexaoEstaEncerrada()) {
                conexaoUploads.terminarConexao("Conexão encerrada pelo cliente.", false);
                conexaoUploads = null;
            }

            while (threadDownloadOn || threadUploadOn) {
                try {
                    sleep(5);
                } 
                
                catch (InterruptedException ex) {
                    
                }
            }

            boolean ultimoStatus = ONLINE;

            ONLINE = false;

            finalizarThreads = false;
            encerrandoConexao = false;
            iniciado = false;

            if (ultimoStatus && mostrarMensagem) {
                JOptionPane.showMessageDialog(null, (msg != null ? msg : "Conexão perdida com o servidor."));
                getControle().encerrarPrograma();
            }
        }
    }

    public void novoDownload(ItemFilaDownload item) {
        itensParaBaixar.add(item);
    }

    public ArrayList<ItemFilaDownload> getDownloadsPendentes() {
        ArrayList<ItemFilaDownload> ls = new ArrayList<>();

        itensParaBaixar.forEach((item) -> {
            ls.add(item);
        });

        return ls;
    }

    public void novoUpload(ItemFilaUpload item) {
        itensParaEnviar.add(item);
    }

    public ArrayList<ItemFilaUpload> getUploadsownloadsPendentes() {
        ArrayList<ItemFilaUpload> ls = new ArrayList<>();

        itensParaEnviar.forEach((item) -> {
            ls.add(item);
        });

        return ls;
    }

    private void conexaoTerminada(String msg) {
        getControle().setOffline();
    }

    public void pedidoRecebido(PacoteRede recebido) {
        switch (recebido.getID()) {
            case IDsRede.ID_NOVA_MENSAGEM: {
                if (recebido.getDado() instanceof Mensagem) {
                    getControle().getControleJanelas().mensagemRecebida((Mensagem) recebido.getDado());
                }

                break;
            }
            
            case IDsRede.ID_FAZER_LOGOFF: {
                if (recebido.getDado() instanceof String) {
                    getControle().logout((String) recebido.getDado());
                }
                
                else {
                    getControle().logout("Você foi forçado à fazer logoff.");
                }
                
                break;
            }
            
            case IDsRede.ID_NOTIFICACAO_LOGIN: {
                getControle().getControleJanelas().atualizarUsuariosOnlineCompleto(); //Alto consumo de dados
                
                break;
            }
            
            case IDsRede.ID_NOTIFICACAO_LOGOUT: {
                getControle().getControleJanelas().atualizarUsuariosOnlineCompleto(); //Alto consumo de dados
                
                break;
            }
        }
    }

    public boolean estaOnline() {
        return ONLINE;
    }

    public void enviarMensagem(Mensagem m) throws OfflineException, ConexaoEncerrada, RespostaTimeoutException, LoginException, SemUsuarioException {
        if (!ONLINE) {
            throw new OfflineException("Sem conexão.");
        }
        
        int id = conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_NOVA_MENSAGEM, m));

        Object obj = conexaoControladora.aguardarResposta(id).getDado();

        if (obj instanceof LoginException) {
            throw (LoginException) obj;
        }

        if (obj instanceof SemUsuarioException) {
            throw (SemUsuarioException) obj;
        }
    }
    
    public void testarKeepAlive() {
        if (!encerrandoConexao || ONLINE) {
            if (conexaoControladora != null) {
                conexaoControladora.renovarPing();
            }
            
            if (conexaoDownloads != null) {
                conexaoDownloads.renovarPing();
            }
            
            if (conexaoUploads != null) {
                conexaoUploads.renovarPing();
            }
        }
    }

    public ArrayList<Usuario> obterUsuariosOnline() throws OfflineException, ConexaoEncerrada, RespostaTimeoutException {
        if (!ONLINE) {
            throw new OfflineException(("Sem conexão."));
        }
        
        Object obj = conexaoControladora.aguardarResposta(conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_OBTER_USUARIOS))).getDado();

        ArrayList<Usuario> aLista = new ArrayList<>();

        if (obj instanceof ArrayList) {
            aLista = (ArrayList<Usuario>) obj;

            if (getControle().usuarioEstaLogado()) {
                for (int x = 0; x < aLista.size(); x++) {
                    try {
                        if (getControle().usuarioEstaLogado() && aLista.get(x).getUsuario().equals(getControle().obterUsuario().getUsuario())) {
                            aLista.remove(x);
                        }
                    } catch (LoginException ex) {
                        //Código sem alcance
                    }
                }
            }
        }

        return aLista;
    }

    public Usuario obterUsuario(String usuario) throws RespostaTimeoutException, OfflineException, ConexaoEncerrada, SemUsuarioException {
        if (!ONLINE) {
            throw new OfflineException("Sem conexão.");
        }

        Object obj = conexaoControladora.aguardarResposta(conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_OBTER_USUARIO, usuario))).getDado();

        if (obj instanceof Usuario) {
            return (Usuario) obj;
        }

        throw (SemUsuarioException) obj;
    }

    public boolean criarConta(String usuario, String senha, String apelido, String email, String idImagem) throws OfflineException, RespostaTimeoutException, NovaContaException, IOException, ConexaoEncerrada {
        if (!ONLINE) {
            throw new OfflineException("Não há conexão com o servidor.");
        }

        String[] str = {usuario, senha, apelido, email, idImagem};

        Object obj = conexaoControladora.aguardarResposta(conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_NOVA_CONTA, str))).getDado();

        if (obj != null) {
            if (obj instanceof NovaContaException) {
                throw (NovaContaException) obj;
            } 
            
            else {
                return true;
            }
        }

        return false;
    }

    public synchronized Usuario realizarLogin(String usuario, String senha) throws OfflineException, RespostaTimeoutException, LoginException, ConexaoEncerrada {
        if (!ONLINE) {
            throw new OfflineException("Não há conexão com o servidor.");
        }

        Object obj = ((PacoteRede) conexaoControladora.aguardarResposta(conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_LOGIN, new String[]{usuario, senha})))).getDado();

        if (obj != null) {
            if (obj instanceof LoginException) {
                throw (LoginException) obj;
            } 
            
            else {
                return (Usuario) obj;
            }
        } 
        
        else {
            throw new LoginException("Dados de login inválidos.");
        }
    }

    private void limparRespostas() {
        if (ONLINE) {
            if (conexaoControladora != null) {
                conexaoControladora.limparRespostasAntigas(30000);
            }
            
            if (conexaoDownloads != null) {
                conexaoDownloads.limparRespostasAntigas(30000);
            }
            
            if (conexaoUploads != null) {
                conexaoUploads.limparRespostasAntigas(30000);
            }
            
            if (conexaoControladora != null) {
                conexaoControladora.limparRespostasAntigas(30000);
            }
        }
    }
    
    public synchronized void deslogar() throws OfflineException, ConexaoEncerrada, RespostaTimeoutException {
        if (!ONLINE) {
            throw new OfflineException("Não há conexão com o servidor.");
        }
        
        Object obj = conexaoControladora.aguardarResposta(conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_FAZER_LOGOFF))).getDado();
        
        if (obj instanceof Boolean) {
            getControle().logout("Você efetuou logout.");
        }
    }
    
    public synchronized boolean alterarImagemPerfil(Usuario usr, String sha256) throws ConexaoEncerrada, RespostaTimeoutException, SemUsuarioException, ArquivoInexistenteException, OfflineException {
        if (!ONLINE) {
            throw new OfflineException("Não há conexão com o servidor.");
        }
        
        String[] str = new String[2];
        
        str[0] = usr.getUsuario();
        str[1] = sha256;
        
        Object dado = conexaoControladora.aguardarResposta(conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_ALTERAR_IMAGEM_PERFIL, str))).getDado();
        
        if (dado instanceof SemUsuarioException) {
            throw (SemUsuarioException) dado;
        }
        
        if (dado instanceof ArquivoInexistenteException) {
            throw (ArquivoInexistenteException) dado;
        }
        
        if (dado instanceof Boolean) {
            return (boolean) dado;
        }
        
        return false;
    }
    
    public synchronized boolean alterarApelido(Usuario usr, String apelido) throws ConexaoEncerrada, SemUsuarioException, OfflineException, RespostaTimeoutException {
        if (!ONLINE) {
            throw new OfflineException("Não há conexão com o servidor.");
        }
        
        String[] str = new String[2];
        
        str[0] = usr.getUsuario();
        str[1] = apelido;
        
        Object dado = conexaoControladora.aguardarResposta(conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_ALTERAR_APELIDO, str))).getDado();
        
        if (dado instanceof SemUsuarioException) {
            throw (SemUsuarioException) dado;
        }
        
        if (dado instanceof Boolean) {
            return (boolean) dado;
        }
        
        return false;
    }
    
    public synchronized boolean alterarSenha(Usuario usr, String senha) throws ConexaoEncerrada, SemUsuarioException, OfflineException, RespostaTimeoutException {
        if (!ONLINE) {
            throw new OfflineException("Não há conexão com o servidor.");
        }
        
        String[] str = new String[2];
        
        str[0] = usr.getUsuario();
        str[1] = senha;
        
        Object dado = conexaoControladora.aguardarResposta(conexaoControladora.enviarPedido(new PacoteRede(IDsRede.ID_ALTERAR_SENHA, str))).getDado();
        
        if (dado instanceof SemUsuarioException) {
            throw (SemUsuarioException) dado;
        }
        
        if (dado instanceof Boolean) {
            return (boolean) dado;
        }
        
        return false;
    }
}
