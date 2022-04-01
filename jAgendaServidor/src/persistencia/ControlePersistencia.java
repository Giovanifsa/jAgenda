/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistencia;

import static controle.Controle.getControle;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import modelo.ControleArquivos;
import modelo.Helpers;
import modelo.cliente.Usuario;
import modelo.exceptions.ArquivoInexistenteException;
import modelo.exceptions.LoginException;
import modelo.exceptions.SemUsuarioException;
import modelo.rede.Conexao;

/**
 *
 * @author Giovani
 */
public class ControlePersistencia {
    
    public static enum Banco {
        DB4O,
        MySQL,
        PostreSQL
    };
    
    private final Banco bancoSelecionado;
    private ControleArquivos controleArquivos;
    private InterfaceBanco banco;
    
    public ControlePersistencia(Banco selecaoBanco, String... parametrosBanco) {
        bancoSelecionado = selecaoBanco;
        controleArquivos = new ControleArquivos();
        
        if (bancoSelecionado == Banco.DB4O) {
            banco = new BancoDB4O();
        }
    }
    
    public Usuario getUsuario(String usr) throws SemUsuarioException {
        for (Usuario user : banco.getTodosUsuarios()) {
            if (user.getUsuario().equals(usr)) {
                return user;
            }
        }
        
        throw new SemUsuarioException("Usuário não encontrado.");
    }
    
    public void inativarUsuario(String usuario) throws SemUsuarioException {
        Usuario usr = getUsuario(usuario);
        usr.inativarUsuario();
        
        banco.salvarUsuario(usr);
        
        for (Conexao con : getControle().getControleConexoes().obterConexoesLogadas()) {
            try {
                //Fecha a conexão do usuário caso sua conta tenha sido inativada com ele online
                if (con.obterUsuario().getUsuario().equals(usuario)) {
                    con.deslogarCliente("Sua conta foi inativada.");
                }
            } catch (LoginException ex) {}
        }
    }
    
    public void alterarImagemPerfil(String usuario, String SHA256Imagem) throws SemUsuarioException, ArquivoInexistenteException {
        if (!controleArquivos.arquivoExiste(SHA256Imagem)) {
            throw new ArquivoInexistenteException("Arquivo de imagem não encontrado no servidor.");
        }
        
        Usuario usr = getUsuario(usuario);
        
        usr.setSHA256ImagemPerfil(SHA256Imagem);
        
        banco.salvarUsuario(usr);
        
        //Notificar alteração da imagem
    }
    
    public void alterarApelidoUsuario(String usuario, String apelido) throws SemUsuarioException {
        Usuario usr = getUsuario(usuario);
        
        usr.setApelidoUsuario(apelido);
        
        banco.salvarUsuario(usr);
        
        //Notificar alteração do apelido
    }
    
    public void alterarSenhaUsuario(String usuario, String novaSenha) throws SemUsuarioException {
        Usuario usr = getUsuario(usuario);
        
        try {
            usr.setSenhaSHA256(Helpers.SHA256(novaSenha));
            
            banco.salvarUsuario(usr);
        } catch (NoSuchAlgorithmException ex) {}
    }
    
    public ArrayList<Usuario> getTodosUsuariosSemDados() {
        ArrayList<Usuario> usrList = new ArrayList<>();
        
        for (Usuario usr : banco.getTodosUsuarios()) {
            usrList.add(usr.clonarSemDados());
        }
        
        return usrList;
    }
    
    public ControleArquivos getControleArquivos() {
        return controleArquivos;
    }
    
    public InterfaceBanco getBanco() {
        return banco;
    }
    
    public Banco getTipoBanco() {
        return bancoSelecionado;
    }
    
    public void encerrarTudo() {
        if (banco != null) {
            banco.encerrarBanco();
            banco = null;
        }
        
        if (controleArquivos != null) {
            controleArquivos.finalize();
            controleArquivos = null;
        }
    }
}
