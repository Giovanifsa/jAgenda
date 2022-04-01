/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.cliente;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author giovani
 */
public class ItemAgenda implements Serializable{
    private String descricao;
    private String titulo;
    private Date dataEvento;
    
    private byte horas, minutos, segundos;
    
    /*
     * TODO: Tornar as horas, minutos e segundos um objeto só, ou utilizar alguma classe do java que faz isso.
    */
    
    /**
     * Constrói um novo ItemAgenda, informação que fará parte de uma ListaAgenda.
     * @param titulo        Título do Item;
     * @param descricao     Descrição do Item;
     * @param dataEvento    Data que ocorrerá o evento;
     * @param hora          Hora que ocorrerá o evento;
     * @param minuto        Minuto que ocorrerá o evento;
     * @param segundo       Segundo que ocorrerá o evento.
     */
    public ItemAgenda(String titulo, String descricao, Date dataEvento, byte hora, byte minuto, byte segundo) {
        
    }
    
    public ItemAgenda(String titulo, String descricao, String data, byte hora, byte minuto, byte segundo) {
        Date dataObj;
        
        //Regex para a data
        
        //this(titulo, descricao, dataObj, hora, minuto, segundo);
    }
}
