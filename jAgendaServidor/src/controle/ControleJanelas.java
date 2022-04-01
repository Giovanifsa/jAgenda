/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import static controle.Controle.getControle;
import java.util.ArrayList;
import modelo.rede.Conexao;
import visao.janelas.JanelaDadosConexao;
import visao.janelas.JanelaPrincipal;

/**
 *
 * @author Giovani
 */
public class ControleJanelas {
    //Lista que guarda as conexões já sendo visualizadas
    private static ArrayList<Conexao> jaAbertos = new ArrayList<>();
    
    private JanelaPrincipal janelaPrincipal;

    public ControleJanelas() {
        
    }
    
    public void abrirDadosConexao(Conexao con) {
        if (!jaAbertos.contains(con)) {
            jaAbertos.add(con);
            
            new JanelaDadosConexao(con).setVisible(true);
        }
    }
    
    public void dadoConexaoFechado(Conexao con) {
        jaAbertos.remove(con);
    }
    
    public void abrirJanelaPrincipal() {
        janelaPrincipal = new JanelaPrincipal();
        janelaPrincipal.setVisible(true);
    }
    
    public void setOffline() {
        janelaPrincipal.alterarInterfaceOffline();
    }
    
    public void setOnline() {
        janelaPrincipal.alterarInterfaceOnline();
    }
    
    public void atualizarListaConexoes() {
        janelaPrincipal.atualizarConexoes(getControle().getControleConexoes().obterConexoes());
    }
    
    public void atualizarListaUsuarios() {
        janelaPrincipal.setListaUsuarios(getControle().getControlePersistencia().getBanco().getTodosUsuarios());
    }
    
    public void atualizarListaGrupos() {
        
    }
    
    public void atualizarListaAgendas() {
        
    }
    
    public void escreverLinhaConsole(String mensagem) {
        janelaPrincipal.escreverConsoleLn(mensagem);
    }
}
