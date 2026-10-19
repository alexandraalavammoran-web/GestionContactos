//Este seria la clase principal y su función es arrancar el programa
//Este archivo contiene el método Main y la instacia de la ventana gráfica
//Tambien inicializa el controlador y hace que la aplicación aparezca visible en la pantalla de mi usuario.
package com.mycompany.gestioncontactos;

import Controller.logicaContactos;
import View.VentanaContactos;

public class GestionContactos {

    public static void main(String[] args) {
        // 1. Creamos la instancia de la ventana gráfica
        VentanaContactos ventana = new VentanaContactos();
        
        // 2. Inicializamos el controlador pasando la ventana como parámetro (Patrón MVC)
        new logicaContactos(ventana);
        
        // 3. Hacemos visible la ventana en el centro de la pantalla
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }
}