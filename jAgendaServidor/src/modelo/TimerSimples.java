/*
 * Trabalho desenvolvido por Giovani Frigo e Jackson Renato.
 * Intuito: Ajudar no controle de alunos na assistência social e outros setores do IFPR.
 */
package modelo;

import static java.lang.Thread.sleep;

/**
 *
 * @author giovani
 * Versão: 1 (Versão do Sistema da Direção, 2016)
 * TODO: Resolver imprecisão do timer.
 */
public class TimerSimples {
    private volatile Thread thread;
    private volatile boolean pararThread = false;
    private volatile boolean pausarThread = false;
    private volatile boolean repetirThread = false;
    private volatile Runnable threadRunnable;
    private volatile long threadTempo = 0;
    
    /**
     * Pausa o cronômetro.
     */
    public void pausar() {
        if (thread != null) {
            pausarThread = true;
        }
    }
    
    /**
     * Despausa o cronômetro.
     */
    public void despausar() {
        if (thread != null) {
            pausarThread = false;
        }
    }
    
    /**
     * Verifica se o cronômetro está pausado.
     * @return Verdadeiro caso o cronômetro esteja pausado ou falso caso contrário.
     */
    public boolean estaPausado() {
        return pausarThread;
    }
    
    /**
     * Constrói o objeto TimerSimples.
     * @param rn        Objeto Runnable cujo método "void run()" será executado logo que a contagem do cronômetro termine.     
     * @param milis     Tempo em milésimos que o cronômetro utilizará para executar o método "void run()" do objeto Runnable.
     * @param repetir   Parâmetro booleano que define se o cronômetro deve se repetir depois que a contagem termine. A repetição pode ser parada chamando o método "parar()".
     */
    public TimerSimples(Runnable rn, long milis, boolean repetir) {
        threadRunnable = rn;
        threadTempo = milis;
        repetirThread = repetir;
    }
    
    /**
     * Inicia a contagem do cronômetro e, caso a contagem termine, executa o método "void run()" da classe abstrata Runnable.
     */
    public void iniciar() {
        if (thread != null || threadRunnable == null) {
            return;
        }
        
        thread = new Thread() {
            @Override
            public void run() {
                long tempobuf;
                
                while (!pararThread) {
                    tempobuf = threadTempo;
                    
                    while (tempobuf > 0) {
                        try {
                            sleep(1);
                        } catch (InterruptedException ex) {}
                        
                        if (pararThread) {
                            break;
                        }
                        
                        if (!pausarThread) {
                            tempobuf--;
                        }
                    }
                    
                    if (pararThread) {
                        break;
                    }
                    
                    threadRunnable.run();
                    
                    if (!repetirThread) {
                        break;
                    }
                }
                
                pararThread = false;
                pausarThread = false;
                thread = null;
            }
        };
        
        thread.start();
    }
    
    /**
     * Para o cronômetro.
     */
    public void parar() {
        if (thread != null) {
            pararThread = true;
        } 
    }
    
    /**
     * Verifica se o cronômetro está parado.
     * @return Verdadeiro caso esteja ou falso caso contrário.
     */
    public boolean estaParado() {
        return (thread == null);
    }
}
