/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.frames;

import controle.Controle;
import static controle.Controle.getControle;
import controle.ItemFilaDownload;
import controle.ItemFilaUpload;
import exceptions.ConexaoEncerrada;
import exceptions.RespostaTimeoutException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.SizeLimitExceededException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import modelo.ArquivoExtendido;
import modelo.ArquivoGuardado;
import modelo.ControleArquivos;
import modelo.InterfaceHook;
import modelo.cliente.Mensagem;
import modelo.cliente.Usuario;
import modelo.exceptions.ArquivoInexistenteException;
import modelo.exceptions.LoginException;
import modelo.exceptions.OfflineException;
import modelo.exceptions.SemUsuarioException;

/**
 * Monta mensagens automaticamente com conteúdo HTML
 * @author giovani
 */
public class ConversaChatPrivadoHTML {
    private final Usuario destinatario;
    
    private final ArrayList<String> trocaMensagens = new ArrayList<>();
    
    public ConversaChatPrivadoHTML(Usuario dest) {
        destinatario = dest;
    }
    
    public ArrayList<String> carregarMensagens() {
        return trocaMensagens;
    }

    /**
     * Abre o seletor de arquivos e envia o arquivo para o usuário desta conversa.
     */
    public synchronized void selecionarArquivoEnvio() {
        JFileChooser jfc = new JFileChooser();
        jfc.setMultiSelectionEnabled(false);
        
        if (jfc.showOpenDialog(jfc) == JFileChooser.APPROVE_OPTION) {
            try {
                ArquivoExtendido arq = new ArquivoExtendido(jfc.getSelectedFile());
                
                ItemFilaUpload itemUp = new ItemFilaUpload(new InterfaceHook<String>() {
                    @Override
                    public void run(String obj) {
                        if (obj != null) {
                            try {
                                Controle.getControle().getControleConexoes().enviarMensagem(new Mensagem(obj, destinatario, true));
                                
                                inserirMensagem("<font color =\"green\">[INFO]</font> Arquivo enviado com sucesso! (ID: " + obj + ")");
                            } catch (OfflineException ex) {
                                inserirMensagem("<font color =\"red\">[ERRO] A conexão com o servidor foi perdida.</font>");
                            } catch (ConexaoEncerrada ex) {
                                inserirMensagem("<font color =\"red\">[ERRO] A conexão com o servidor foi perdida.</font>");
                            } catch (RespostaTimeoutException ex) {
                                inserirMensagem("<font color =\"yellow\">[ATENÇÃO] A mensagem talvez não tenha sido enviada: Nenhum dado obtido do servidor.</font>");
                            } catch (LoginException ex) {
                                inserirMensagem("<font color =\"red\">[ERRO] Sua sessão expirou, por favor, faça login novamente.</font>");
                            } catch (SemUsuarioException ex) {
                                inserirMensagem("<font color =\"red\">[ERRO] O usuário que você está tentando enviar mensagens não existe.</font>");
                            }
                        }
                    }
                }, arq);
                
                inserirMensagem("<font color =\"green\">[INFO]</font> Enviando arquivo \"" + arq.getNomeOriginal() + "." + (arq.getExtensao() != null ? arq.getExtensao() : "") + "\"!");
                
                if (arq.isImagem()) {
                    ArquivoGuardado arquivo = getControle().getControleArquivos().guardarArquivo(arq);
                    
                    inserirMensagem("<img src=\"file:" + ControleArquivos.ARQUIVOS_DIRETORIO_PADRAO + arquivo.getArquivoSalvo() + "\"/>");
                }
                
                getControle().getControleConexoes().novoUpload(itemUp);
                
            } catch (IOException ex) {
                getControle().getControleLogger().logarErro("IOException obtido no controle de conversas: " + ex.getMessage(), true);
            } catch (SizeLimitExceededException ex) {
                JOptionPane.showMessageDialog(getControle().getControleJanelas().obterJanelaPrincipal(), ex.getMessage());
            }
        }
    }
    
    public synchronized void inserirMensagem(String texto) {
        trocaMensagens.add(texto);
        
        getControle().getControleJanelas().atualizarChat();
    }
    
    public synchronized void mensagemRecebida(Mensagem m) {
        if (m.contemArquivo()) {
            ItemFilaDownload ifd = new ItemFilaDownload(m.getSHA256Arquivo(), new InterfaceHook<ArquivoExtendido>() {
                @Override
                public void run(ArquivoExtendido obj) {
                    if (obj != null) {
                        try {
                            ArquivoGuardado arq = getControle().getControleArquivos().getArquivo(obj.SHA256Arquivo());

                            if (obj.isImagem()) {
                                inserirMensagem("<font color =\"green\">[INFO]</font> Imagem baixada com sucesso! (" + ControleArquivos.ARQUIVOS_DIRETORIO_PADRAO + arq.getArquivoSalvo() + ")");
                                inserirMensagem("<img src=\"file:" + ControleArquivos.ARQUIVOS_DIRETORIO_PADRAO + arq.getArquivoSalvo() + "\"/>");
                            }

                            else {
                                inserirMensagem("<font color =\"green\">[INFO]</font> Arquivo baixado com sucesso! (" + ControleArquivos.ARQUIVOS_DIRETORIO_PADRAO + arq.getArquivoSalvo() + ")");
                                inserirMensagem("<font color =\"green\">[INFO]</font> Pré-visualização indisponível: Arquivo não é uma imagem.");
                            }

                        } catch (ArquivoInexistenteException ex) {
                            inserirMensagem("<font color =\"red\">[ERRO]</font> Não foi possível ler ou gravar o download em disco.");
                        }
                    }
                    
                    else {
                        inserirMensagem("<font color =\"red\">[ERRO]</font> Falha ao baixar o arquivo!");
                    }
                }
            });
            
            inserirMensagem("<font color =\"red\">" + m.obterRemetente().getApelidoUsuario() + "</font> enviou um arquivo! Iniciando download...");
            
            getControle().getControleConexoes().novoDownload(ifd);
        }
        
        else {
            inserirMensagem("<font color =\"red\">" + m.obterRemetente().getApelidoUsuario() + "</font> - " + m.obterMensagem());
        }
    }
    
    public synchronized void enviarMensagem(String texto) {
        try {
            getControle().getControleConexoes().enviarMensagem(new Mensagem(texto, destinatario, false));

            inserirMensagem("<font color =\"blue\">Você</font> - " + texto);
        } catch (OfflineException ex) {
            Logger.getLogger(ConversaChatPrivadoHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConexaoEncerrada ex) {
            Logger.getLogger(ConversaChatPrivadoHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RespostaTimeoutException ex) {
            Logger.getLogger(ConversaChatPrivadoHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoginException ex) {
            Logger.getLogger(ConversaChatPrivadoHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SemUsuarioException ex) {
            Logger.getLogger(ConversaChatPrivadoHTML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Usuario obterDestinatario() {
        return destinatario;
    }
}
