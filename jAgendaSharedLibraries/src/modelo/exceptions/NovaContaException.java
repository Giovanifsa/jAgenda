/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.exceptions;

import java.io.Serializable;

/**
 *
 * @author giovani
 */
public class NovaContaException extends Exception implements Serializable {
    public NovaContaException(String msg) {
        super(msg);
    }
}
