/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.cliente;

import java.util.ArrayList;

/**
 *
 * @author giovani
 */
public class GrupoClientes {
    private String nomeGrupo;
    private final ArrayList<Integer> indicesClientes = new ArrayList<>();
    private final ArrayList<Integer> indicesAdministradores = new ArrayList<>();
    
    /**
     * Constrói um objeto GrupoClientes, definindo já um nome para este grupo.
     * @param nomeGrupo Nome do grupo.
     */
    public GrupoClientes(String nomeGrupo) {
        this.nomeGrupo = nomeGrupo;
    }
    
    /**
     * Verifica se certo cliente está no grupo a partir do seu índice.
     * @param indiceCliente Índice que será verificado.
     * @return              Verdadeiro caso esteja ou falso caso contrário.
     */
    public boolean clienteEstaNoGrupo(int indiceCliente) {
        for (Integer i : indicesClientes) {
            if (i == indiceCliente) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adiciona um cliente ao grupo à partir do seu índice.
     * @param indiceCliente Índice do cliente que será adicionado.
     */
    public void adicionarCliente(int indiceCliente) {
        if (!clienteEstaNoGrupo(indiceCliente)) {
            indicesClientes.add(indiceCliente);
        }
    }
    
    /**
     * Obtém uma lista com todos os participantes do grupo (Clientes).
     * @return Lista de Clientes.
     */
    public ArrayList<Integer> obterParticipantes() {
        return indicesClientes;
    }
    
    /**
     * Retorna uma lista com todos os índices dos administradores do grupo.
     * @return Lista de índices (Integers).
     */
    public ArrayList<Integer> obterAdministradores() {
        return indicesAdministradores;
    }
    
    /**
     * Altera o nome do grupo.
     * @param novoNome Nome que será colocado.
     */
    public void setNome(String novoNome) {
        nomeGrupo = novoNome;
    }
}
