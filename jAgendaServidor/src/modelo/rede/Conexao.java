/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.rede;

import controle.Controle;
import static controle.Controle.getControle;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import exceptions.ConexaoEncerrada;
import java.util.ArrayList;
import modelo.ArquivoExtendido;
import modelo.Helpers;
import modelo.IDsRede;
import modelo.InterfaceHook;
import modelo.cliente.Mensagem;
import modelo.cliente.Usuario;
import modelo.exceptions.AlteracaoException;
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
 * @author giovani
 */
public class Conexao {
    //Dados do cliente obtidos após ele realizar o login.
    private volatile Usuario usuario = null;
    
    //Controle de rede
    private volatile ControladorSocket controleConexao;
    
    private volatile int tipoConexao = -1;
    private volatile boolean versaoValida = false;
    
    public Conexao(Socket conexaoRecebida, InterfaceHook<Exception> erroFatal) throws ConexaoEncerrada {
        controleConexao = new ControladorSocket(conexaoRecebida, new SocketHook() {
            @Override
            public void pedidoRecebido(PacoteRede pr) {
                Conexao.this.processarPedido(pr);
            }

            @Override
            public void erroFatal(Exception excptn) {
                erroFatal.run(excptn);
            }

            @Override
            public void conexaoTerminada(String string) {
                getControle().getControleConexoes().setClienteDesconectado(Conexao.this);
            }
        }, true, true);
    }
    
