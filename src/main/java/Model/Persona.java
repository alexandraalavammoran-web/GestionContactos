//Es la clase base u objeto plano (POJO). 
//Define la estructura del dato de un contacto(sus atributos nombre, teléfono, email, categoría y si es favorito)
//Todo esto lo hace junto con sus métodos Getters y Setters.
package Model;

/**
 *
 * @author CRISTINA
 */
public class Persona {
    private String nombre;
    private String telefono;
    private String email;
    private String categoria;
    private boolean favorito;
    //Este es un constructor vacio por si acaso 
    public Persona(){
    } //Este es el constructor 
        public Persona(String nombre, String telefono, String email, String categorio, boolean favorito){
            this.nombre = nombre;
            this.telefono = telefono;
            this.email = email;
            this.categoria = categorio;
            this.favorito = favorito;
        }
        // Son los métodos GETTER Y SETTER
       public String getNombre() {return nombre;}
       public void setNombre(String nombre){this.nombre = nombre;}
       
       public String getTelefono() {return telefono;}
       public void setTelefono(String telefono){this.telefono = telefono;}
       
       public String getEmail() {return email;}
       public void setEmail(String email){this.email = email;}
       
       public String getCategoria() {return categoria;}
       public void setCategoria(String categoria){this.categoria = categoria;}
       
       public boolean isFavorito(){return favorito;}
       public void setFavorito(boolean favorito){this.favorito = favorito;}
    }

