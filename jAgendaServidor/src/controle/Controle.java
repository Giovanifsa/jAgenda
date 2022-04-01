/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import modelo.Helpers;
import persistencia.ControlePersistencia;

/**
 *
 * @author giovani
 */

public class Controle {
    //Main
    public static void main(String[] args) {
        new Controle();
    }
    
    public static int VER_COMP = 2;
    
    private static Controle controleIniciado = null;
    private final ControleConexoes contConexoes;
    private final ControleJanelas contJanelas;
    private final ControlePersistencia contPersistencia;

    private Controle() {
        controleIniciado = this;
        
        contPersistencia = new ControlePersistencia(ControlePersistencia.Banco.DB4O);
        
        contJanelas = new ControleJanelas();
        contConexoes = new ControleConexoes();
        
        contJanelas.abrirJanelaPrincipal();
        
        contJanelas.atualizarListaUsuarios();
    }
    
    public static Controle getControle() {
        return controleIniciado;
    }
    
    public ControleJanelas getControleJanelas() {
        return contJanelas;
    }
    
    public ControleConexoes getControleConexoes() {
        return contConexoes;
    }
    
    public ControlePersistencia getControlePersistencia() {
        return contPersistencia;
    }
    
    public void setOffline() {
        contJanelas.setOffline();
    }
    
    public void setOnline() {
        contJanelas.setOnline();
    }
    
    public void reiniciarPrograma() {
        contConexoes.encerrarServico();
        contPersistencia.encerrarTudo();
        
        Helpers.reiniciarPrograma();
    }
    
    public void encerrarPrograma() {
        contConexoes.encerrarServico();
        contPersistencia.encerrarTudo();
        
        System.exit(0);
    }

    public void erroFatal(Exception ex) {
        contConexoes.encerrarServico();
    }
}
