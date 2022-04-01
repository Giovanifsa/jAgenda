/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import javax.swing.JMenuBar;
import javax.swing.JPanel;

/**
 *
 * @author giovani
 */
public abstract class CustomJPanel extends JPanel{
    private JMenuBar menu = null;
    private CustomJPanel painelAnterior;
    private JFrameAnimado janela;
    private String tituloJanela;
    
    public CustomJPanel(String tituloJanela) {
        this.tituloJanela = tituloJanela;
    }
    
    public CustomJPanel(CustomJPanel painelAnt, JFrameAnimado janelaCallback) {
        painelAnterior = painelAnt;
        janela = janelaCallback;
    }
    
    public CustomJPanel(CustomJPanel painelAnt, JFrameAnimado janelaCallback, String tituloJanela) {
        this(painelAnt, janelaCallback);
        this.tituloJanela = tituloJanela;
    }
    
    public void setMenu(JMenuBar menu) {
        this.menu = menu;
    }
    
    public JMenuBar obterMenu() {
        return menu;
    }
    
    protected CustomJPanel obterPainelAnterior() {
        return painelAnterior;
    }
    
    public String obterTituloJanela() {
        return tituloJanela;
    }
    
    public JFrameAnimado obterJanela() {
        return janela;
    }
    
    protected void voltarAoPainelAnterior(boolean chamarAoRemover) {
        if (janela != null && painelAnterior != null) {
            new Thread() {
                @Override
                public void run() {
                    janela.alterarPainelAnimado(painelAnterior, chamarAoRemover);
                }
            }.start();
        }
    }
    
    protected void alterarPainel(CustomJPanel novoPainel, boolean chamarAoRemover) {
        janela.alterarPainel(novoPainel, true, chamarAoRemover);
    }
    
    protected void alterarPainelAnimado(CustomJPanel novoPainel, boolean chamarAoRemover) {
        if (janela != null) {
            new Thread() {
                @Override
                public void run() {
                    janela.alterarPainelAnimado(novoPainel, chamarAoRemover);
                }
            }.start();
        }
    }
    
    public abstract void aoRemover();
}
