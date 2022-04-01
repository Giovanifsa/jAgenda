/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.exceptions;

import java.io.Serializable;

/**
 *
 * @author ifpr
 */
public class AlteracaoException extends Exception implements Serializable {
    public AlteracaoException(String m) {
        super(m);
    }
}
