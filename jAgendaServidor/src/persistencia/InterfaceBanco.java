/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistencia;

import java.util.ArrayList;
import modelo.cliente.Usuario;
import modelo.exceptions.SemUsuarioException;

/**
 *
 * @author Giovani
 */
public interface InterfaceBanco {
    public ArrayList<Usuario> getTodosUsuarios();
    
    public void encerrarBanco();

    public boolean usuarioExiste(String usuario);

    public void salvarUsuario(Usuario usr);
    
    public void excluirUsuario(String usuario) throws SemUsuarioException;
}
