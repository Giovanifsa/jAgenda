/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import modelo.Helpers;

/**
 *
 * @author giovani
 */
public class JFrameAnimado extends javax.swing.JFrame {
    private final int tamanhoPulo = 30;
    private final int correcaoWindows = 32, correcaoMac = 22;
    private int correcaoH = correcaoMac; //Padrão
    
    private CustomJPanel painelAtivo = null;
    
    public static WindowAdapter WINDOW_ADAPTER_COMUM = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
    
    public JFrameAnimado() {
        super("Vazio");
        
        switch (Helpers.getSO()) {
            case Windows: {
                correcaoH = correcaoWindows;
                break;
            }
            
            case MacOS: {
                correcaoH = correcaoMac;
                break;
            }
        }
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
        
        setResizable(false);
    }
    
    public JFrameAnimado(WindowListener listener) {
        this();
        addWindowListener(listener);
    }
    
    public JFrameAnimado(CustomJPanel painel) {
        this();
        
        alterarPainel(painel, true, false);
        setVisible(true);
    }
    
    public JFrameAnimado(CustomJPanel painel, WindowListener listener) {
        this(painel);
        
        addWindowListener(listener);
    }
    
    public void alterarPainel(CustomJPanel painel, boolean modificarTamanho, boolean chamarAoRemover) {
        setJMenuBar(null);
        
        painel.setVisible(true);
        painel.setSize(painel.getPreferredSize().width, painel.getPreferredSize().height);
        
        getContentPane().removeAll();
        
        if (painelAtivo != null) {
            painelAtivo.aoRemover();
        }
        
        getContentPane().add(painel);
        
        if (modificarTamanho) {
            setSize(painel.getPreferredSize().width, painel.getPreferredSize().height + correcaoH);
        }

        if (painel.obterMenu() != null) {
            painel.obterMenu().setVisible(true);
            painel.obterMenu().setSize(painel.obterMenu().getPreferredSize());
            setJMenuBar(painel.obterMenu());
        }
        
        painelAtivo = painel;
        
        if (painel.obterTituloJanela() != null) {
            setTitle(painel.obterTituloJanela());
        }
        
    }
            
    public void alterarPainelAnimado(CustomJPanel painel, boolean chamarAoRemover) {
        new Thread() {
            @Override
            public void run() {
                alterarPainel(painel, false, chamarAoRemover);
        
                while (getWidth() != painel.getWidth() || getHeight() != (painel.getHeight() + correcaoH)) {

                    try {
                        SwingUtilities.invokeAndWait(() -> {
                            int puloW = tamanhoPulo;
                            int puloH = tamanhoPulo;

                            if (getWidth() + tamanhoPulo > painel.getWidth() && getWidth() - tamanhoPulo < painel.getWidth()) {
                                puloW = 1;
                            }

                            if (getWidth() > painel.getWidth()) {
                                setSize(getWidth() - puloW, getHeight());
                            }

                            else if (getWidth() < painel.getWidth()) {
                                setSize(getWidth() + puloW, getHeight());
                            }

                            if (getHeight() + tamanhoPulo > (painel.getHeight() + correcaoH) && getHeight() - tamanhoPulo < (painel.getHeight() + correcaoH)) {
                                puloH = 1;
                            }

                            if (getHeight() > (painel.getHeight() + correcaoH)) {
                                setSize(getWidth(), getHeight() - puloH);
                            }

                            else if (getHeight() < (painel.getHeight() + correcaoH)){
                                setSize(getWidth(), getHeight() + puloH);
                            }     
                        });
                    } catch (InterruptedException | InvocationTargetException ex) {}
                }
            }
        }.start();
    }
}