    //Processará os pacotes de rede que são pedidos.
    private void processarPedido(PacoteRede pedido) {
        if (versaoValida) {
            if (tipoConexao == -1) {
                switch (pedido.getID()) {
                    case IDsRede.ID_TIPOCONEXAO_CONTROLE: {
                        tipoConexao = IDsRede.ID_TIPOCONEXAO_CONTROLE;
                        break;
                    }

                    case IDsRede.ID_TIPOCONEXAO_DOWNLOAD: {
                        tipoConexao = IDsRede.ID_TIPOCONEXAO_DOWNLOAD;
                        break;
                    }

                    case IDsRede.ID_TIPOCONEXAO_UPLOAD: {
                        tipoConexao = IDsRede.ID_TIPOCONEXAO_UPLOAD;
                        break;
                    }
                }

                getControle().getControleJanelas().atualizarListaConexoes();
            }

            else if (tipoConexao == IDsRede.ID_TIPOCONEXAO_CONTROLE) {
                switch (pedido.getID()) {
                    case IDsRede.ID_LOGIN: {
                        if (pedido.getDado() != null && pedido.getDado() instanceof String[]) {
                            String[] dadosLogin = (String[]) pedido.getDado();

                            if (dadosLogin.length == 2) {
                                try {
                                    try {
                                        usuario = getControle().getControleConexoes().realizarLogin(dadosLogin[0], dadosLogin[1]);

                                        controleConexao.enviarResposta(new PacoteRede(usuario), pedido.getIndice());

                                        getControle().getControleConexoes().setClienteLogado(this, usuario);
                                    } catch (NoSuchAlgorithmException ex) {
                                        getControle().erroFatal(ex);
                                        break;
                                    } catch (LoginException ex) {
                                        controleConexao.enviarResposta(new PacoteRede(ex), pedido.getIndice());
                                        break;
                                    }
                                } catch (ConexaoEncerrada ex) {
                                    //O método de terminar conexão já foi chamado nesse ponto
                                }
                            }
                        }

                        break;
                    }

                    case IDsRede.ID_NOVA_CONTA: {
                        if (pedido.getDado() != null && pedido.getDado() instanceof String[]) {
                            String[] str = (String[]) pedido.getDado();

                            try {
                                if ((str.length == 4 || str.length == 5) && str[0] != null && str[1] != null && str[2] != null &&
                                        str[3] != null && !str[0].equals("") && !str[1].equals("") && !str[2].equals("") && 
                                        !str[3].equals("")) {

                                    if (!getControle().getControlePersistencia().getBanco().usuarioExiste(str[0])) {
                                        Usuario usr = new Usuario(str[0], str[2], Helpers.SHA256(str[1]), str[3], (str.length == 5 ? str[4] : null));

                                        getControle().getControlePersistencia().getBanco().salvarUsuario(usr);

                                        controleConexao.enviarResposta(new PacoteRede(true), pedido.getIndice());
                                    }

                                    else {
                                        controleConexao.enviarResposta(new PacoteRede(new NovaContaException("\"" + str[0] + "\" já está cadastrado.")), pedido.getIndice());
                                    }
                                }

                                else {
                                    controleConexao.enviarResposta(new PacoteRede(new NovaContaException("Dados não supridos!")), pedido.getIndice());
                                }
                            } catch (ConexaoEncerrada ex) {
                                //O método de terminar conexão já foi chamado nesse ponto
                            } catch (NoSuchAlgorithmException ex) {
                                try {
                                    controleConexao.enviarResposta(new PacoteRede(new NovaContaException("Falha ao criar a conta!")), pedido.getIndice());
                                } catch (ConexaoEncerrada ex1) {}
                            }
                        }

                        break;
                    }

                    case IDsRede.ID_OBTER_USUARIO: {
                        if (pedido.getDado() != null && pedido.getDado() instanceof String) {
                            try {
                                try {
                                    Usuario usr = getControle().getControlePersistencia().getUsuario((String) pedido.getDado());

                                    controleConexao.enviarResposta(new PacoteRede(usr.clonarSemDados()), pedido.getIndice());
                                } catch (SemUsuarioException ex) {
                                    controleConexao.enviarResposta(new PacoteRede(ex), pedido.getIndice());
                                }
                            } catch (ConexaoEncerrada ex) {
                                //O método de terminar conexão já foi chamado nesse ponto
                            }
                        }

                        break;
                    }

                    case IDsRede.ID_NOVA_MENSAGEM: {
                        if (pedido.getDado() != null && pedido.getDado() instanceof Mensagem) {
                            try {
                                if (!usuarioEstaLogado()) {
                                    controleConexao.enviarResposta(new PacoteRede(new LoginException("O login não foi efetuado.")), pedido.getIndice());
                                    break;
                                }

                                Mensagem m = (Mensagem) pedido.getDado();
                                m.setRemetente(usuario.clonarSemDados());

                                try {
                                    getControle().getControleConexoes().enviarMensagem(m);
                                } catch (SemUsuarioException ex) {
                                    controleConexao.enviarResposta(new PacoteRede(ex), pedido.getIndice());
                                    break;
                                }

                                controleConexao.enviarResposta(new PacoteRede(true), pedido.getIndice());
                            } catch (ConexaoEncerrada ex) {
                                //Este exception, neste caso, indica que a conexão está sendo encerrada,
                                //e está sendo tratado pelo controlador de socket.
                            }
                        }

                        break;
                    }

                    case IDsRede.ID_OBTER_USUARIOS: {
                        ArrayList<Usuario> lista = new ArrayList<>();

                        for (Conexao cl : getControle().getControleConexoes().obterConexoes()) {
                            if (cl.usuarioEstaLogado()) {
                                try {
                                    lista.add(cl.obterUsuario().clonarSemDados());
                                } catch (LoginException ex) {}
                            }
                        }

                        try {
                            controleConexao.enviarResposta(new PacoteRede(lista), pedido.getIndice());
                        } catch (ConexaoEncerrada ex) {}

                        break;
                    }
                    
                    case IDsRede.ID_FAZER_LOGOFF: {
                        try {
                            if (!usuarioEstaLogado()) {
                                controleConexao.enviarResposta(new PacoteRede(new LoginException("Você não está logado para efetuar logoff!")), pedido.getIndice());
                            }
                            
                            else {
                                Usuario usr = usuario;
                                usuario = null;
                                
                                getControle().getControleConexoes().setClienteDeslogado(this, usr);
                                
                                controleConexao.enviarResposta(new PacoteRede(true), pedido.getIndice());
                            }
                        } catch (ConexaoEncerrada ex) {}
                        
                        break;
                    }
                    
                    case IDsRede.ID_ALTERAR_IMAGEM_PERFIL: {
                        if (pedido.getDado() != null && pedido.getDado() instanceof String[] && ((String[]) pedido.getDado()).length == 2) {
                            try {
                                if (usuarioEstaLogado()) {
                                    try {
                                        String usu = ((String[]) pedido.getDado())[0];
                                        
                                        if (!usu.isEmpty() && (usu.equals(usuario.getUsuario()) || usuario.isAdmin())){
                                            getControle().getControlePersistencia().alterarImagemPerfil(usu, ((String[]) pedido.getDado())[1]);
                                            
                                            controleConexao.enviarResposta(new PacoteRede(true), pedido.getIndice());
                                        }
                                        
                                        else {
                                            controleConexao.enviarResposta(new PacoteRede(new AlteracaoException("Não é possível alterar dados de outra conta sem permissões.")), pedido.getIndice());
                                        }
                                    } catch (SemUsuarioException | ArquivoInexistenteException ex) {
                                        controleConexao.enviarResposta(new PacoteRede(ex), pedido.getIndice());
                                    }
                                }
                                
                                else {
                                    controleConexao.enviarResposta(new PacoteRede(new LoginException("Você não está logado para fazer edições!")), pedido.getIndice());
                                }
                            } catch (ConexaoEncerrada ex) {
                                //Vazio
                            }
                        }
                        
                        break;
                    }
                    
                    case IDsRede.ID_ALTERAR_APELIDO: {
                        if (pedido.getDado() != null && pedido.getDado() instanceof String[] && ((String[]) pedido.getDado()).length == 2) {
                            try {
                                if (usuarioEstaLogado()) {
                                    try {
                                        String usu = ((String[]) pedido.getDado())[0];
                                        
                                        if (!usu.isEmpty() && (usu.equals(usuario.getUsuario()) || usuario.isAdmin())){
                                            getControle().getControlePersistencia().alterarApelidoUsuario(usu, ((String[]) pedido.getDado())[1]);
                                        }
                                        
                                        else {
                                            controleConexao.enviarResposta(new PacoteRede(new AlteracaoException("Não é possível alterar dados de outra conta sem permissões.")), pedido.getIndice());
                                        }
                                        
                                        controleConexao.enviarResposta(new PacoteRede(true), pedido.getIndice());
                                    } catch (SemUsuarioException ex) {
                                        controleConexao.enviarResposta(new PacoteRede(ex), pedido.getIndice());
                                    }
                                }
                                
                                else {
                                    controleConexao.enviarResposta(new PacoteRede(new LoginException("Você não está logado para fazer edições!")), pedido.getIndice());
                                }
                            } catch (ConexaoEncerrada ex) {
                                //Vazio
                            }
                        }
                        
                        break;
                    }
                    
                    case IDsRede.ID_ALTERAR_SENHA: {
                        if (pedido.getDado() != null && pedido.getDado() instanceof String[] && ((String[]) pedido.getDado()).length == 2) {
                            try {
                                if (usuarioEstaLogado()) {
                                    try {
                                        String usu = ((String[]) pedido.getDado())[0];
                                        
                                        if (!usu.isEmpty() && (usu.equals(usuario.getUsuario()) || usuario.isAdmin())){
                                            getControle().getControlePersistencia().alterarSenhaUsuario(usu, ((String[]) pedido.getDado())[1]);
                                        }
                                        
                                        else {
                                            controleConexao.enviarResposta(new PacoteRede(new AlteracaoException("Não é possível alterar dados de outra conta sem permissões.")), pedido.getIndice());
                                        }
                                        
                                        controleConexao.enviarResposta(new PacoteRede(true), pedido.getIndice());
                                    } catch (SemUsuarioException ex) {
                                        controleConexao.enviarResposta(new PacoteRede(ex), pedido.getIndice());
                                    }
                                }
                                
                                else {
                                    controleConexao.enviarResposta(new PacoteRede(new LoginException("Você não está logado para fazer edições!")), pedido.getIndice());
                                }
                            } catch (ConexaoEncerrada ex) {
                                //Vazio
                            }
                        }
                        
                        break;
                    }
                }
            }

            else if (tipoConexao == IDsRede.ID_TIPOCONEXAO_UPLOAD) {
                if (pedido.getID() == IDsRede.ID_ENVIAR_ARQUIVO) {
                    if (pedido.getDado() instanceof ArquivoExtendido) {
                        ArquivoExtendido arq = ((ArquivoExtendido) pedido.getDado());

                        try {
                            if (arq.getTamanhoArquivo() <= ArquivoExtendido.TAMANHO_LIMITE_ARQUIVOS) {
                                controleConexao.enviarResposta(new PacoteRede(getControle().getControlePersistencia().getControleArquivos().guardarArquivo(arq).getSHA256()), pedido.getIndice());
                            }

                            else {
                                controleConexao.enviarResposta(new PacoteRede(ArquivoExtendido.EXCEPTION_TAMANHO_EXCEDIDO), pedido.getIndice());
                            }
                        } catch (ConexaoEncerrada ex) {}
                    }
                }

                if (pedido.getID() == IDsRede.ID_VERIFICAR_EXISTENCIA_ARQUIVO) {
                    if (pedido.getDado() instanceof String) {
                        try {
                            controleConexao.enviarResposta(new PacoteRede(getControle().getControlePersistencia().getControleArquivos().arquivoExiste((String) pedido.getDado())), pedido.getIndice());
                        } catch (ConexaoEncerrada ex) {}
                    }
                }
            }

            else if (tipoConexao == IDsRede.ID_TIPOCONEXAO_DOWNLOAD) {
                if (pedido.getID() == IDsRede.ID_BAIXAR_ARQUIVO) {
                    if (pedido.getDado() instanceof String) {
                        try {
                            ArquivoExtendido arq;
                            try {
                                arq = getControle().getControlePersistencia().getControleArquivos().carregarArquivo((String) pedido.getDado());

                                controleConexao.enviarResposta(new PacoteRede(arq), pedido.getIndice());
                            } catch (ArquivoInexistenteException ex) {
                                controleConexao.enviarResposta(new PacoteRede(ex), pedido.getIndice());
                            }
                        } catch (ConexaoEncerrada ex) {

                        }
                    }
                }
            }
        }
        
        else {
            if (pedido.getID() == IDsRede.ID_VERIFICAR_VERSAO) {
                if (pedido.getDado() instanceof Integer) {
                    int versaoCliente = (Integer) pedido.getDado();
                    
                    try {
                        if (versaoCliente == Controle.VER_COMP) {
                            controleConexao.enviarResposta(new PacoteRede(true), pedido.getIndice());
                            versaoValida = true;
                        }
                        
                        else if (versaoCliente < Controle.VER_COMP) {
                            controleConexao.enviarResposta(new PacoteRede(new VersaoDiferenteException("Sua versão do aplicativo está desatualizada.")), pedido.getIndice());
                        }
                        
                        else {
                            controleConexao.terminarConexao("O servidor ainda não está atualizado, tente novamente mais tarde.", true);
                        }
                    } catch (ConexaoEncerrada ex) {
                    }
                }
            }
            
            else if (pedido.getID() == IDsRede.ID_BAIXAR_CLIENTE_ATUALIZADO) {
                
            }
        }
    }
    
