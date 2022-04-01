/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import static controle.Controle.getControle;
import exceptions.ConexaoEncerrada;
import exceptions.RespostaTimeoutException;
import java.awt.Frame;
import javax.swing.JOptionPane;
import modelo.cliente.Mensagem;
import modelo.cliente.Usuario;
import modelo.exceptions.OfflineException;
import visao.janelas.JanelaConta;
import visao.janelas.JanelaLogin;
import visao.janelas.JanelaNovaConta;
import visao.janelas.JanelaPrincipal;

/**
 *
 * @author Giovani
 */
public class ControleJanelas {
    private JanelaPrincipal janelaPrincipal;
    private JanelaLogin janelaLogin;
    private JanelaNovaConta janelaNovaConta;
    private JanelaConta janelaConta;
    
    public JanelaPrincipal obterJanelaPrincipal() {
        return janelaPrincipal;
    }
    
    public void mostrarTelaLogin() {
        fecharTelaLogin();
        
        janelaLogin = new JanelaLogin();
        janelaLogin.setLocationRelativeTo(null);
        janelaLogin.setVisible(true);
    }
    
    public void fecharTelaLogin() {
        if (janelaLogin != null) {
            janelaLogin.dispose();
            janelaLogin = null;
        }
    }
    
    
    public void mensagemRecebida(Mensagem m) {
        janelaPrincipal.mensagemRecebida(m);
    }
    
    public void atualizarChat() {
        janelaPrincipal.atualizarChat();
    }
    
    public void abrirJanelaPrincipal(Usuario usr) {
        fecharJanelaPrincipal();
        
        janelaPrincipal = new JanelaPrincipal();
        janelaPrincipal.setVisible(true);
        
        atualizarUsuariosOnlineCompleto();
    }
    
    public void fecharJanelaPrincipal() {
        if (janelaPrincipal != null) {
            janelaPrincipal.dispose();
            janelaPrincipal = null;
        }
    }
    
    public void abrirJanelaNovaConta() {
        if (janelaNovaConta != null) {
            janelaNovaConta.setState(Frame.NORMAL);
            janelaNovaConta.toFront();
        }
        
        else {
            janelaNovaConta = new JanelaNovaConta();
            janelaNovaConta.setVisible(true);
        }
    }
    
    public void fecharJanelaNovaConta() {
        if (janelaNovaConta != null) {
            janelaNovaConta.dispose();
            janelaNovaConta = null;
        }
    }
    
    public void loginRealizado(Usuario usr) {
        fecharJanelaNovaConta();
        fecharTelaLogin();
        abrirJanelaPrincipal(usr);
    }

    public void logoffExecutado(String msg) {
        fecharJanelaPrincipal();
        mostrarTelaLogin();
        
        if (msg != null) {
            JOptionPane.showMessageDialog(janelaLogin, msg);
        }
    }
    
    public void atualizarUsuariosOnlineCompleto() {
        if (janelaPrincipal != null) {
            try {
                janelaPrincipal.setListaUsuariosOnline(getControle().getControleConexoes().obterUsuariosOnline());
            } catch (OfflineException ex) {
                
            } catch (ConexaoEncerrada ex) {
                
            } catch (RespostaTimeoutException ex) {
                JOptionPane.showMessageDialog(janelaLogin, "Não foi possível obter todos os usuários online. <ERRO: timeout>");
            }
        }
    }
    
    public void abrirTelaConta(Usuario usr) {
        if (janelaConta != null) {
            janelaConta.setState(Frame.NORMAL);
            janelaConta.toFront();
        }
        
        else {
            janelaConta = new JanelaConta(usr);
            janelaConta.setVisible(true);
        }
    }
    
    public void fecharTelaConta() {
        if (janelaConta != null) {
            janelaConta.dispose();
            janelaConta = null;
        }
    }
}
