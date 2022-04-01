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
public class Usuario implements Serializable {
    private String
            usuario,
            apelidoUsuario,
            senhaSHA256,
            email;
    private ArrayList<Integer> acessoListas = new ArrayList<>(); //Cache, pois as listas terão os índices de quem pode acessar.
    private ArrayList<Integer> administraListas = new ArrayList<>(); //Cache, pois as listas terão os índices de quem administra.
    private ArrayList<Usuario> listaAmigos = new ArrayList<>();
    private String SHA256ImagemPerfil;
    private boolean admin;
    private boolean ativo = true;
    
    /**
     * Constrói um novo usuário com os dados especificados.
     * @param usuario Identificação de usuário utilizado para login.
     * @param apelidoUsuario Apelido do usuário que será visível para outros usuários.
     * @param senhaSHA256 Hash de senha calculado do usuário.
     * @param email Email do usuário.
     * @param SHA256ImagemPerfil ID da imagem de perfil previamente enviada ao servidor.
     * @throws IllegalArgumentException 
     */
    public Usuario(String usuario, String apelidoUsuario, String senhaSHA256, String email, String SHA256ImagemPerfil) throws IllegalArgumentException{
        if (usuario == null || apelidoUsuario == null || senhaSHA256 == null || email == null) {
            throw new IllegalArgumentException("Argumentos nulos não são aceitos aqui.");
        }
        
        this.usuario = usuario;
        this.apelidoUsuario = apelidoUsuario;
        this.senhaSHA256 = senhaSHA256;
        this.email = email;
        this.SHA256ImagemPerfil = SHA256ImagemPerfil;
    }
    
    private Usuario(String usuario, String apelidoUsuario, String SHA256ImagemPerfil) throws IllegalArgumentException {
        if (usuario == null || apelidoUsuario == null) {
            throw new IllegalArgumentException("Argumentos nulos não são aceitos aqui.");
        }
        
        this.usuario = usuario;
        this.apelidoUsuario = apelidoUsuario;
        this.SHA256ImagemPerfil = SHA256ImagemPerfil;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenhaSHA256() {
        return senhaSHA256;
    }
    
    public void setApelidoUsuario(String apelido) {
        apelidoUsuario = apelido;
    }
    
    public void inativarUsuario() {
        ativo = false;
    }
    
    public String getApelidoUsuario() {
        return apelidoUsuario;
    }
    
    public String getEmail() {
        return email;
    }

    public ArrayList<Integer> getAcessoListas() {
        return acessoListas;
    }

    public String getSHA256ImagemPerfil() {
        return SHA256ImagemPerfil;
    }

    public ArrayList<Integer> getAdministraListas() {
        return administraListas;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setSenhaSHA256(String senhaSHA256) {
        this.senhaSHA256 = senhaSHA256;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAcessoListas(ArrayList<Integer> acessoListas) {
        this.acessoListas = acessoListas;
    }

    public void setAdministraListas(ArrayList<Integer> administraListas) {
        this.administraListas = administraListas;
    }
    
    public ArrayList<Usuario> getListaAmigos() {
        return listaAmigos;
    }
    
    public void adicionarAmigo(Usuario usr) {
        listaAmigos.add(usr);
    }

    public void setSHA256ImagemPerfil(String SHA256ImagemPerfil) {
        this.SHA256ImagemPerfil = SHA256ImagemPerfil;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    
    public boolean isAdmin() {
        return admin;
    }
    
    public Usuario clonarSemDados() {
        return new Usuario(usuario, apelidoUsuario, SHA256ImagemPerfil);
    }
}
