/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistencia;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import static controle.Controle.getControle;
import java.util.ArrayList;
import modelo.Helpers;
import modelo.cliente.Usuario;
import modelo.exceptions.SemUsuarioException;

/**
 *
 * @author giovani
 */

public class BancoDB4O implements InterfaceBanco {
    public static final String DB4O_DIRETORIO_PADRAO = Helpers.obterDiretorioAtual() + "/DB4O_Usuarios.bin";
    private final ObjectContainer bancoDB4O;
    
    public BancoDB4O() {
        bancoDB4O = Db4o.openFile(DB4O_DIRETORIO_PADRAO);
    }
    
    @Override
    public synchronized ArrayList<Usuario> getTodosUsuarios() {
        ObjectSet<Usuario> osUsuarios = bancoDB4O.query(Usuario.class);
        
        ArrayList<Usuario> listaUs = new ArrayList<>();
        
        for (Usuario usr : osUsuarios) {
            listaUs.add(usr);
        }
        
        return listaUs;
    }
    
    @Override
    public synchronized void encerrarBanco() {
        bancoDB4O.close();
    }

    @Override
    public synchronized boolean usuarioExiste(String usuario) {
        for (Usuario us : getTodosUsuarios()) {
            if (us.getUsuario().equals(usuario)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public synchronized void salvarUsuario(Usuario usr) {
        for (Usuario user : getTodosUsuarios()) {
            if (user.getUsuario().equals(usr.getUsuario())) {
                bancoDB4O.delete(user);
                break;
            }
        }
        
        bancoDB4O.store(usr);
        bancoDB4O.commit();
        
        getControle().getControleJanelas().atualizarListaUsuarios();
    }

    @Override
    public void excluirUsuario(String usuario) throws SemUsuarioException {
        if (!usuarioExiste(usuario)) {
            throw new SemUsuarioException("Este usuário não existe.");
        }
        
        for (Usuario usr : getTodosUsuarios()) {
            if (usr.getUsuario().equals(usuario)) {
                bancoDB4O.delete(usr);
                break;
            }
        }
        
        getControle().getControleJanelas().atualizarListaUsuarios();
    }
}
