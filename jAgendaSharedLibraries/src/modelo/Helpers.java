/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author giovani
 */
public class Helpers {
    public static ImageIcon IMG_ICON_PADRAO = new ImageIcon(Helpers.class.getResource("/visao/imagens/user-icon 128.png"));
    
    public enum TipoOS { Windows, MacOS, Linux, Outro };
    
    public static String SHA256(String password) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");        
        
        return new String(sha256.digest(password.getBytes()));
    }
    
    public static ImageIcon redmImagemLabel(byte[] imagem, int tamanhoX, int tamanhoY) throws IOException {
        return new ImageIcon(byteArrayParaImagem(imagem).getScaledInstance(tamanhoX, tamanhoY, Image.SCALE_FAST));
    }
    
    public static ImageIcon redmImagemLabel(byte[] imagem, JLabel label) throws IOException {
        BufferedImage bImage = byteArrayParaImagem(imagem);
        
        return new ImageIcon(bImage.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_DEFAULT));
    }
    
    public static ImageIcon redmImagemLabel(BufferedImage bi, JLabel label) {
        return new ImageIcon(bi.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_DEFAULT));
    }
    
    public static BufferedImage byteArrayParaImagem(byte[] imagem) throws IOException {
        return (ImageIO.read(new ByteArrayInputStream(imagem)));
    }
    
    public static TipoOS getSO() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        
        TipoOS osDetec = TipoOS.Outro;
        
        if ((OS.contains("mac")) || (OS.contains("darwin"))) {
          osDetec = TipoOS.MacOS;
        } 
        
        else if (OS.contains("win")) {
          osDetec = TipoOS.Windows;
        } 
        
        else if (OS.contains("nux")) {
          osDetec = TipoOS.Linux;
        }
        
        return osDetec;
    }
    
    public static void reiniciarPrograma()
    {
        try {
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            final File currentJar = new File(Helpers.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            
            /* is it a jar file? */
            if(!currentJar.getName().endsWith(".jar"))
                return;
            
            /* Build command: java -jar application.jar */
            final ArrayList<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());
            
            final ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(Helpers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
    
    public static String obterDiretorioAtual() {
        return Paths.get("").toAbsolutePath().toString();
    }
    
    /**
     * Obtém a data e o horário da máquina.
     * @param formato Formato de identação do horário: dd - dia, MM - mês, yyyy - ano, HH - hora, mm - minutos, ss - segundos.
     * @return 
     */
    public static String obterDataHorario24H(String formato) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formato);
        LocalDateTime ldt = LocalDateTime.now();
        
        return dtf.format(ldt);
    }
} 