    public Usuario obterUsuario() throws LoginException {
        if (usuario == null) {
            throw new LoginException("O usuário não está logado.");
        }
        
        return usuario;
    }
    
    public boolean usuarioEstaLogado() {
        return (usuario != null);
    }
    
    public void deslogarCliente(String msg) throws LoginException {
        if (usuario == null) {
            throw new LoginException("O usuário não está logado.");
        }
        
        Usuario usr = usuario;
        usuario = null;
        
        getControle().getControleConexoes().setClienteDeslogado(this, usr);
        
        try {
            controleConexao.enviarPedido(new PacoteRede(IDsRede.ID_FAZER_LOGOFF, msg));
        } catch (ConexaoEncerrada ex) {}
    }
    
    public ControladorSocket getControleConexao() {
        return controleConexao;
    }
    
    public void enviarMensagem(Mensagem m) throws LoginException {
        if (!usuarioEstaLogado()) {
            throw new LoginException("O usuário ainda não efetivou login.");
        }
        
        try {
            controleConexao.enviarPedido(new PacoteRede(IDsRede.ID_NOVA_MENSAGEM, m));
        } catch (ConexaoEncerrada ex) {
            //Salvar mensagem para enviar depois
        }
    }
    
    public int getTipoConexao() {
        return tipoConexao;
    }
    
    public void notificarLogout(Usuario deslogado) throws LoginException {
        if (usuarioEstaLogado()) {
            try {
                controleConexao.enviarPedido(new PacoteRede(IDsRede.ID_NOTIFICACAO_LOGOUT, deslogado));
            } catch (ConexaoEncerrada ex) {}
        }
        
        else {
            throw new LoginException("Este cliente ainda não está logado.");
        }
    }
    
    public void notificarLogin(Usuario logado) throws LoginException {
        if (usuarioEstaLogado()) {
            try {
                controleConexao.enviarPedido(new PacoteRede(IDsRede.ID_NOTIFICACAO_LOGIN, logado));
            } catch (ConexaoEncerrada ex) {}
        }
        
        else {
            throw new LoginException("Este cliente ainda não está logado.");
        }
    }
}
