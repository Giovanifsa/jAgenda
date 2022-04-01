/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.cliente;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author giovani
 */
public class Agenda implements Serializable {
    //Nenhuma método desta classe será utilizado pelo servidor, e sim são apenas clones da classe que está no cliente, com a razão
    //de permitir o funcionamento do Serializable.
    
    private final ArrayList<ItemAgenda> itensAgenda = new ArrayList<>();
    private final ArrayList<Integer> indicesAdministradores = new ArrayList<>();
    private String nomeLista, sha256Imagem;
    
    /**
     * Constrói o objeto Agenda com o nome desejado.
     * @param nome 
     */
    public Agenda(String nome) {
        nomeLista = nome;
    }
    
    public Agenda(String nome, String sha256imagemSalva) {
        nomeLista = nome;
        sha256Imagem = sha256imagemSalva;
    }
    
    /**
     * Altera o nome da agenda.
     * @param nome Novo nome para a agenda.
     */
    public void alterarNome(String nome) {
        nomeLista = nome;
    }
    
    /**
     * Adiciona um novo administrador.
     * @param indiceCliente Índice do usuário que se tornará administrador.
     */
    public void adicionarAdministrador(int indiceCliente) {
        for (int ind : indicesAdministradores) {
            if (ind == indiceCliente) {
                return;
            }
        }
        
        indicesAdministradores.add(indiceCliente);
    }
    
    /**
     * Remove um administrador.
     * @param indiceCliente Índice do usuário que será removido da administração.
     */
    public void removerAdministrador(int indiceCliente) {
        
    }
    
    /**
     * Adiciona um novo item à esta agenda.
     * @param item
     * @return 
     */
    public int adicionarItem(ItemAgenda item) {
        itensAgenda.add(item);
        
        return itensAgenda.size() - 1;
    }
    
    /**
     * Remove um item desta agenda à partir do seu índice.
     * @param indiceItem Índice do item.
     */
    public void removerItem(int indiceItem) {
        itensAgenda.remove(indiceItem);
    }
    
    /**
     * Remove um item desta agenda à partir do seu objeto.
     * @param item Objeto do item.
     */
    public void removerItem(ItemAgenda item) {
        itensAgenda.remove(item);
    }
    
    public boolean contemAdministrador(int indiceUsuario) {
        for (int i : indicesAdministradores) {
            if (i == indiceUsuario) {
                return true;
            }
        }
        
        return false;
    }
    
    public String getSHA256Imagem() {
        return sha256Imagem;
    }
    
    public void setSHA256Imagem(String sha256) {
        sha256Imagem = sha256;
    }
    
    public String getNomeAgenda() {
        return nomeLista;
    }
}
