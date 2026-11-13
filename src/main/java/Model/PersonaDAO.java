//Este se encarga de la persistencia y del manejo de los archivos.
//Recibe los datos del contacto y gestiona la escritura o almacenamiento físico (como guardar en archivos de texto o registros).
package Model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PersonaDAO {
    private File archivo;
    private Persona persona;
    
    //Este es el onstructor que recibe persona a guardar
    public PersonaDAO(Persona persona){
    this.persona = persona;
    this.archivo = new File("Contacto.txt");//Archivo de texto donde se guardara
    }
   //El método para escribir datos en el archivo texto
    public void escribirArchivo() throws IOException{
        FileWriter w = new FileWriter(archivo, true); // true ayuda a que no se sobrescriba, sino que agg
        PrintWriter p = new PrintWriter(w);
        
        //Escribimos datos separados por comas
        p.println(persona.getTelefono()+"," + persona.getEdad() + "," + persona.getTelefono() + "," + 
        persona.getCedula() + "," + persona.getEmail()+"," + persona.getPais() + "," + 
        persona.getCategoria()+","+ persona.isFavorito());
        
        p.close();
        w.close();
    }
}
