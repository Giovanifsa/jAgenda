/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

/**
 *
 * @author Giovani
 * @param <T>
 */
public interface InterfaceHook<T> {
    public void run(T obj);
}